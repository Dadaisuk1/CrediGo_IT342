package com.credigo.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusResponse {
    private String paymentIntentId;
    private String status;
    private boolean walletCredited;
    private String message;
    private Long amount;
    private String currency;

    // Static factory methods for common responses
    public static PaymentStatusResponse pending(String paymentIntentId, Long amount, String currency) {
        return new PaymentStatusResponse(
            paymentIntentId,
            "awaiting_payment_method",
            false,
            "Payment pending completion",
            amount,
            currency
        );
    }

    public static PaymentStatusResponse succeeded(String paymentIntentId, Long amount, String currency, boolean credited) {
        return new PaymentStatusResponse(
            paymentIntentId,
            "succeeded",
            credited,
            credited ? "Payment completed and wallet credited" : "Payment completed but wallet not yet credited",
            amount,
            currency
        );
    }

    public static PaymentStatusResponse failed(String paymentIntentId, String reason) {
        return new PaymentStatusResponse(
            paymentIntentId,
            "failed",
            false,
            "Payment failed: " + reason,
            null,
            null
        );
    }

    public static PaymentStatusResponse notFound(String paymentIntentId) {
        return new PaymentStatusResponse(
            paymentIntentId,
            "not_found",
            false,
            "Payment not found",
            null,
            null
        );
    }
}
