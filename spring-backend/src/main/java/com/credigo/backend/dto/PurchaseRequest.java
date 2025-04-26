package com.credigo.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO representing the request from a user to purchase a product (game top-up).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequest {

  @NotNull(message = "Product ID cannot be null")
  private Integer productId; // The ID of the Product (Game Top-up Item) being purchased

  @NotBlank(message = "Game Account ID cannot be blank")
  @Size(max = 100, message = "Game Account ID cannot exceed 100 characters")
  private String gameAccountId; // The target Game User ID provided by the customer

  // Optional: Game Server/Zone ID, make it required if necessary for the game
  @Size(max = 50, message = "Game Server ID cannot exceed 50 characters")
  private String gameServerId;

  // Quantity is usually 1 for top-ups, but include if multiple can be bought at
  // once
  @NotNull(message = "Quantity cannot be null")
  @Min(value = 1, message = "Quantity must be at least 1")
  private Integer quantity = 1; // Default to 1

  // Payment method might be implicitly 'WALLET' in this flow,
  // but could be included if other methods are added later.
  // private String paymentMethod = "WALLET";

}
