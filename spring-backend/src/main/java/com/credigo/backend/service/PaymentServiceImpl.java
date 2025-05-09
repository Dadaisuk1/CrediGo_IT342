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

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    private final String secretKey;
    private final String successUrl;
    private final String cancelUrl;
    private final String webhookUrl;
    private final String apiVersion;
    private final WebClient webClient;
    private final WalletService walletService;

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
        long amountInCentavos = topUpRequest.getAmount().multiply(BigDecimal.valueOf(100)).longValue();

        // Minimum amount check (100 PHP)
        if (amountInCentavos < 10000) {
            log.error("Amount too small: {} centavos. Minimum is 10000 centavos (₱100)", amountInCentavos);
            throw new IllegalArgumentException("Minimum amount for payment is ₱100");
        }

        try {
            // Use Links API for all payment types
            return createPaymentLinkForWalletTopUp(amountInCentavos, username);
        } catch (Exception e) {
            log.error("Payment creation failed", e);
            throw new RuntimeException("Could not create payment due to: " + e.getMessage(), e);
        }
    }

    private PaymentResponse createPaymentLinkForWalletTopUp(long amountInCentavos, String username) {
        String description = "Wallet top-up for " + username;

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("amount", amountInCentavos);
        attributes.put("description", description);
        attributes.put("remarks", "CrediGo wallet top-up");

        // Support multiple payment methods
        attributes.put("payment_method_allowed",
                       Arrays.asList("card", "gcash", "paymaya", "grab_pay"));
        attributes.put("currency", "PHP");

        // Add redirect URLs with specific return path to wallet page
        Map<String, String> redirect = new HashMap<>();
        // PayMongo will automatically append ?id=link_xxx and ?reference=xxx to the success URL
        // So our success URL will look like: /payment/success?redirect=/wallet&username=user123&id=link_abc123&reference=xyz
        redirect.put("success", successUrl + "?redirect=/wallet&username=" + username);
        redirect.put("failed", cancelUrl + "?redirect=/wallet&username=" + username);
        attributes.put("redirect", redirect);

        // Add metadata for tracking
        Map<String, String> metadata = new HashMap<>();
        metadata.put("credigo_username", username);
        metadata.put("transaction_type", "wallet_topup");
        attributes.put("metadata", metadata);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("data", Map.of("attributes", attributes));

        log.info("Creating Paymongo Link for wallet top-up: username={}, amount={}", username, amountInCentavos/100.0);

        try {
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

            // The checkout_url is what you redirect users to
            String checkoutUrl = (String) responseAttributes.get("checkout_url");
            String status = (String) responseAttributes.get("status");
            String referenceNumber = (String) responseAttributes.get("reference_number");
            String paymentId = (String) data.get("id");

            log.info("Created Paymongo Link: id={}, reference={}, status={}",
                    paymentId, referenceNumber, status);

            // Create a transaction record in the database
            try {
                // Convert amount to PHP from centavos
                BigDecimal phpAmount = BigDecimal.valueOf(amountInCentavos).divide(new BigDecimal("100"));

                // Add to transaction history (as pending)
                walletService.recordPendingTransaction(
                    username,
                    phpAmount,
                    "WALLET_TOPUP",
                    "Wallet top-up via PayMongo (" + paymentId + ")",
                    paymentId,
                    "PENDING"
                );

                log.info("Created pending transaction record for payment {}", paymentId);
            } catch (Exception e) {
                log.error("Failed to create transaction record for payment {}: {}", paymentId, e.getMessage(), e);
                // Continue anyway since the payment link is created
            }

            return new PaymentResponse(
                null,
                checkoutUrl,
                status,
                paymentId,
                "PHP",
                amountInCentavos
            );
        } catch (WebClientResponseException e) {
            log.error("PayMongo API error: {} - {}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("PayMongo API error: " + e.getMessage(), e);
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
            attributes.put("payment_method_allowed", Arrays.asList("card", "gcash", "paymaya", "grab_pay"));
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

            // Convert amount to Long regardless of whether it's Integer or Long
            Long amount = null;
            Object amtObj = responseAttributes.get("amount");
            if (amtObj instanceof Number) {
                amount = ((Number) amtObj).longValue();
            }

            return new PaymentResponse(
                null,
                (String) responseAttributes.get("checkout_url"),
                (String) responseAttributes.get("status"),
                (String) data.get("id"),
                (String) responseAttributes.get("currency"),
                amount
            );
        } catch (WebClientResponseException e) {
            log.error("PayMongo API error: {} - {}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("PayMongo API error: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyPayment(String orderId, String username) {
        // To verify a Link payment, check its status
        try {
            // Here you would typically retrieve the Link by ID or reference number
            // and check its status. For demonstration, we'll return true.
            log.info("Verifying payment for orderId: {}, username: {}", orderId, username);
            return true;
        } catch (Exception e) {
            log.error("Error verifying payment: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public PaymentStatusResponse checkPaymentStatus(String paymentId, String username) {
        try {
            log.info("Checking payment status for ID: {}, username: {}", paymentId, username);

            Map<String, Object> response = webClient.get()
                .uri("/links/" + paymentId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

            if (response == null || !(response.get("data") instanceof Map)) {
                throw new RuntimeException("Invalid response structure from PayMongo.");
            }

            Map<String, Object> data = (Map<String, Object>) response.get("data");
            Map<String, Object> attributes = (Map<String, Object>) data.get("attributes");

            String status = (String) attributes.get("status");
            boolean paid = "paid".equals(status);

            log.info("Payment {} status: {}, paid: {}", paymentId, status, paid);

            return new PaymentStatusResponse(
                (String) data.get("id"),
                paid,
                status,
                (String) attributes.get("reference_number")
            );
        } catch (WebClientResponseException e) {
            log.error("Error checking payment status: {} - {}",
                    e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error checking payment status: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getPaymentLinkDetails(String paymentId) {
        try {
            log.info("Getting payment link details for ID: {}", paymentId);

            Map<String, Object> response = webClient.get()
                .uri("/links/" + paymentId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

            if (response == null) {
                throw new RuntimeException("No response from PayMongo API");
            }

            log.debug("PayMongo link details response: {}", response);
            return response;
        } catch (WebClientResponseException e) {
            log.error("Error getting payment link details: {} - {}",
                    e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error getting payment link details: " + e.getMessage(), e);
        }
    }
}
