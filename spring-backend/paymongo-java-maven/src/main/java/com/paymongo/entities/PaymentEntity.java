package com.paymongo.entities;

import java.util.ArrayList;
import java.util.Map;

import com.paymongo.ApiResource;

public class PaymentEntity {
  public String id;
  public String access_url;
  public int amount;
  public BillingEntity billing;
  public String currency;
  public String description;
  public int fee;
  public Boolean livemode;
  public Object metadata;
  public int net_amount;
  public String payment_intent_id;
  public String payout;
  public ArrayList<Object> refunds;
  public Object source;
  public String statement_descriptor;
  public String status;
  public int tax_amount;
  public ArrayList<Object> taxes;
  public Integer available_at;
  public int created_at;
  public int paid_at;
  public int updated_at;

  public PaymentEntity(Map<String, Object> data) {
    var attributes = (Map<String, Object>) data.get("attributes");

    this.id = (String) data.get("id");
    this.amount = (int) attributes.get("amount");
    this.access_url = (String) attributes.get("access_url");

    if (attributes.get("billing") != null) {
      this.billing = new BillingEntity((Map<String, Object>) attributes.get("billing"));
    }

    this.currency = (String) attributes.get("currency");
    this.description = (String) attributes.get("description");
    this.fee = (int) attributes.get("fee");
    this.livemode = (Boolean) attributes.get("livemode");
    this.metadata = attributes.get("metadata");
    this.net_amount = (int) attributes.get("net_amount");
    this.payment_intent_id = (String) attributes.get("payment_intent_id");
    this.payout = (String) attributes.get("payout");
    this.refunds = new ArrayList<Object>();

    if (!attributes.get("refunds").equals(new ArrayList<Object>())
        && (ArrayList<Object>) attributes.get("refunds") instanceof ArrayList<Object>) {
      ArrayList<Object> refunds = (ArrayList<Object>) attributes.get("refunds");

      for (Object refund : refunds) {
        ApiResource row_refund = new ApiResource((Map<String, Object>) refund);
        RefundEntity refund_data = new RefundEntity((Map<String, Object>) row_refund.data);
        this.refunds.add(refund_data);
      }
    }

    this.source = attributes.get("source");
    this.statement_descriptor = (String) attributes.get("statement_descriptor");
    this.status = (String) attributes.get("status");
    this.taxes = (ArrayList<Object>) attributes.get("taxes");
    this.available_at = (Integer) attributes.get("available_at");
    this.created_at = (int) attributes.get("created_at");
    this.paid_at = (int) attributes.get("paid_at");
    this.updated_at = (int) attributes.get("updated_at");
  }
}
