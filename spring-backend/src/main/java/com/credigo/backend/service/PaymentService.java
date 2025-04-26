package com.credigo.backend.service;

import com.credigo.backend.dto.WalletTopUpRequest;
import com.credigo.backend.dto.PaymentResponse;

public interface PaymentService {
  PaymentResponse createWalletTopUpPaymentIntent(WalletTopUpRequest topUpRequest, String username);
}
