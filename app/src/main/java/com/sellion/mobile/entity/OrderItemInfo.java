package com.sellion.mobile.entity;

public class OrderItemInfo {
    public String name;
    public int quantity;
    public double price;
    public int stock;

    public OrderItemInfo(String name, int quantity, double price, int stock) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.stock = stock;
    }
}

