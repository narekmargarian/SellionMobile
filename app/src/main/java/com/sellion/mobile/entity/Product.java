package com.sellion.mobile.entity;

public class Product {
    public String name;
    public String price;

    public Product(String name, String price) {
        this.name = name;
        this.price = price;
    }

    // Либо (более правильный подход) добавь геттеры:
    public String getName() { return name; }
    public String getPrice() { return price; }
}

