package com.sellion.mobile.entity;

public enum ReturnReason {
    EXPIRED("Просрочка"),
    DAMAGED("Поврежденная упаковка"),
    WAREHOUSE("На склад"),
    OTHER("Другое");

    private final String title;
    ReturnReason(String title) { this.title = title; }
    public String getTitle() { return title; }

}