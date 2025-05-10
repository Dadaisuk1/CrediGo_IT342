package com.credigo.backend.service; // Ensure this file is in src/main/java/com/credigo/backend/service/

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.credigo.backend.dto.PaymentResponse;
import com.credigo.backend.dto.PaymentStatusResponse;
import com.credigo.backend.dto.WalletTopUpRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

import reactor.core.publisher.Mono;
import java.util.Base64;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import lombok.extern.slf4j.Slf4j;
import com.credigo.backend.service.NotificationService;
import com.credigo.backend.service.EmailService;
import com.credigo.backend.service.NotificationService.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.credigo.backend.repository.PaymentRepository;
import com.credigo.backend.repository.UserRepository;
import com.credigo.backend.entity.Payment;
import com.credigo.backend.entity.PaymentStatus;
import com.credigo.backend.dto.PaymentRequestDTO;
import com.credigo.backend.exception.ResourceNotFoundException;

@Service
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {
    private final String secretKey;
    private final String successUrl;
    private final String cancelUrl;
    private final String webhookUrl;
    private final String apiVersion;
    private final WebClient webClient;
    private final WalletService walletService;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private EmailService emailService;

    public PaymentServiceImpl(
            @Value("${paymongo.secret.key}") String secretKey,
            @Value("${paymongo.base.url}") String baseUrl,
            @Value("${paymongo.success.url}") String successUrl,
            @Value("${paymongo.cancel.url}") String cancelUrl,
            @Value("${paymongo.webhook.url}") String webhookUrl,
            @Value("${paymongo.api.version}") String apiVersion,
            WalletService walletService) {

        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new IllegalArgumentException("PayMongo secret key is not configured.");
        }

        this.secretKey = secretKey;
        this.successUrl = successUrl;
        this.cancelUrl = cancelUrl;
        this.webhookUrl = webhookUrl;
        this.apiVersion = apiVersion;
        this.walletService = walletService;

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString((this.secretKey + ":").getBytes()))
                .defaultHeader("Accept", "application/json")
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("PayMongo-Version", apiVersion)
                .build();
    }

    @Override
    public PaymentResponse createWalletTopUpPaymentIntent(WalletTopUpRequest topUpRequest, String username) {
        String paymentType = topUpRequest.getPaymentType().toLowerCase();
        long amountInCentavos = topUpRequest.getAmount().multiply(BigDecimal.valueOf(100)).longValue();

        try {
            if ("card".equals(paymentType)) {
                return createCardPaymentIntent(amountInCentavos, username);
            } else if ("gcash".equals(paymentType) || "paymaya".equals(paymentType)) {
                return createEWalletSource(amountInCentavos, username, paymentType);
            } else {
                throw new IllegalArgumentException("Unsupported payment type: " + paymentType);
            }
        } catch (Exception e) {
            log.error("Payment creation failed", e);
            throw new RuntimeException("Could not create payment due to: " + e.getMessage(), e);
        }
    }

    private PaymentResponse createCardPaymentIntent(long amountInCentavos, String username) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("amount", amountInCentavos);
        attributes.put("payment_method_allowed", List.of("card"));
        attributes.put("currency", "PHP");
        attributes.put("description", "Wallet top-up for " + username);
        attributes.put("statement_descriptor", "CrediGo Top-up");

        Map<String, String> metadata = new HashMap<>();
        metadata.put("credigo_username", username);
        metadata.put("transaction_type", "wallet_topup");
        attributes.put("metadata", metadata);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("data", Map.of("attributes", attributes));

        log.info("Sending PayMongo payment intent request: {}", requestBody);

        try {
            Map<String, Object> response = webClient.post()
                    .uri("/payment_intents")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response == null || !(response.get("data") instanceof Map)) {
                throw new RuntimeException("Invalid response structure from PayMongo.");
            }

            Map<String, Object> responseData = (Map<String, Object>) response.get("data");
            Map<String, Object> responseAttributes = (Map<String, Object>) responseData.get("attributes");

            // Convert amount to Long regardless of whether it's Integer or Long
            Long amount = null;
            Object amtObj = responseAttributes.get("amount");
            if (amtObj instanceof Number) {
                amount = ((Number) amtObj).longValue();
            }

            return new PaymentResponse(
                (String) responseAttributes.get("client_key"),
                null,
                (String) responseAttributes.get("status"),
                (String) responseData.get("id"),
                (String) responseAttributes.get("currency"),
                amount
            );
        } catch (WebClientResponseException e) {
            log.error("PayMongo API error: {} - {}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("PayMongo API error: " + e.getMessage(), e);
        }
    }

    private PaymentResponse createEWalletSource(long amountInCentavos, String username, String type) {
        log.debug("Creating {} source for user {}", type, username);
        // Step 1: Create a Source
        Map<String, Object> sourceAttributes = new HashMap<>();
        sourceAttributes.put("amount", amountInCentavos);
        sourceAttributes.put("currency", "PHP");
        sourceAttributes.put("redirect", Map.of(
            "success", successUrl,
            "failed", cancelUrl
        ));
        sourceAttributes.put("type", type);

        // Include complete billing information
        Map<String, Object> billing = new HashMap<>();
        billing.put("name", username);
        billing.put("email", username + "@credigo.com");
        billing.put("phone", "09123456789");

        // Add complete address information
        Map<String, Object> address = new HashMap<>();
        address.put("line1", "123 CrediGo Street");
        address.put("line2", "Brgy. IT342");
        address.put("city", "Manila");
        address.put("state", "Metro Manila");
        address.put("postal_code", "1000");
        address.put("country", "PH");

        billing.put("address", address);
        sourceAttributes.put("billing", billing);

        // Add metadata
        Map<String, String> metadata = new HashMap<>();
        metadata.put("credigo_username", username);
        metadata.put("transaction_type", "wallet_topup");
        sourceAttributes.put("metadata", metadata);

        Map<String, Object> sourceRequestBody = new HashMap<>();
        sourceRequestBody.put("data", Map.of("attributes", sourceAttributes));

        log.info("Sending PayMongo source request for {}: {}", type, sourceRequestBody);

        try {
            log.debug("Making API call to /sources endpoint");
            Map<String, Object> sourceResponse = webClient.post()
                    .uri("/sources")
                    .bodyValue(sourceRequestBody)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (sourceResponse == null || !(sourceResponse.get("data") instanceof Map)) {
                log.error("Invalid source response from PayMongo: {}", sourceResponse);
                throw new RuntimeException("Invalid source response from PayMongo.");
            }

            Map<String, Object> sourceData = (Map<String, Object>) sourceResponse.get("data");
            String sourceId = (String) sourceData.get("id");
            Map<String, Object> sourceDataAttributes = (Map<String, Object>) sourceData.get("attributes");

            log.debug("Source created successfully with ID: {}", sourceId);

            // For GCash/PayMaya, we don't create a payment immediately
            // Instead, we return the checkout URL from the source and wait for webhook notification
            if (sourceDataAttributes != null && sourceDataAttributes.get("redirect") instanceof Map) {
                Map<String, Object> redirect = (Map<String, Object>) sourceDataAttributes.get("redirect");
                String checkoutUrl = (String) redirect.get("checkout_url");

                log.info("Checkout URL for {} payment: {}", type, checkoutUrl);

                // Get amount from response
                Long amount = null;
                Object amtObj = sourceDataAttributes.get("amount");
                if (amtObj instanceof Number) {
                    amount = ((Number) amtObj).longValue();
                }

                log.info("IMPORTANT: After user completes payment on the {} platform, your webhook handler must listen for 'source.chargeable' event", type);
                log.info("and then create a payment using the source ID: {}", sourceId);

                return new PaymentResponse(
                    null, // No client key for e-wallets
                    checkoutUrl,
                    (String) sourceDataAttributes.get("status"),
                    sourceId,
                    (String) sourceDataAttributes.get("currency"),
                    amount
                );
            } else {
                log.error("Missing redirect URL in PayMongo source response: {}", sourceDataAttributes);
                throw new RuntimeException("Missing redirect URL in PayMongo source response");
            }
        } catch (WebClientResponseException e) {
            log.error("PayMongo API error for {}: {} - {}", type, e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("PayMongo API error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error creating {} source: {}", type, e.getMessage(), e);
            throw new RuntimeException("Error creating payment source: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentResponse createPaymentLink(String orderId, String username) {
        try {
            // TODO: Get order details and amount from your order service
            // For now, using a dummy amount
            long amountInCentavos = 10000; // 100 PHP

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("amount", amountInCentavos);
            attributes.put("description", "Payment for Order " + orderId);
            attributes.put("remarks", "Order payment for " + username);
            attributes.put("payment_method_allowed", Arrays.asList("gcash", "paymaya"));
            attributes.put("currency", "PHP");

            // Add redirect URLs with orderId
            Map<String, String> redirect = new HashMap<>();
            redirect.put("success", successUrl + "?orderId=" + orderId);
            redirect.put("failed", cancelUrl + "?orderId=" + orderId);
            attributes.put("redirect", redirect);

            // Add metadata for tracking
            Map<String, String> metadata = new HashMap<>();
            metadata.put("order_id", orderId);
            metadata.put("username", username);
            attributes.put("metadata", metadata);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("data", Map.of("attributes", attributes));

            Map<String, Object> response = webClient.post()
                    .uri("/links")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response == null || !(response.get("data") instanceof Map)) {
                throw new RuntimeException("Invalid response structure from PayMongo.");
            }

            Map<String, Object> data = (Map<String, Object>) response.get("data");
            Map<String, Object> responseAttributes = (Map<String, Object>) data.get("attributes");

            return new PaymentResponse(
                null,
                (String) responseAttributes.get("checkout_url"),
                "pending",
                (String) data.get("id"),
                "PHP",
                amountInCentavos
            );

        } catch (Exception e) {
            log.error("Error creating payment link: {}", e.getMessage());
            throw new RuntimeException("Failed to create payment link: " + e.getMessage());
        }
    }

    @Override
    public boolean verifyPayment(String orderId, String username) {
        try {
            // TODO: Get the payment link ID associated with this order from your database
            // For now, we'll assume the orderId is the payment link ID
            String paymentLinkId = orderId;

            Map<String, Object> response = webClient.get()
                    .uri("/links/" + paymentLinkId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response == null || !(response.get("data") instanceof Map)) {
                throw new RuntimeException("Invalid response structure from PayMongo.");
            }

            Map<String, Object> data = (Map<String, Object>) response.get("data");
            Map<String, Object> attributes = (Map<String, Object>) data.get("attributes");

            // Check if the payment is paid
            String status = (String) attributes.get("status");
            return "paid".equals(status);

        } catch (Exception e) {
            log.error("Error verifying payment: {}", e.getMessage());
            throw new RuntimeException("Failed to verify payment: " + e.getMessage());
        }
    }

    @Override
    public PaymentStatusResponse checkPaymentStatus(String paymentIntentId, String username) {
        if (paymentIntentId == null || paymentIntentId.isBlank()) {
            return PaymentStatusResponse.failed(paymentIntentId, "Invalid payment intent ID");
        }

        try {
            log.info("Checking payment status for paymentIntentId: {}, username: {}", paymentIntentId, username);

            Map<String, Object> response = webClient.get()
                    .uri("/payment_intents/" + paymentIntentId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response == null || !(response.get("data") instanceof Map)) {
                log.warn("Invalid or empty response while checking payment status for {}", paymentIntentId);
                return PaymentStatusResponse.notFound(paymentIntentId);
            }

            Map<String, Object> responseData = (Map<String, Object>) response.get("data");
            Map<String, Object> responseAttributes = (Map<String, Object>) responseData.get("attributes");

            String status = (String) responseAttributes.get("status");
            Long amount = null;
            Object amtObj = responseAttributes.get("amount");
            if (amtObj instanceof Number) {
                amount = ((Number) amtObj).longValue();
            }
            String currency = (String) responseAttributes.get("currency");

            // Check if this payment is already credited to avoid double-crediting
            Map<String, Object> metadata = (Map<String, Object>) responseAttributes.get("metadata");
            String transactionType = null;
            String paymentUsername = null;

            if (metadata != null) {
                transactionType = (String) metadata.get("transaction_type");
                paymentUsername = (String) metadata.get("credigo_username");
            }

            // Verify this payment is for wallet top-up and for the current user
            if (!"wallet_topup".equals(transactionType)) {
                log.warn("Payment {} is not a wallet top-up transaction", paymentIntentId);
                return PaymentStatusResponse.failed(paymentIntentId, "Not a wallet top-up transaction");
            }

            if (!username.equals(paymentUsername)) {
                log.warn("Payment {} belongs to user {}, not {}", paymentIntentId, paymentUsername, username);
                return PaymentStatusResponse.failed(paymentIntentId, "Payment belongs to another user");
            }

            if ("succeeded".equals(status)) {
                // Payment succeeded - credit the wallet if needed
                try {
                    BigDecimal amountDecimal = BigDecimal.valueOf(amount).divide(new BigDecimal("100"));
                    String description = (String) responseAttributes.get("description");
                    if (description == null) {
                        description = "Wallet top-up via PayMongo";
                    }

                    walletService.addFundsToWallet(username, amountDecimal, paymentIntentId, description);
                    log.info("Successfully credited wallet for user {} via PayMongo payment {} ({} {})",
                            username, paymentIntentId, amountDecimal, currency);

                    return PaymentStatusResponse.succeeded(paymentIntentId, amount, currency, true);
                } catch (Exception e) {
                    log.error("Failed to credit wallet for paymentIntentId {}: {}", paymentIntentId, e.getMessage(), e);
                    return PaymentStatusResponse.succeeded(paymentIntentId, amount, currency, false);
                }
            } else {
                // Payment not yet succeeded
                return new PaymentStatusResponse(
                    paymentIntentId,
                    status,
                    false,
                    "Payment status: " + status,
                    amount,
                    currency
                );
            }

        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                log.warn("Payment intent {} not found", paymentIntentId);
                return PaymentStatusResponse.notFound(paymentIntentId);
            }
            log.error("PayMongo API error while checking payment status: {} - {}",
                    e.getRawStatusCode(), e.getResponseBodyAsString());
            return PaymentStatusResponse.failed(paymentIntentId, "API Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error checking payment status for {}: {}", paymentIntentId, e.getMessage(), e);
            return PaymentStatusResponse.failed(paymentIntentId, e.getMessage());
        }
    }

    @Override
    public Payment processPayment(PaymentRequestDTO paymentRequest) {
        // ... existing payment processing logic ...

        Payment payment = paymentRepository.save(newPayment);

        // Send transaction confirmation email
        String transactionDetails = String.format(
            "<p><strong>Transaction ID:</strong> %s</p>" +
            "<p><strong>Amount:</strong> ₱%.2f</p>" +
            "<p><strong>Status:</strong> %s</p>" +
            "<p><strong>Date:</strong> %s</p>",
            payment.getId(),
            payment.getAmount(),
            payment.getStatus(),
            payment.getCreatedAt()
        );

        emailService.sendTransactionConfirmationEmail(
            payment.getUser().getEmail(),
            payment.getUser().getUsername(),
            transactionDetails
        );

        // Send notification based on payment status
        switch (payment.getStatus()) {
            case SUCCESS:
                notificationService.sendSuccessNotification(
                    payment.getUser().getId().toString(),
                    String.format("Payment of ₱%.2f was successful", payment.getAmount())
                );
                break;
            case PENDING:
                notificationService.sendWarningNotification(
                    payment.getUser().getId().toString(),
                    String.format("Payment of ₱%.2f is pending", payment.getAmount())
                );
                break;
            case FAILED:
                notificationService.sendErrorNotification(
                    payment.getUser().getId().toString(),
                    String.format("Payment of ₱%.2f failed", payment.getAmount())
                );
                break;
        }

        return payment;
    }

    @Override
    public void updatePaymentStatus(Long paymentId, PaymentStatus status) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        payment.setStatus(status);
        paymentRepository.save(payment);

        // Send status update notification
        String message = String.format(
            "Your payment of ₱%.2f has been %s",
            payment.getAmount(),
            status.name().toLowerCase()
        );

        switch (status) {
            case SUCCESS:
                notificationService.sendSuccessNotification(payment.getUser().getId().toString(), message);
                break;
            case PENDING:
                notificationService.sendWarningNotification(payment.getUser().getId().toString(), message);
                break;
            case FAILED:
                notificationService.sendErrorNotification(payment.getUser().getId().toString(), message);
                break;
        }
    }
}
