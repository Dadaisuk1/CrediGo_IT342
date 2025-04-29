package com.paymongo.exceptions;

import java.util.Map;

public class AuthenticationException extends StandardException {
  public AuthenticationException(Map<String, Object> response) {
    super(response);
  }
}
