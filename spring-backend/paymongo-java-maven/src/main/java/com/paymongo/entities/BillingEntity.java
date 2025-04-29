package com.paymongo.entities;

import java.util.Map;

public class BillingEntity {
  public BillingAddressEntity address;
  public String email;
  public String name;
  public String phone;

  public BillingEntity(Map<String, Object> data) {
    this.address = new BillingAddressEntity((Map<String, String>) data.get("address"));
    this.email = (String) data.get("email");
    this.name = (String) data.get("name");
    this.phone = (String) data.get("phone");
  }
}
