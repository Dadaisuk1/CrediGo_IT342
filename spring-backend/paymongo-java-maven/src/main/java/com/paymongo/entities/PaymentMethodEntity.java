package com.paymongo.entities;

import java.util.Map;

public class PaymentMethodEntity {
  public String id;
  public Object billing;
  public Object details;
  public Boolean livemode;
  public Object metadata;
  public String source_id;
  public String type;
  public int created_at;
  public int updated_at;

  public PaymentMethodEntity(Map<String, Object> data) {
    var attributes = (Map<String, Object>) data.get("attributes");

    this.id = (String) data.get("id");
    this.billing = (Object) attributes.get("billing");
    this.details = (Object) attributes.get("details");
    this.livemode = (Boolean) attributes.get("livemode");
    this.metadata = (Object) attributes.get("metadata");
    this.source_id = (String) attributes.get("source_id");
    this.type = (String) attributes.get("type");
    this.created_at = (int) attributes.get("created_at");
    this.updated_at = (int) attributes.get("updated_at");
  }
}
