package com.paymongo.services;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;

import com.paymongo.ApiResource;
import com.paymongo.PaymongoClient;
import com.paymongo.entities.CustomerEntity;
import com.paymongo.entities.LinkEntity;
import com.paymongo.entities.ListingEntity;
import com.paymongo.entities.PaymentEntity;
import com.paymongo.entities.PaymentIntentEntity;
import com.paymongo.entities.PaymentMethodEntity;
import com.paymongo.entities.RefundEntity;
import com.paymongo.entities.WebhookEntity;
import com.paymongo.exceptions.StandardException;

/**
 * BaseService
 */
public class BaseService {
  public static Object request(String entity, String method, String path, Object payload) throws StandardException {
    return request(entity, method, path, payload, false);
  }

  public static Object request(String entity, String method, String path, Object payload, Boolean is_listing) throws StandardException {
    try {
      ApiResource api_resource = PaymongoClient.execute_request(method, payload, path);

      if (is_listing) {
        ArrayList<Map<String, Object>> data_list = (ArrayList<Map<String, Object>>) api_resource.data;
        ArrayList resource_list = new ArrayList<>();

        for (Map<String, Object> data : data_list) {
          resource_list.add(map_response(entity, new ApiResource(data)));
        }

        return new ListingEntity(resource_list, api_resource.has_more);
      } else {
        return map_response(entity, api_resource);
      }
    } catch (URISyntaxException | IOException | InterruptedException e) {
      e.printStackTrace();
    }

    return null;
  }

  private static Object map_response(String entity, ApiResource api_resource) {
    switch (entity) {
      case "CustomerEntity":
        return new CustomerEntity((Map<String, Object>) api_resource.data);
      case "LinkEntity":
        return new LinkEntity((Map<String, Object>) api_resource.data);
      case "PaymentEntity":
        return new PaymentEntity((Map<String, Object>) api_resource.data);
      case "PaymentIntentEntity":
        return new PaymentIntentEntity((Map<String, Object>) api_resource.data);
      case "PaymentMethodEntity":
        return new PaymentMethodEntity((Map<String, Object>) api_resource.data);
      case "RefundEntity":
        return new RefundEntity((Map<String, Object>) api_resource.data);
      case "WebhookEntity":
        return new WebhookEntity((Map<String, Object>) api_resource.data);
    }

    return null;
  }
}
