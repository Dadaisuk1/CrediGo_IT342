package com.credigo.backend.service; // Ensure this file is in src/main/java/com/credigo/backend/service/

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.credigo.backend.dto.PaymentResponse;
import com.credigo.backend.dto.WalletTopUpRequest;
import reactor.core.publisher.Mono;

// Consider adding DTOs for PayMongo request/response instead of raw Maps

// import com.credigo.backend.dto.paymongo.PayMongoRequestDto;
// import com.credigo.backend.dto.paymongo.PayMongoResponseDto;
// import org.springframework.http.HttpHeaders; // Uncomment if using commented headers
// import org.springframework.http.MediaType; // Uncomment if using commented headers
// import org.springframework.http.client.reactive.ReactorClientHttpConnector; // Uncomment if using commented clientConnector
// import reactor.netty.http.client.HttpClient; // Uncomment if using commented clientConnector
// import java.time.Duration; // Uncomment if using commented clientConnector

import java.util.HashMap;
import java.util.Map;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {

    // Use final fields for injected dependencies
    private final String secretKey;
    private final WebClient webClient;

    /**
     * Constructor injection is preferred for mandatory dependencies.
     * Spring injects the value of 'paymongo.secret.key' from application properties
     * and initializes the WebClient *after* the secretKey is available.
     *
     * @param secretKey The PayMongo secret key injected from properties.
     */
    public PaymentServiceImpl(@Value("${paymongo.secret.key}") String secretKey) {
        // Validate injected value (optional but recommended)
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new IllegalArgumentException("PayMongo secret key ('paymongo.secret.key') is not configured.");
        }
        this.secretKey = secretKey;

        // Initialize WebClient here, ensuring secretKey is not null
        this.webClient = WebClient.builder()
                .baseUrl("https://api.paymongo.com/v1")
                // Correctly encode the secret key for Basic Authentication
                .defaultHeader("Authorization",
                        "Basic " + Base64.getEncoder().encodeToString((this.secretKey + ":").getBytes()))
                // You might want to add other default headers like Content-Type if needed
                // .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                // Consider adding timeouts
                // .clientConnector(new
                // ReactorClientHttpConnector(HttpClient.create().responseTimeout(Duration.ofSeconds(10))))
                .build();
    }

    @Override
    public PaymentResponse createWalletTopUpPaymentIntent(WalletTopUpRequest topUpRequest, String username) {
        // --- Build PayMongo Request Body ---

        // Attributes Map
        Map<String, Object> attributes = new HashMap<>();
        // Convert amount from BigDecimal (e.g., 10.50 PHP) to long cents (1050)
        long amountInCentavos = topUpRequest.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
        attributes.put("amount", amountInCentavos);
        attributes.put("payment_method_allowed", List.of("card", "gcash", "paymaya")); // Use PayMongo's standard
                                                                                       // identifiers.
        attributes.put("currency", "PHP");
        attributes.put("description", "Wallet top-up for " + username);

        // --- IMPORTANT: Add metadata required by the webhook handler ---
        Map<String, String> metadata = new HashMap<>();
        metadata.put("credigo_username", username); // Pass the username
        metadata.put("transaction_type", "wallet_topup"); // Identify the transaction type
        attributes.put("metadata", metadata);
        // --- End of Metadata ---

        // Add other required or optional attributes as per PayMongo documentation
        // attributes.put("statement_descriptor", "Credigo TopUp");

        // Data Map (wrapping attributes)
        Map<String, Object> data = new HashMap<>();
        data.put("attributes", attributes);

        // Top-level Request Body Map (wrapping data)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("data", data);

        // --- Call PayMongo API ---
        try {
            // Make the API call. .block() makes it synchronous.
            Map<String, Object> response = webClient.post()
                    .uri("/payment_intents") // Endpoint for creating PaymentIntents
                    .bodyValue(requestBody) // Send the structured request body
                    .retrieve() // Retrieve the response
                    // Handle API errors (e.g., 4xx, 5xx)
                    .onStatus(status -> status.isError(), clientResponse -> clientResponse.bodyToMono(String.class) // Or
                                                                                                                    // a
                                                                                                                    // specific
                                                                                                                    // error
                                                                                                                    // DTO
                            .flatMap(errorBody -> {
                                System.err.println(
                                        "PayMongo API Error: " + clientResponse.statusCode() + " - " + errorBody);
                                // Throw a custom exception
                                return Mono.error(new RuntimeException(
                                        "PayMongo API error: " + clientResponse.statusCode() + " Body: " + errorBody));
                            }))
                    // Define how to convert the successful response body
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .block(); // WARNING: .block() makes the call synchronous. Avoid in reactive controllers.

            // --- Process the Response ---
            // Added more robust null checks
            if (response == null || !(response.get("data") instanceof Map)) {
                System.err.println("Invalid response structure from PayMongo: 'data' key missing or not a Map.");
                throw new RuntimeException("Failed to process PayMongo response: Invalid 'data' structure.");
            }

            Map<String, Object> responseData = (Map<String, Object>) response.get("data");
            if (!(responseData.get("attributes") instanceof Map)) {
                System.err.println(
                        "Invalid response structure from PayMongo: 'data.attributes' key missing or not a Map.");
                throw new RuntimeException("Failed to process PayMongo response: Invalid 'data.attributes' structure.");
            }

            Map<String, Object> responseAttributes = (Map<String, Object>) responseData.get("attributes");
            if (!(responseAttributes.get("client_key") instanceof String)) {
                System.err.println(
                        "Invalid response structure from PayMongo: 'data.attributes.client_key' key missing or not a String.");
                throw new RuntimeException("Failed to process PayMongo response: Missing or invalid 'client_key'.");
            }

            String clientKey = (String) responseAttributes.get("client_key");

            // Return the relevant information (e.g., the client key)
            return new PaymentResponse(clientKey);

        } catch (Exception e) {
            // Log the exception details more informatively
            System.err.println("Error creating PayMongo Payment Intent for user " + username + ": " + e.getMessage());
            // It's often better to wrap the original exception
            throw new RuntimeException("Could not create payment intent due to an internal error.", e);
        }
    }
}
