import com.credigo.backend.service.PaymentService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.credigo.backend.dto.PaymentResponse;
import com.credigo.backend.dto.WalletTopUpRequest;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Value("${paymongo.secret.key}")
    private String secretKey;

    private final WebClient webClient = WebClient.builder()
        .baseUrl("https://api.paymongo.com/v1")
        .defaultHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes()))
        .build();

    @Override
    public PaymentResponse createWalletTopUpPaymentIntent(WalletTopUpRequest topUpRequest, String username) {
        // Build PayMongo request body
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("amount", topUpRequest.getAmount().multiply(BigDecimal.valueOf(100)).longValue()); // centavos
        attributes.put("payment_method_allowed", Arrays.asList("visa", "gcash", "paymaya", "mastercard"));
        attributes.put("currency", "PHP");
        attributes.put("description", "Wallet top-up for " + username);

        Map<String, Object> data = new HashMap<>();
        data.put("attributes", attributes);

        Map<String, Object> body = new HashMap<>();
        body.put("data", data);

        // Call PayMongo API 
        Map response = webClient.post()
            .uri("/payment_intents")
            .bodyValue(body)
            .retrieve()
            .bodyToMono(Map.class)
            .block();

        // Extract client_key or payment_intent_id from response as needed
        String clientKey = (String) ((Map)((Map)response.get("data")).get("attributes")).get("client_key");
        return new PaymentResponse(clientKey);
    }
}