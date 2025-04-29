package com.paymongo.services;

import com.paymongo.entities.PaymentMethodEntity;
import com.paymongo.exceptions.StandardException;

public class PaymentMethod extends BaseService {
  public static final String URI = "payment_methods";

  public static PaymentMethodEntity create(Object payload) throws StandardException {
    return (PaymentMethodEntity) request(
      "PaymentMethodEntity",
      "POST",
      URI,
      payload,
      false
    );
  }

  public static PaymentMethodEntity retrieve(String id) throws StandardException {
    return (PaymentMethodEntity) request(
      "PaymentMethodEntity",
      "GET",
      URI + "/" + id,
      null,
      false
    );
  }
}
