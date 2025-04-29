package com.paymongo.services;

import com.paymongo.entities.CustomerEntity;
import com.paymongo.exceptions.StandardException;

public class Customer extends BaseService {
  public static final String URI = "customers";

  public static CustomerEntity create(Object payload) throws StandardException {
    return (CustomerEntity) request(
      "CustomerEntity",
      "POST",
      URI,
      payload
    );
  }

  public static CustomerEntity delete(String id) throws StandardException {
    return (CustomerEntity) request(
      "CustomerEntity",
      "DELETE",
      URI + "/" + id,
      null
    );
  }

  public static CustomerEntity retrieve(String id) throws StandardException {
    return (CustomerEntity) request(
      "CustomerEntity",
      "GET",
      URI + "/" + id,
      null
    );
  }

  public static CustomerEntity update(String id, Object payload) throws StandardException {
    return (CustomerEntity) request(
      "CustomerEntity",
      "PUT",
      URI + "/" + id,
      payload
    );
  }
}
