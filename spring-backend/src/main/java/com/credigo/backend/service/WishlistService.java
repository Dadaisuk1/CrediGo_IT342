package com.credigo.backend.service;

import java.util.List;
import com.credigo.backend.dto.WishlistItemResponse;

public interface WishlistService {
  List<WishlistItemResponse> getWishlist(String username);

  WishlistItemResponse addToWishlist(String username, Integer productId);

  void removeFromWishlist(String username, Integer productId);
}
