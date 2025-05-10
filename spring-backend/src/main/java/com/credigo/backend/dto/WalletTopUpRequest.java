package com.credigo.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO representing the request from a user to add funds to their wallet.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WalletTopUpRequest {

  @NotNull(message = "Amount is required")
  @DecimalMin(value = "50.00", message = "Minimum top-up amount is ₱50.00")
  @Digits(integer = 6, fraction = 2, message = "Amount format is invalid") // Example: max 999999.99
  private BigDecimal amount;

  @NotNull(message = "Payment type is required")
  @Pattern(regexp = "^(card|gcash|paymaya)$", message = "Payment type must be either 'card', 'gcash', or 'paymaya'")
  private String paymentType;

  // Currency is often determined server-side (e.g., always PHP for this app),
  // but could be included if multiple currencies were supported.
  // private String currency = "PHP";

  private Map<String, Object> billing;
  private String mobileNumber;
  private String successRedirectUrl;
  private String cancelRedirectUrl;

}
