package com.sellion.mobile.entity;

import java.util.Map;

public class OrderModel {
    public enum Status { PENDING, SENT }
    public String shopName;
    public Map<String, Integer> items;
    public String paymentMethod;
    public String deliveryDate;
    public boolean needsSeparateInvoice;
    public Status status = Status.PENDING;

    public OrderModel(String shopName, Map<String, Integer> items, String paymentMethod, String deliveryDate, boolean needsSeparateInvoice) {
        this.shopName = shopName;
        this.items = items;
        this.paymentMethod = paymentMethod;
        this.deliveryDate = deliveryDate;
        this.needsSeparateInvoice = needsSeparateInvoice;
    }
}