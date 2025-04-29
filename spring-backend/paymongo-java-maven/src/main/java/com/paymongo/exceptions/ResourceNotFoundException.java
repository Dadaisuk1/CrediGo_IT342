package com.paymongo.exceptions;

import java.util.Map;

public class ResourceNotFoundException extends StandardException {
  public ResourceNotFoundException(Map<String, Object> response) {
    super(response);
  }
}
