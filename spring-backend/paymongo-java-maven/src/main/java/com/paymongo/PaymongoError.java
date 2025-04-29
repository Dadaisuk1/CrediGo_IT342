package com.paymongo;

import java.util.Map;

public class PaymongoError {
  private String code;
  private String detail;
  private String source;

  public PaymongoError(Map<String, Object> error) {
    this.code = error.containsKey("code") ? error.get("code").toString() : null;
    this.detail = error.get("detail").toString();
    this.source = error.containsKey("source") ? error.get("source").toString() : null;
  }

  public String getCode() {
    return code;
  }

  public String getDetail() {
    return detail;
  }

  public String getSource() {
    return source;
  }
}
