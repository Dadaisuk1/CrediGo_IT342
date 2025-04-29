package com.paymongo.entities;

import java.util.Map;

public class LinkEntity {
  public String id;
  public int amount;
  public Boolean archived;
  public String checkout_url;
  public String currency;
  public String description;
  public int fee;
  public Boolean livemode;
  public Object payments;
  public String reference_number;
  public String remarks;
  public String status;
  public Object taxes;
  public int created_at;
  public int updated_at;

  public LinkEntity(Map<String, Object> data) {
    var attributes = (Map<String, Object>) data.get("attributes");

    this.id = (String) data.get("id");
    this.amount = (int) attributes.get("amount");
    this.archived = (Boolean) attributes.get("archived");
    this.checkout_url = (String) attributes.get("checkout_url");
    this.currency = (String) attributes.get("currency");
    this.description = (String) attributes.get("description");
    this.fee = (int) attributes.get("fee");
    this.livemode = (Boolean) attributes.get("livemode");
    this.payments = (Object) attributes.get("payments");
    this.reference_number = (String) attributes.get("reference_number");
    this.remarks = (String) attributes.get("remarks");
    this.status = (String) attributes.get("status");
    this.taxes = (Object) attributes.get("taxes");
    this.created_at = (int) attributes.get("created_at");
    this.updated_at = (int) attributes.get("updated_at");
  }
}
