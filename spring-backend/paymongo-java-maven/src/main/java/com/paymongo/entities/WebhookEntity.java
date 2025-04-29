package com.paymongo.entities;

import java.util.ArrayList;
import java.util.Map;

public class WebhookEntity {
  public String id;
  public int amount;
  public ArrayList events;
  public Boolean livemode;
  public String secret_key;
  public String status;
  public String url;
  public int created_at;
  public int updated_at;

  public WebhookEntity(Map<String, Object> data) {
    var attributes = (Map<String, Object>) data.get("attributes");

    this.id = (String) data.get("id");
    this.events = (ArrayList) attributes.get("events");
    this.livemode = (Boolean) attributes.get("livemode");
    this.secret_key = (String) attributes.get("secret_key");
    this.status = (String) attributes.get("status");
    this.url = (String) attributes.get("url");
    this.created_at = (int) attributes.get("created_at");
    this.updated_at = (int) attributes.get("updated_at");
  }
}
