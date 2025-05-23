package com.credigo.backend.controller;


import com.credigo.backend.dto.PaymentResponse;
import com.credigo.backend.dto.PaymentStatusResponse;
import org.springframework.beans.factory.annotation.Value;
import com.credigo.backend.dto.WalletTopUpRequest;
import com.credigo.backend.service.PaymentService;
import com.credigo.backend.service.WalletService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders; // Needed for @RequestHeader

import java.math.BigDecimal;
import java.util.Map;
// Imports needed for webhook security verification
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat; // Requires Java 17+
import java.util.HashMap;
import org.springframework.web.reactive.function.client.WebClient;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    @Value("${paymongo.webhook.secret.key}")
    private String paymongoWebhookSecretKey;

    private final WalletService walletService;
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    @Autowired
    public PaymentController(WalletService walletService, PaymentService paymentService, ObjectMapper objectMapper, WebClient webClient) {
        this.walletService = walletService;
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
        this.webClient = webClient;
        log.info("PayMongo Webhook Secret Key Loaded: {}", paymongoWebhookSecretKey != null && !paymongoWebhookSecretKey.isEmpty() ? "Yes" : "No");
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
        log.info("Received request to create payment intent for user: {}, amount: {}", currentUsername,
                topUpRequest.getAmount());

        try {
            PaymentResponse paymentResponse = paymentService.createWalletTopUpPaymentIntent(topUpRequest,
                    currentUsername);
            log.info("PaymentIntent created successfully for user: {}", currentUsername);
            return ResponseEntity.ok(paymentResponse);
        } catch (Exception e) {
            log.error("Failed to create PaymentIntent for user {}: {}", currentUsername, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create payment intent.");
        }
    }

    @PostMapping("/paymongo/webhook")
    public ResponseEntity<String> handlePayMongoWebhook(
            @RequestBody String payload, // The raw JSON payload
            @RequestHeader(HttpHeaders.USER_AGENT) String userAgent, // Example of getting another header
            @RequestHeader(name = "Paymongo-Signature", required = false) String signatureHeader) { // Get signature
                                                                                                    // header

        log.info("PayMongo webhook endpoint hit.");

        // --- Webhook Signature Verification (Highly Recommended) ---
        // Check if the secret key is configured
        if (paymongoWebhookSecretKey == null || paymongoWebhookSecretKey.isBlank()
                || !paymongoWebhookSecretKey.startsWith("whsk_")) {
            log.error(
                    "PayMongo webhook secret key is not configured correctly in application properties. Cannot verify signature.");
            // Return OK to PayMongo but don't process
            return ResponseEntity.ok("Webhook received but cannot verify (config missing/invalid).");
        }
        // Check if the signature header is present
        if (signatureHeader == null || signatureHeader.isBlank()) {
            log.warn("PayMongo webhook request missing Paymongo-Signature header.");
            // You might choose to reject here, or proceed cautiously if testing without
            // signatures
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing signature.");
        }

        try {
            // Perform the actual signature validation
            if (!isValidSignature(payload, signatureHeader, paymongoWebhookSecretKey)) {
                log.warn("Invalid PayMongo webhook signature received. Header: {}", signatureHeader);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature.");
            }
            log.info("PayMongo webhook signature verified successfully."); // Use INFO level for successful verification

        } catch (Exception e) {
            log.error("Error verifying PayMongo webhook signature: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Signature verification failed.");
        }
        // --- End of Verification ---

        // If signature is valid, proceed to process the payload
        log.debug("Processing verified PayMongo webhook payload (first 200 chars): {}",
                payload != null ? payload.substring(0, Math.min(payload.length(), 200)) : "<empty>");
        try {
            Map<String, Object> webhookEvent = objectMapper.readValue(payload, Map.class);

            Object dataObj = webhookEvent.get("data");
            if (!(dataObj instanceof Map)) {
                log.error("PayMongo webhook: 'data' field missing or not an object in payload.");
                return ResponseEntity.ok("Webhook received but missing/invalid data field.");
            }
            Map<String, Object> data = (Map<String, Object>) dataObj;

            Object typeObj = data.get("type");
            Object attributesObj = data.get("attributes");

            if (!(typeObj instanceof String)) {
                log.error("PayMongo webhook: 'data.type' field missing or not a String.");
                return ResponseEntity.ok("Webhook received but invalid data type field.");
            }
            String eventType = (String) typeObj;

            if (!(attributesObj instanceof Map)) {
                log.error("PayMongo webhook: 'data.attributes' missing or not a Map for event type {}.", eventType);
                return ResponseEntity.ok("Webhook received but missing attributes.");
            }
            Map<String, Object> attributes = (Map<String, Object>) attributesObj;

            // --- Check for the correct event type ---
            // Your webhook is configured for "payment.paid"
            // The previous code checked for "payment_intent.succeeded"
            // Adjust this 'if' condition based on the actual event PayMongo sends for your
            // payment flow.
            log.info("PayMongo webhook received event type: {}", eventType);
            if ("payment.paid".equals(eventType)) { // Adjusted to match your webhook config
                processPaymentPaidEvent(attributes);
                log.info("Processing {} event.", eventType);
            } else if ("payment_intent.succeeded".equals(eventType)) {
                processSuccessfulPaymentIntent(attributes);
                log.info("Processing {} event.", eventType);
            } else if ("source.chargeable".equals(eventType)) {
                processSourceChargeable(attributes);
                log.info("Processing {} event.", eventType);
            } else {
                log.info("Ignoring PayMongo event type: {}", eventType);
            }

        } catch (JsonProcessingException e) {
            log.error("Error parsing PayMongo webhook JSON payload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON payload.");
        } catch (Exception e) {
            log.error("Unexpected error processing PayMongo webhook payload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook processing failed.");
        }

        return ResponseEntity.ok("Webhook received");
    }

    /**
     * Helper method to process the attributes of a successful payment.paid event.
     * Adapt fields based on the actual structure of the 'payment.paid' event
     * attributes.
     *
     * @param attributes The attributes map from the webhook event data.
     */
    private void processPaymentPaidEvent(Map<String, Object> attributes) {
        String paymentId = null; // Example: Assuming 'payment.paid' has a payment ID
        try {
            // --- Extract fields specific to the 'payment.paid' event ---
            // You MUST inspect the actual JSON PayMongo sends for 'payment.paid'
            // to know the correct field names and structure. The following are guesses.
            paymentId = attributes.get("id") instanceof String ? (String) attributes.get("id") : null; // Might be
                                                                                                       // payment ID

            Long amount = null;
            Object amtObj = attributes.get("amount");
            if (amtObj instanceof Number) {
                amount = ((Number) amtObj).longValue();
            } else {
                log.warn(
                        "PayMongo payment.paid event: 'amount' is missing or not a number in attributes for payment {}",
                        paymentId);
            }

            String currency = attributes.get("currency") instanceof String ? (String) attributes.get("currency") : null;
            String description = attributes.get("description") instanceof String
                    ? (String) attributes.get("description")
                    : "Payment via PayMongo"; // Default description

            // --- How to get the username? ---
            // Does 'payment.paid' have metadata? Or do you need to look up the associated
            // source/payment_intent?
            // This part is CRITICAL and depends entirely on the 'payment.paid' event
            // structure.
            // Option 1: Check for metadata directly on the payment (if available)
            String username = null;
            String type = null;
            Map<String, Object> metadata = null;
            if (attributes.get("metadata") instanceof Map) {
                metadata = (Map<String, Object>) attributes.get("metadata");
                username = metadata.get("credigo_username") instanceof String
                        ? (String) metadata.get("credigo_username")
                        : null;
                type = metadata.get("transaction_type") instanceof String ? (String) metadata.get("transaction_type")
                        : null;
                log.info("PayMongo payment.paid event details: paymentId={}, amount={}, currency={}, metadata={}",
                        paymentId, amount, currency, metadata);
            } else {
                // Option 2: Maybe the source ID is here?
                String sourceId = attributes.get("source") instanceof String ? (String) attributes.get("source") : null;
                if (sourceId != null) {
                    log.warn(
                            "PayMongo payment.paid event: No metadata found directly. Need to fetch source/payment_intent {} to find username.",
                            sourceId);
                    // !!! You would need to add logic here to call PayMongo API again using
                    // sourceId
                    // to get the original PaymentIntent/Source which *should* have the metadata.
                    // This makes the webhook handler more complex.
                } else {
                    log.warn(
                            "PayMongo payment.paid event: Cannot determine user. No metadata found directly on payment {} and no source ID provided.",
                            paymentId);
                }
            }

            // Check if it's a valid wallet top-up event (based on metadata if found)
            if ("wallet_topup".equals(type) && username != null && !username.isBlank() && amount != null
                    && amount > 0) {
                BigDecimal amountDecimal = BigDecimal.valueOf(amount).divide(new BigDecimal("100"));
                try {
                    // Use paymentId or another relevant ID for the transaction reference
                    walletService.addFundsToWallet(username, amountDecimal,
                            paymentId != null ? paymentId : "paymongo_payment", description);
                    log.info("Successfully credited wallet for user {} via PayMongo payment {} ({} {})", username,
                            paymentId, amountDecimal, currency);
                } catch (Exception e) {
                    log.error("Failed to credit wallet for user {} from payment {}: {}", username, paymentId,
                            e.getMessage(), e);
                }
            } else {
                log.warn(
                        "Ignoring PayMongo payment.paid for paymentId={}: metadata missing, not a wallet top-up, invalid username, or zero/missing amount. Type='{}', Username='{}', Amount={}",
                        paymentId, type, username, amount);
            }
        } catch (Exception e) {
            log.error("Error processing payment.paid attributes for paymentId {}: {}", paymentId, e.getMessage(), e);
        }
    }

    /**
     * Helper method to process the attributes of a successful payment intent.
     * Kept for reference or if you handle payment_intent events elsewhere.
     *
     * @param attributes The attributes map from the webhook event data.
     */
    private void processSuccessfulPaymentIntent(Map<String, Object> attributes) {
        // (Logic as defined in previous versions - processes payment_intent.succeeded)
        String paymentIntentId = null;
        try {
            paymentIntentId = attributes.get("id") instanceof String ? (String) attributes.get("id") : null;
            Long amount = null;
            Object amtObj = attributes.get("amount");
            if (amtObj instanceof Number) {
                amount = ((Number) amtObj).longValue();
            } else {
                log.warn(
                        "PayMongo payment_intent.succeeded event: 'amount' is missing or not a number in attributes for intent {}",
                        paymentIntentId);
            }
            String currency = attributes.get("currency") instanceof String ? (String) attributes.get("currency") : null;
            String description = attributes.get("description") instanceof String
                    ? (String) attributes.get("description")
                    : "Wallet top-up via PayMongo";
            String username = null;
            String type = null;
            Map<String, Object> metadata = null;
            if (attributes.get("metadata") instanceof Map) {
                metadata = (Map<String, Object>) attributes.get("metadata");
                username = metadata.get("credigo_username") instanceof String
                        ? (String) metadata.get("credigo_username")
                        : null;
                type = metadata.get("transaction_type") instanceof String ? (String) metadata.get("transaction_type")
                        : null;
                log.info(
                        "PayMongo payment_intent.succeeded event details: intentId={}, amount={}, currency={}, metadata={}",
                        paymentIntentId, amount, currency, metadata);
            } else {
                log.warn("PayMongo payment_intent.succeeded event: 'metadata' missing or not a map for intentId={}",
                        paymentIntentId);
            }

            if ("wallet_topup".equals(type) && username != null && !username.isBlank() && amount != null
                    && amount > 0) {
                BigDecimal amountDecimal = BigDecimal.valueOf(amount).divide(new BigDecimal("100"));
                try {
                    walletService.addFundsToWallet(username, amountDecimal, paymentIntentId, description);
                    log.info("Successfully credited wallet for user {} via PayMongo paymentIntent {} ({} {})", username,
                            paymentIntentId, amountDecimal, currency);
                } catch (Exception e) {
                    log.error("Failed to credit wallet for user {} from paymentIntent {}: {}", username,
                            paymentIntentId, e.getMessage(), e);
                }
            } else {
                log.warn(
                        "Ignoring PayMongo payment_intent.succeeded for intentId={}: metadata missing, not a wallet top-up, invalid username, or zero/missing amount. Type='{}', Username='{}', Amount={}",
                        paymentIntentId, type, username, amount);
            }
        } catch (Exception e) {
            log.error("Error processing payment_intent.succeeded attributes for intentId {}: {}", paymentIntentId,
                    e.getMessage(), e);
        }
    }

    /**
     * Helper method to process source.chargeable event from PayMongo webhook.
     * Creates a Payment using the chargeable source for GCash and PayMaya.
     *
     * @param attributes The attributes map from the webhook event data.
     */
    private void processSourceChargeable(Map<String, Object> attributes) {
        String eventType = attributes.get("type") instanceof String ? (String) attributes.get("type") : null;

        if (!"source.chargeable".equals(eventType)) {
            log.warn("Ignoring non-source.chargeable event: {}", eventType);
            return;
        }

        try {
            Map<String, Object> eventData = attributes.get("data") instanceof Map
                ? (Map<String, Object>) attributes.get("data")
                : null;

            if (eventData == null) {
                log.error("Missing data in source.chargeable event");
                return;
            }

            String sourceId = eventData.get("id") instanceof String ? (String) eventData.get("id") : null;
            if (sourceId == null) {
                log.error("Missing source ID in source.chargeable event");
                return;
            }

            Map<String, Object> sourceAttributes = eventData.get("attributes") instanceof Map
                ? (Map<String, Object>) eventData.get("attributes")
                : null;

            if (sourceAttributes == null) {
                log.error("Missing attributes in source.chargeable event data");
                return;
            }

            String sourceType = sourceAttributes.get("type") instanceof String ? (String) sourceAttributes.get("type") : null;
            if (!"gcash".equals(sourceType) && !"paymaya".equals(sourceType) && !"grab_pay".equals(sourceType)) {
                log.warn("Unexpected source type in source.chargeable event: {}", sourceType);
                return;
            }

            Long amount = null;
            Object amountObj = sourceAttributes.get("amount");
            if (amountObj instanceof Number) {
                amount = ((Number) amountObj).longValue();
            } else {
                log.error("Missing or invalid amount in source.chargeable event");
                return;
            }

            String currency = sourceAttributes.get("currency") instanceof String ? (String) sourceAttributes.get("currency") : "PHP";

            Map<String, Object> metadata = sourceAttributes.get("metadata") instanceof Map
                ? (Map<String, Object>) sourceAttributes.get("metadata")
                : new HashMap<>();

            String username = metadata.get("credigo_username") instanceof String
                ? (String) metadata.get("credigo_username")
                : null;

            // Prepare payment request
            Map<String, Object> paymentAttributes = new HashMap<>();
            paymentAttributes.put("amount", amount);
            paymentAttributes.put("currency", currency);
            paymentAttributes.put("description", "Wallet top-up via " + sourceType);
            paymentAttributes.put("source", Map.of("id", sourceId, "type", "source"));

            Map<String, Object> paymentRequestBody = new HashMap<>();
            paymentRequestBody.put("data", Map.of("attributes", paymentAttributes));

            log.info("Creating payment for chargeable source: {} (type: {}, amount: {})",
                sourceId, sourceType, amount);

            // Create payment
            Map<String, Object> paymentResponse = webClient.post()
                    .uri("/payments")
                    .bodyValue(paymentRequestBody)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (paymentResponse == null || !(paymentResponse.get("data") instanceof Map)) {
                log.error("Invalid payment response from PayMongo after source.chargeable");
                return;
            }

            Map<String, Object> paymentData = (Map<String, Object>) paymentResponse.get("data");
            String paymentId = paymentData.get("id") instanceof String ? (String) paymentData.get("id") : null;

            if (paymentId != null && username != null) {
                log.info("Successfully created payment {} for source {} (user: {})",
                    paymentId, sourceId, username);

                // Credit wallet if username is available
                try {
                    BigDecimal amountDecimal = BigDecimal.valueOf(amount).divide(new BigDecimal("100"));
                    walletService.addFundsToWallet(username, amountDecimal, paymentId,
                        "Wallet top-up via " + sourceType);
                    log.info("Successfully credited wallet for user {} via {} (payment: {})",
                        username, sourceType, paymentId);
                } catch (Exception e) {
                    log.error("Failed to credit wallet for user {} from payment {}: {}",
                        username, paymentId, e.getMessage(), e);
                }
            } else {
                log.warn("Created payment for source {} but unable to credit wallet (paymentId: {}, username: {})",
                    sourceId, paymentId, username);
            }
        } catch (Exception e) {
            log.error("Error processing source.chargeable event: {}", e.getMessage(), e);
        }
    }

    /**
     * Verifies the PayMongo webhook signature.
     *
     * @param payload         The raw request body payload.
     * @param signatureHeader The value of the "Paymongo-Signature" header.
     * @param secret          The webhook secret key.
     * @return true if the signature is valid, false otherwise.
     * @throws NoSuchAlgorithmException If HmacSHA256 is not available.
     * @throws InvalidKeyException      If the secret key is invalid.
     */
    private boolean isValidSignature(String payload, String signatureHeader, String secret)
            throws NoSuchAlgorithmException, InvalidKeyException {
        String timestamp = null;
        String testSignature = null;
        String liveSignature = null;

        // 1. Split the header to get t, te, li
        String[] parts = signatureHeader.split(",");
        for (String part : parts) {
            String[] kv = part.trim().split("=", 2); // Trim whitespace
            if (kv.length == 2) {
                String key = kv[0];
                String value = kv[1];
                switch (key) {
                    case "t":
                        timestamp = value;
                        break;
                    case "te":
                        testSignature = value;
                        break;
                    case "li":
                        liveSignature = value;
                        break;
                }
            }
        }

        if (timestamp == null || (testSignature == null && liveSignature == null)) {
            log.warn("Webhook signature header is missing required components (t, te, or li). Header: {}",
                    signatureHeader);
            return false; // Cannot verify if components are missing
        }

        // 2. Concatenate timestamp, '.', and payload
        String signedPayload = timestamp + "." + payload;

        // 3. Calculate HMAC-SHA256
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key_spec = new SecretKeySpec(secret.getBytes(), "HmacSHA256"); // Use UTF-8 by default
        sha256_HMAC.init(secret_key_spec);
        byte[] calculatedHashBytes = sha256_HMAC.doFinal(signedPayload.getBytes()); // Use UTF-8 by default

        // Convert calculated hash to hex string (requires Java 17+)
        String calculatedHashHex = HexFormat.of().formatHex(calculatedHashBytes);

        // 4. Compare with the appropriate signature (te for test, li for live)
        // Determine which signature to use. Check if the secret key indicates test
        // mode,
        // or rely on the presence of te/li. Checking the key prefix is safer.
        String signatureToCompare = null;
        boolean isTestMode = secret.startsWith("whsk_test_"); // Check if using a test secret key

        if (isTestMode && testSignature != null) {
            signatureToCompare = testSignature;
            log.debug("Comparing calculated hash with TEST signature (te)");
        } else if (!isTestMode && liveSignature != null) {
            signatureToCompare = liveSignature;
            log.debug("Comparing calculated hash with LIVE signature (li)");
        } else if (testSignature != null) {
            // Fallback if key type is unknown but te is present
            signatureToCompare = testSignature;
            log.warn(
                    "Webhook secret key doesn't start with 'whsk_test_' but 'te' signature is present. Comparing with 'te'.");
        } else if (liveSignature != null) {
            // Fallback if key type is unknown but li is present
            signatureToCompare = liveSignature;
            log.warn(
                    "Webhook secret key starts with 'whsk_test_' but only 'li' signature is present. Comparing with 'li'.");
        }

        if (signatureToCompare == null) {
            log.warn(
                    "Could not determine appropriate signature (te/li) to compare against based on key type and header content.");
            return false;
        }

        // Perform a constant-time comparison if possible, although standard
        // equalsIgnoreCase is often sufficient here.
        boolean isValid = calculatedHashHex.equalsIgnoreCase(signatureToCompare);

        if (!isValid) {
            log.warn("Webhook signature mismatch! Provided: {}, Calculated: {}", signatureToCompare, calculatedHashHex);
        }
        return isValid;
    }

    @PostMapping("/create-payment-link/{orderId}")
    public ResponseEntity<?> createPaymentLink(@PathVariable String orderId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
            }
            String username = authentication.getName();

            PaymentResponse response = paymentService.createPaymentLink(orderId, username);
            return ResponseEntity.ok(Map.of("checkoutUrl", response.getCheckoutUrl()));
        } catch (Exception e) {
            log.error("Error creating payment link for orderId {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create payment link: " + e.getMessage());
        }
    }

    @GetMapping("/verify/{orderId}")
    public ResponseEntity<?> verifyPayment(@PathVariable String orderId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
            }
            String username = authentication.getName();

            boolean isVerified = paymentService.verifyPayment(orderId, username);
            return ResponseEntity.ok(Map.of("verified", isVerified));
        } catch (Exception e) {
            log.error("Error verifying payment for orderId {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to verify payment: " + e.getMessage());
        }
    }

    // For testing only - NOT FOR PRODUCTION
    @PostMapping("/test-confirm-payment")
    public ResponseEntity<?> testConfirmPayment(@RequestBody Map<String, Object> requestBody) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }
        String adminUsername = authentication.getName();

        String paymentIntentId = (String) requestBody.get("paymentIntentId");
        if (paymentIntentId == null) {
            return ResponseEntity.badRequest().body("Payment intent ID is required.");
        }

        // Get the target username (the user who should receive the funds)
        String targetUsername = (String) requestBody.get("targetUsername");
        // If targetUsername is not provided, default to the admin's username
        if (targetUsername == null || targetUsername.trim().isEmpty()) {
            targetUsername = adminUsername;
        }

        String amountStr = requestBody.get("amount") != null ? requestBody.get("amount").toString() : null;
        if (amountStr == null) {
            return ResponseEntity.badRequest().body("Amount is required.");
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid amount format.");
        }

        log.info("Test endpoint: Manually crediting user {} wallet with {} PHP, paymentIntentId: {}",
                targetUsername, amount, paymentIntentId);

        try {
            // Generate a unique payment ID for each test transaction to avoid duplicate checks
            // Combine the original ID with a timestamp to make it unique
            String uniqueTestPaymentId = paymentIntentId + "_test_" + System.currentTimeMillis();

            // Add funds to the targetUsername's wallet, not the admin's
            walletService.addFundsToWallet(targetUsername, amount, uniqueTestPaymentId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payment confirmed and " + amount + " PHP added to " + targetUsername + "'s wallet.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error confirming payment: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not confirm payment: " + e.getMessage());
        }
    }

    @GetMapping("/status/{paymentIntentId}")
    public ResponseEntity<?> checkPaymentStatus(@PathVariable String paymentIntentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }
        String username = authentication.getName();

        try {
            PaymentStatusResponse statusResponse = paymentService.checkPaymentStatus(paymentIntentId, username);

            if ("not_found".equals(statusResponse.getStatus())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(statusResponse);
            }

            return ResponseEntity.ok(statusResponse);
        } catch (Exception e) {
            log.error("Error checking payment status for {}: {}", paymentIntentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "error",
                    "message", "Failed to check payment status: " + e.getMessage()
                ));
        }
    }

    // Add a helper method to get the base URL for redirects
    private String getRedirectBaseUrl(HttpServletRequest request) {
        // Check if the request contains our custom header
        String frontendUrl = request.getHeader("X-Frontend-Url");

        if (frontendUrl != null && !frontendUrl.isEmpty()) {
            log.info("Using frontend URL from header: {}", frontendUrl);
            return frontendUrl;
        }

        // Default fallback cases
        String serverName = request.getServerName();
        if (serverName.equals("localhost") || serverName.equals("127.0.0.1")) {
            return "http://localhost:5173";
        } else {
            return "https://credi-go-it-342.vercel.app";
        }
    }

    @PostMapping("/wallet/topup")
    public ResponseEntity<?> createWalletTopUp(
            @RequestBody WalletTopUpRequest topUpRequest,
            Authentication authentication,
            HttpServletRequest request) {

        String username = authentication.getName();
        log.info("Creating wallet top up for user: {}, amount: {}, type: {}",
                username, topUpRequest.getAmount(), topUpRequest.getPaymentType());

        try {
            // Set redirect URLs based on the request origin
            String baseRedirectUrl = getRedirectBaseUrl(request);
            topUpRequest.setSuccessRedirectUrl(baseRedirectUrl + "/payment/success");
            topUpRequest.setCancelRedirectUrl(baseRedirectUrl + "/payment/cancel");

            log.info("Setting redirect URLs - success: {}, cancel: {}",
                    topUpRequest.getSuccessRedirectUrl(), topUpRequest.getCancelRedirectUrl());

            PaymentResponse response = paymentService.createWalletTopUpPaymentIntent(topUpRequest, username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating wallet top-up: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
