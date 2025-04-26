package com.credigo.backend.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItemResponse {
  private Integer productId;
  private String productName;
  private BigDecimal productPrice;
  private String productDescription;
  private String productImageUrl;
  private boolean productIsAvailable;

  private Integer platformId;
  private String platformName;

  private LocalDateTime addedAt;

  public BigDecimal getProductPrice() {
    return productPrice;
  }

  public void setProductPrice(BigDecimal productPrice) {
    this.productPrice = productPrice;
  }
}
