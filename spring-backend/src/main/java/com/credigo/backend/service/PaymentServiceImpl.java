package com.credigo.backend.service; // Ensure this file is in src/main/java/com/credigo/backend/service/

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.credigo.backend.dto.PaymentResponse;
import com.credigo.backend.dto.WalletTopUpRequest;
import org.springframework.core.ParameterizedTypeReference;

import java.math.BigDecimal;
import java.util.*;

import reactor.core.publisher.Mono;
import java.util.Base64;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final String secretKey;
    private final WebClient webClient;

    public PaymentServiceImpl(@Value("${paymongo.secret.key}") String secretKey) {
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new IllegalArgumentException("PayMongo secret key is not configured.");
        }
        this.secretKey = secretKey;
        this.webClient = WebClient.builder()
                .baseUrl("https://api.paymongo.com/v1")
                .defaultHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString((this.secretKey + ":").getBytes()))
                .build();
    }

    @Override
    public PaymentResponse createWalletTopUpPaymentIntent(WalletTopUpRequest topUpRequest, String username) {
        long amountInCentavos = topUpRequest.getAmount().multiply(BigDecimal.valueOf(100)).longValue();

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("amount", amountInCentavos);
        attributes.put("payment_method_allowed", Arrays.asList("card", "gcash", "paymaya"));
        attributes.put("currency", "PHP");
        attributes.put("description", "Wallet top-up for " + username);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("credigo_username", username);
        metadata.put("transaction_type", "wallet_topup");
        attributes.put("metadata", metadata);

        Map<String, Object> data = new HashMap<>();
        data.put("attributes", attributes);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("data", data);

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

            String clientKey = (String) responseAttributes.get("client_key");

            return new PaymentResponse(clientKey);

        } catch (Exception e) {
            throw new RuntimeException("Could not create payment intent due to an internal error.", e);
        }
    }
}