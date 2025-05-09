package com.credigo.backend.service;

import com.credigo.backend.dto.WalletTopUpRequest;
import com.credigo.backend.dto.PaymentResponse;
import com.credigo.backend.dto.PaymentStatusResponse;

import java.util.Map;

/**
 * Service interface for processing payments.
 */
public interface PaymentService {

  /**
   * Creates a wallet top-up payment intent.
   *
   * @param topUpRequest The wallet top-up request
   * @param username     The username requesting the top-up
   * @return PaymentResponse containing payment details
   */
  PaymentResponse createWalletTopUpPaymentIntent(WalletTopUpRequest topUpRequest, String username);

  /**
   * Creates a payment link for a product order.
   *
   * @param orderId  The order ID
   * @param username The username making the payment
   * @return PaymentResponse containing payment details
   */
  PaymentResponse createPaymentLink(String orderId, String username);

  /**
   * Verifies a payment.
   *
   * @param orderId  The order ID
   * @param username The username making the payment
   * @return boolean indicating if the payment is verified
   */
  boolean verifyPayment(String orderId, String username);

  /**
   * Checks the status of a payment.
   *
   * @param paymentId The payment ID to check
   * @param username  The username associated with the payment
   * @return PaymentStatusResponse containing the status information
   */
  PaymentStatusResponse checkPaymentStatus(String paymentId, String username);

  /**
   * Gets detailed information about a payment link from PayMongo.
   *
   * @param paymentId The payment link ID
   * @return Map containing the response from PayMongo API
   */
  Map<String, Object> getPaymentLinkDetails(String paymentId);
}
