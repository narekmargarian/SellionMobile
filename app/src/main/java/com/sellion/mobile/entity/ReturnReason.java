package com.sellion.mobile.entity;

public enum ReturnReason {
    EXPIRED("Испорченный (просрочка)"),
    DAMAGED("Поврежденная упаковка"),
    WAREHOUSE("На склад"),
    REJECTION("Отказ клиента"),
    OTHER("Другое");

    private final String title;
    ReturnReason(String title) { this.title = title; }
    public String getTitle() { return title; }
    public static String[] getAllTitles() {
        ReturnReason[] values = ReturnReason.values();
        String[] titles = new String[values.length];
        for (int i = 0; i < values.length; i++) titles[i] = values[i].getTitle();
        return titles;
    }
}