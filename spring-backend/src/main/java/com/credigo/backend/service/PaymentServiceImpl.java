package com.credigo.backend.service; // Ensure this file is in src/main/java/com/credigo/backend/service/

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.credigo.backend.dto.PaymentResponse;
import com.credigo.backend.dto.WalletTopUpRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

import reactor.core.publisher.Mono;
import java.util.Base64;

@Service
public class PaymentServiceImpl implements PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final String secretKey;
    private final WebClient webClient;
    private final String successUrl;
    private final String cancelUrl;
    private final String webhookUrl;
    private final String apiVersion;

    public PaymentServiceImpl(
            @Value("${paymongo.secret.key}") String secretKey,
            @Value("${paymongo.base.url}") String baseUrl,
            @Value("${paymongo.success.url}") String successUrl,
            @Value("${paymongo.cancel.url}") String cancelUrl,
            @Value("${paymongo.webhook.url}") String webhookUrl,
            @Value("${paymongo.api.version}") String apiVersion) {

        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new IllegalArgumentException("PayMongo secret key is not configured.");
        }

        this.secretKey = secretKey;
        this.successUrl = successUrl;
        this.cancelUrl = cancelUrl;
        this.webhookUrl = webhookUrl;
        this.apiVersion = apiVersion;

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
        attributes.put("payment_method_allowed", Collections.singletonList("card"));
        attributes.put("currency", "PHP");
        attributes.put("description", "Wallet top-up for " + username);
        attributes.put("statement_descriptor", "CrediGo Top-up");

        Map<String, String> metadata = new HashMap<>();
        metadata.put("credigo_username", username);
        metadata.put("transaction_type", "wallet_topup");
        attributes.put("metadata", metadata);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("data", Map.of("attributes", attributes));

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

        return new PaymentResponse(
            (String) responseAttributes.get("client_key"),
            null,
            (String) responseAttributes.get("status"),
            (String) responseData.get("id"),
            (String) responseAttributes.get("currency"),
            (Long) responseAttributes.get("amount")
        );
    }

    private PaymentResponse createEWalletSource(long amountInCentavos, String username, String type) {
        // Step 1: Create a Source
        Map<String, Object> sourceAttributes = new HashMap<>();
        sourceAttributes.put("amount", amountInCentavos);
        sourceAttributes.put("currency", "PHP");
        sourceAttributes.put("redirect", Map.of(
            "success", successUrl,
            "failed", cancelUrl
        ));
        sourceAttributes.put("type", type);
        sourceAttributes.put("billing", Map.of(
            "name", username,
            "email", username + "@credigo.com"
        ));

        Map<String, Object> sourceRequestBody = new HashMap<>();
        sourceRequestBody.put("data", Map.of("attributes", sourceAttributes));

        Map<String, Object> sourceResponse = webClient.post()
                .uri("/sources")
                .bodyValue(sourceRequestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        if (sourceResponse == null || !(sourceResponse.get("data") instanceof Map)) {
            throw new RuntimeException("Invalid source response from PayMongo.");
        }

        Map<String, Object> sourceData = (Map<String, Object>) sourceResponse.get("data");
        String sourceId = (String) sourceData.get("id");

        // Step 2: Create a Payment using the Source
        Map<String, Object> paymentAttributes = new HashMap<>();
        paymentAttributes.put("amount", amountInCentavos);
        paymentAttributes.put("currency", "PHP");
        paymentAttributes.put("description", "Wallet top-up for " + username);
        paymentAttributes.put("source", Map.of("id", sourceId, "type", "source"));

        Map<String, Object> paymentRequestBody = new HashMap<>();
        paymentRequestBody.put("data", Map.of("attributes", paymentAttributes));

        Map<String, Object> paymentResponse = webClient.post()
                .uri("/payments")
                .bodyValue(paymentRequestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        if (paymentResponse == null || !(paymentResponse.get("data") instanceof Map)) {
            throw new RuntimeException("Invalid payment response from PayMongo.");
        }

        Map<String, Object> paymentData = (Map<String, Object>) paymentResponse.get("data");
        Map<String, Object> paymentResponseAttributes = (Map<String, Object>) paymentData.get("attributes");
        Map<String, Object> redirectAttributes = (Map<String, Object>) ((Map<String, Object>) sourceData.get("attributes")).get("redirect");

        return new PaymentResponse(
            null,
            (String) redirectAttributes.get("checkout_url"),
            (String) paymentResponseAttributes.get("status"),
            (String) paymentData.get("id"),
            (String) paymentResponseAttributes.get("currency"),
            (Long) paymentResponseAttributes.get("amount")
        );
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
}
