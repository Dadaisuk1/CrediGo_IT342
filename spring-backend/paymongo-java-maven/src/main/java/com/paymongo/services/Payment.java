package com.paymongo.services;

import com.paymongo.entities.PaymentEntity;
import com.paymongo.exceptions.StandardException;

public class Payment extends BaseService {
  public static final String URI = "payments";

  public static PaymentEntity retrieve(String id) throws StandardException {
    return (PaymentEntity) request(
      "PaymentEntity",
      "GET",
      URI + "/" + id,
      null,
      false
    );
  }
}
