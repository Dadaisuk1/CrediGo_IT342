package com.paymongo;

import java.util.ArrayList;
import java.util.Map;
/**
 * ApiResource
 */
public class ApiResource {
  public Object attributes;
  public Object data;
  public String id;
  public boolean has_more;

  public ApiResource(Map<String, Object> response) {
    if (response.containsKey("data")) {
      Object dataObj = response.get("data");

      this.data = dataObj instanceof Map ? (Map<String, Object>) dataObj : (ArrayList) dataObj;
    } else {
      this.data = response;
    }

    if (this.data instanceof Map) {
      if (((Map<String, Object>) data).containsKey("attributes")) {
        Object attributesObj = ((Map<String, Object>) data).get("attributes");

        this.attributes = (Map<String, Object>) attributesObj;
        this.id = (String) ((Map<String, Object>) data).get("id");
      }
    }

    if (response.containsKey("has_more")) {
      this.has_more = (boolean) response.get("has_more");
    }
  }
}
