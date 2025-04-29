package com.paymongo.entities;

import java.util.Map;

public class BillingAddressEntity {
  public String city;
  public String country;
  public String line1;
  public String line2;
  public String postal_code;
  public String state;

  public BillingAddressEntity(Map<String, String> data) {
    this.city = data.get("city");
    this.country = data.get("country");
    this.line1 = data.get("line1");
    this.line2 = data.get("line2");
    this.postal_code = data.get("postal_code");
    this.state = data.get("state");
  }
}
