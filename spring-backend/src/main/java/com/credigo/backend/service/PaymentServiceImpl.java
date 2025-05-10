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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.env.Environment;
import com.credigo.backend.repository.UserRepository;

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
    private final Environment environment;

    @Autowired
    private UserRepository userRepository;

    public PaymentServiceImpl(
            @Value("${paymongo.secret.key}") String secretKey,
            @Value("${paymongo.base.url}") String baseUrl,
            @Value("${paymongo.success.url}") String successUrl,
            @Value("${paymongo.cancel.url}") String cancelUrl,
            @Value("${paymongo.webhook.url}") String webhookUrl,
            @Value("${paymongo.api.version}") String apiVersion,
            WalletService walletService,
            Environment environment) {

        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new IllegalArgumentException("PayMongo secret key is not configured.");
        }

        this.secretKey = secretKey;
        this.environment = environment;

        // Check environment to determine if we should use local or production URLs
        boolean isDevelopment = isDevelopmentEnvironment();

        // Use local URL in development, otherwise use configured URL
        this.successUrl = isDevelopment ? "http://localhost:5173/payment/success" : successUrl;
        this.cancelUrl = isDevelopment ? "http://localhost:5173/payment/cancel" : cancelUrl;
        this.webhookUrl = webhookUrl;
        this.apiVersion = apiVersion;
        this.walletService = walletService;

        log.info("Payment Service initialized with success URL: {}", this.successUrl);
        log.info("Payment Service initialized with cancel URL: {}", this.cancelUrl);

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString((this.secretKey + ":").getBytes()))
                .defaultHeader("Accept", "application/json")
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("PayMongo-Version", apiVersion)
                .build();
    }

    // Add a helper method to detect development environment
    private boolean isDevelopmentEnvironment() {
        String[] activeProfiles = environment.getActiveProfiles();
        for (String profile : activeProfiles) {
            if (profile.equals("dev") || profile.equals("development") || profile.equals("local")) {
                return true;
            }
        }

        // Also check if we're running on localhost
        String serverPort = environment.getProperty("server.port");
        if (serverPort != null && !serverPort.equals("80") && !serverPort.equals("443")) {
            return true;
        }

        return false;
    }

    @Override
    public PaymentResponse createWalletTopUpPaymentIntent(WalletTopUpRequest topUpRequest, String username) {
        String paymentType = topUpRequest.getPaymentType().toLowerCase();
        long amountInCentavos = topUpRequest.getAmount().multiply(BigDecimal.valueOf(100)).longValue();

        try {
            if ("card".equals(paymentType)) {
                return createCardPaymentIntent(amountInCentavos, username);
            } else if ("gcash".equals(paymentType) || "paymaya".equals(paymentType)) {
                return createEWalletSource(amountInCentavos, username, paymentType, topUpRequest);
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

    private PaymentResponse createEWalletSource(long amountInCentavos, String username, String type, WalletTopUpRequest topUpRequest) {
        log.debug("Creating {} source for user {}", type, username);
        // Step 1: Create a Source
        Map<String, Object> sourceAttributes = new HashMap<>();
        sourceAttributes.put("amount", amountInCentavos);
        sourceAttributes.put("currency", "PHP");

        // Use redirect URLs from the request if provided, otherwise fall back to configured URLs
        String successUrl = (topUpRequest != null && topUpRequest.getSuccessRedirectUrl() != null)
            ? topUpRequest.getSuccessRedirectUrl()
            : this.successUrl;

        String cancelUrl = (topUpRequest != null && topUpRequest.getCancelRedirectUrl() != null)
            ? topUpRequest.getCancelRedirectUrl()
            : this.cancelUrl;

        log.info("Using redirect URLs - success: {}, cancel: {}", successUrl, cancelUrl);

        // Proper format for redirect
        Map<String, String> redirectMap = new HashMap<>();
        redirectMap.put("success", successUrl);
        redirectMap.put("failed", cancelUrl);
        sourceAttributes.put("redirect", redirectMap);

        sourceAttributes.put("type", type);

        // Include complete billing information
        Map<String, Object> billing = new HashMap<>();

        // Default billing info
        billing.put("name", "CrediGo User");
        billing.put("email", username + "@example.com");
        billing.put("phone", "09123456789");

        // Add complete address information
        Map<String, String> address = new HashMap<>();
        address.put("line1", "Test Address Line 1");
        address.put("line2", "Test Address Line 2");
        address.put("city", "Quezon City");
        address.put("state", "Metro Manila");
        address.put("postal_code", "1101");
        address.put("country", "PH");

        billing.put("address", address);
        sourceAttributes.put("billing", billing);

        // Add metadata
        Map<String, String> metadata = new HashMap<>();
        metadata.put("credigo_username", username);
        metadata.put("transaction_type", "wallet_topup");
        sourceAttributes.put("metadata", metadata);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("attributes", sourceAttributes);

        Map<String, Object> sourceRequestBody = new HashMap<>();
        sourceRequestBody.put("data", dataMap);

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
            throw new RuntimeException("PayMongo API error: " + e.getRawStatusCode() + " - " + e.getResponseBodyAsString());
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
}
