package com.credigo.backend.service;

import com.credigo.backend.dto.WalletTopUpRequest;
import com.credigo.backend.dto.PaymentResponse;
import com.credigo.backend.dto.PaymentStatusResponse;

public interface PaymentService {
  PaymentResponse createWalletTopUpPaymentIntent(WalletTopUpRequest topUpRequest, String username);
  PaymentResponse createPaymentLink(String orderId, String username);
  boolean verifyPayment(String orderId, String username);
  PaymentStatusResponse checkPaymentStatus(String paymentIntentId, String username);
}
