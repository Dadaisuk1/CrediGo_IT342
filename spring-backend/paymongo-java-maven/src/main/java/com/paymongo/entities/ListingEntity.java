package com.paymongo.entities;

import java.util.ArrayList;

public class ListingEntity {
  public ArrayList data;
  public Boolean has_more;

  public ListingEntity(ArrayList data, Boolean has_more) {
    this.data = (ArrayList) data;
    this.has_more = (Boolean) has_more;
  }
}
