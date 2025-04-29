package com.paymongo.services;

import com.paymongo.entities.ListingEntity;
import com.paymongo.entities.WebhookEntity;
import com.paymongo.exceptions.StandardException;

public class Webhook extends BaseService {
  public static final String URI = "webhooks";

  public static ListingEntity all() throws StandardException {
    return (ListingEntity) request(
      "WebhookEntity",
      "GET",
      URI,
      null,
      true
    );
  }

  public static WebhookEntity create(Object payload) throws StandardException {
    return (WebhookEntity) request(
      "WebhookEntity",
      "POST",
      URI,
      payload,
      false
    );
  }

  public static WebhookEntity disable(String id) throws StandardException {
    return (WebhookEntity) request(
      "WebhookEntity",
      "POST",
      URI + "/" + id + "/disable",
      null,
      false
    );
  }

  public static WebhookEntity enable(String id) throws StandardException {
    return (WebhookEntity) request(
      "WebhookEntity",
      "POST",
      URI + "/"+ id + "/enable",
      null,
      false
    );
  }

  public static WebhookEntity retrieve(String id) throws StandardException {
    return (WebhookEntity) request(
      "WebhookEntity",
      "GET",
      URI + "/" + id,
      null,
      false
    );
  }

  public static WebhookEntity update(String id, Object payload) throws StandardException {
    return (WebhookEntity) request(
      "WebhookEntity",
      "PUT",
      URI + "/" + id,
      payload,
      false
    );
  }
}
