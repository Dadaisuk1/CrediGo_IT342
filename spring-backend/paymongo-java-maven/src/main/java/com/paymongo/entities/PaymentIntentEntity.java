package com.paymongo.entities;

import java.util.Map;

public class PaymentIntentEntity {
  public String id;
  public int amount;
  public String capture_type;
  public String client_key;
  public String currency;
  public String description;
  public String last_payment_error;
  public Boolean livemode;
  public Object metadata;
  public Object next_action;
  public Object payment_method;
  public Object payment_method_allowed;
  public Object payment_method_options;
  public Object payments;
  public String setup_future_usage;
  public String statement_descriptor;
  public String status;
  public int created_at;
  public int updated_at;

  public PaymentIntentEntity(Map<String, Object> data) {
    var attributes = (Map<String, Object>) data.get("attributes");

    this.id = (String) data.get("id");
    this.amount = (int) attributes.get("amount");
    this.capture_type = (String) attributes.get("capture_type");
    this.client_key = (String) attributes.get("client_key");
    this.currency = (String) attributes.get("currency");
    this.description = (String) attributes.get("description");
    this.last_payment_error = (String) attributes.get("last_payment_error");
    this.livemode = (Boolean) attributes.get("livemode");
    this.metadata = attributes.get("metadata");
    this.next_action = attributes.get("next_action");
    this.payment_method = (Object) attributes.get("payment_method");
    this.payment_method_allowed = (Object) attributes.get("payment_method_allowed");
    this.payment_method_options = attributes.get("payment_method_options");
    this.payments = (Object) attributes.get("payments");
    this.setup_future_usage = (String) attributes.get("setup_future_usage");
    this.statement_descriptor = (String) attributes.get("statement_descriptor");
    this.status = (String) attributes.get("status");
    this.created_at = (int) attributes.get("created_at");
    this.updated_at = (int) attributes.get("updated_at");
  }
}
