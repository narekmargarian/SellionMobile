package com.sellion.mobile.entity;

import java.util.Map;

public class OrderModel {
    public enum Status {PENDING, SENT, COMPLETED}

    public String shopName;
    public Status status;
    public Map<String, Integer> items;
    public boolean isReturn; // Флаг возврата

    public String paymentMethod;
    public boolean needsSeparateInvoice;

    // ОБНОВЛЕННЫЙ КОНСТРУКТОР
    public OrderModel(String shopName, Map<String, Integer> items, String paymentMethod, boolean needsSeparateInvoice, boolean isReturn) {
        this.shopName = shopName;
        this.items = items;
        this.paymentMethod = paymentMethod;
        this.needsSeparateInvoice = needsSeparateInvoice;
        this.isReturn = isReturn; // Устанавливаем тип
        this.status = Status.PENDING;
    }
}
