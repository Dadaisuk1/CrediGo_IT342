package com.credigo.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String clientKey;
    private String checkoutUrl;
    private String status;
    private String paymentIntentId;
    private String currency;
    private Long amount;

    // Constructor for basic response
    public PaymentResponse(String clientKey, String checkoutUrl) {
        this.clientKey = clientKey;
        this.checkoutUrl = checkoutUrl;
    }
}
