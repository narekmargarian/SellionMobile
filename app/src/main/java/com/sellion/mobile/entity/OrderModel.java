package com.sellion.mobile.entity;

import java.util.Map;

public class OrderModel {
    public enum Status { PENDING, SENT, COMPLETED } // Ожидает, Отправлен, Завершен

    public String shopName;
    public Status status;
    public Map<String, Integer> items; // Товары и количество

    public OrderModel(String shopName, Map<String, Integer> items) {
        this.shopName = shopName;
        this.items = items;
        this.status = Status.PENDING; // По умолчанию новый заказ ожидает отправки
    }
}

