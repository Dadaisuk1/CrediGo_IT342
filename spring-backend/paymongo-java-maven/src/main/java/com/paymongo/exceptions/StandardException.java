package com.paymongo.exceptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.paymongo.PaymongoError;

public class StandardException extends Exception {
  private Map<String, Object> data;
  private List<PaymongoError> errors;

  public StandardException(Map<String, Object> response) {
    super();
    this.data = response;
    this.errors = new ArrayList<>();
    List<Map<String, Object>> errorList = (List<Map<String, Object>>) response.get("errors");
    if (errorList != null) {
      for (Map<String, Object> error : errorList) {
        this.errors.add(new PaymongoError(error));
      }
    }
  }

  public Map<String, Object> getData() {
    return data;
  }

  public List<PaymongoError> getErrors() {
    return errors;
  }
}
