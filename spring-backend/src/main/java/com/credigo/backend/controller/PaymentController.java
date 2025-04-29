package com.credigo.backend.controller;

import com.credigo.backend.dto.PaymentResponse;
import com.credigo.backend.dto.WalletTopUpRequest;
import com.credigo.backend.service.PaymentService;
import com.credigo.backend.service.WalletService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    @Value("${paymongo.secret.key}")
    private String paymongoSecretKey;

    private final WalletService walletService;
    private final PaymentService paymentService;

    @Autowired
    public PaymentController(WalletService walletService, PaymentService paymentService) {
        this.walletService = walletService;
        this.paymentService = paymentService;
    }

    @PostMapping("/create-payment-intent")
    public ResponseEntity<?> createWalletTopUpIntent(@Valid @RequestBody WalletTopUpRequest topUpRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("Attempt to create payment intent by unauthenticated user.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }
        String currentUsername = authentication.getName();
        log.info("Received request to create payment intent for user: {}, amount: {}", currentUsername, topUpRequest.getAmount());

        try {
            PaymentResponse paymentResponse = paymentService.createWalletTopUpPaymentIntent(topUpRequest, currentUsername);
            log.info("PaymentIntent created successfully for user: {}", currentUsername);
            return ResponseEntity.ok(paymentResponse);
        } catch (RuntimeException e) {
            log.error("Failed to create PaymentIntent for user {}: {}", currentUsername, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/paymongo/webhook")
    public ResponseEntity<String> handlePayMongoWebhook(@RequestBody String payload) {
        log.info("PayMongo webhook endpoint hit. Payload (first 200 chars): {}", payload.substring(0, Math.min(payload.length(), 200)));

        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> webhookEvent = objectMapper.readValue(payload, Map.class);

            Map<String, Object> data = (Map<String, Object>) webhookEvent.get("data");
            if (data == null) {
                log.error("PayMongo webhook: 'data' field missing in payload.");
                return ResponseEntity.ok("Webhook received but missing data field.");
            }

            String eventType = (String) data.get("type");
            Map<String, Object> attributes = (Map<String, Object>) data.get("attributes");
            if (attributes == null) {
                log.error("PayMongo webhook: 'attributes' missing in event data.");
                return ResponseEntity.ok("Webhook received but missing attributes.");
            }

            if ("payment_intent.succeeded".equals(eventType)) {
                handleSuccessfulPayMongoIntent(attributes);
            } else {
                log.warn("Unhandled PayMongo event type: {}", eventType);
            }

        } catch (Exception e) {
            log.error("Error processing PayMongo webhook: {}", e.getMessage(), e);
        }

        return ResponseEntity.ok("Webhook received");
    }

    private void handleSuccessfulPayMongoIntent(Map<String, Object> attributes) {
        if (attributes == null) {
            log.error("PayMongo webhook: attributes missing in event data.");
            return;
        }
        try {
            String paymentIntentId = attributes.get("id") != null ? attributes.get("id").toString() : null;
            Long amount = attributes.get("amount") instanceof Integer ? ((Integer) attributes.get("amount")).longValue() : (Long) attributes.get("amount");
            String currency = (String) attributes.get("currency");
            String description = attributes.get("description") != null ? (String) attributes.get("description") : "Wallet top-up via PayMongo";
            Map<String, Object> metadata = attributes.get("metadata") instanceof Map ? (Map<String, Object>) attributes.get("metadata") : null;
            String username = metadata != null ? (String) metadata.get("credigo_username") : null;
            String type = metadata != null ? (String) metadata.get("transaction_type") : null;

            log.info("PayMongo webhook: payment_intent.succeeded for user={}, paymentIntentId={}, amount={}, currency={}", username, paymentIntentId, amount, currency);

            if ("wallet_topup".equals(type) && username != null && !username.isBlank()) {
                BigDecimal amountDecimal = BigDecimal.valueOf(amount).divide(new BigDecimal("100"));
                try {
                    walletService.addFundsToWallet(username, amountDecimal, paymentIntentId, description);
                    log.info("Successfully credited wallet for user {} via PayMongo paymentIntent {}", username, paymentIntentId);
                } catch (Exception e) {
                    log.error("Failed to credit wallet for user {}: {}", username, e.getMessage(), e);
                }
            } else {
                log.warn("Ignoring PayMongo payment_intent.succeeded: metadata missing or not a wallet top-up. Metadata: {}", metadata);
            }
        } catch (Exception e) {
            log.error("Error processing PayMongo payment_intent.succeeded: {}", e.getMessage(), e);
        }
    }
}
