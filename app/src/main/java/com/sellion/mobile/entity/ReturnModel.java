package com.sellion.mobile.entity;

import java.util.Map;

public class ReturnModel {
    public enum Status { PENDING, SENT }
    public String shopName;
    public Map<String, Integer> items;
    public String returnReason;
    public String returnDate;
    public Status status = Status.PENDING;

    public ReturnModel(String shopName, Map<String, Integer> items, String returnReason, String returnDate) {
        this.shopName = shopName;
        this.items = items;
        this.returnReason = returnReason;
        this.returnDate = returnDate;
    }
}



