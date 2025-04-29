package com.paymongo.entities;

import java.util.Map;

public class CustomerEntity {
  public String id;
  public String default_device;
  public String default_payment_method_id;
  public Boolean deleted;
  public String email;
  public String first_name;
  public String last_name;
  public Boolean livemode;
  public String phone;
  public String type;
  public int created_at;
  public int updated_at;

  public CustomerEntity(Map<String, Object> data){
    var attributes = (Map<String, Object>) data.get("attributes");

    this.id = (String) data.get("id");
    this.default_device = (String) attributes.get("default_device");
    this.default_payment_method_id = (String) attributes.get("default_payment_method_id");
    this.deleted = (Boolean) attributes.get("deleted");
    this.email = (String) attributes.get("email");
    this.first_name = (String) attributes.get("first_name");
    this.last_name = (String) attributes.get("last_name");
    this.livemode = (Boolean) attributes.get("livemode");
    this.phone = (String) attributes.get("phone");
    this.type = (String) attributes.get("type");
    this.created_at = (attributes.get("created_at") == null) ? 0: (int) attributes.get("created_at");
    this.updated_at = (attributes.get("updated_at") == null) ? 0 : (int) attributes.get("updated_at");
  }
}

