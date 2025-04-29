package com.paymongo.exceptions;

import java.util.Map;

public class InvalidRequestException extends StandardException {
  public InvalidRequestException(Map<String, Object> response) {
    super(response);
  }
}
