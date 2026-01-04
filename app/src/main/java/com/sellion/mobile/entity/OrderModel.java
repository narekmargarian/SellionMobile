package com.sellion.mobile.entity;

import java.util.Map;

public class OrderModel {
    public enum Status {PENDING, SENT, COMPLETED}

    public String shopName;
    public Status status;
    public Map<String, Integer> items;

    // Новые поля
    public String paymentMethod;
    public boolean needsSeparateInvoice;

    public OrderModel(String shopName, Map<String, Integer> items, String paymentMethod, boolean needsSeparateInvoice) {
        this.shopName = shopName;
        this.items = items;
        this.paymentMethod = paymentMethod;
        this.needsSeparateInvoice = needsSeparateInvoice;
        this.status = Status.PENDING;
    }
}

