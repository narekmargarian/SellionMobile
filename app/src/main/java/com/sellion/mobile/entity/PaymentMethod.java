package com.sellion.mobile.entity;

public enum PaymentMethod {

    CASH("Наличный"),
    TRANSFER("Перевод");

    private final String title;
    PaymentMethod(String title) { this.title = title; }
    public String getTitle() { return title; }
}
