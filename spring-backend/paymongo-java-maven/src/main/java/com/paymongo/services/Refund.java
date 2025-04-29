package com.paymongo.services;

import com.paymongo.entities.RefundEntity;
import com.paymongo.exceptions.StandardException;

public class Refund extends BaseService {
  public static final String URI = "refunds";

  public static RefundEntity create(Object payload) throws StandardException {
    return (RefundEntity) request(
      "RefundEntity",
      "POST",
      URI,
      payload,
      false
    );
  }

  public static RefundEntity retrieve(String id) throws StandardException {
    return (RefundEntity) request(
      "RefundEntity",
      "GET",
      URI + "/" + id,
      null,
      false
    );
  }
}
