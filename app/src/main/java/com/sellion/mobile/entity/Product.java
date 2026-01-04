package com.sellion.mobile.entity;

public class Product {
    private String name;
    private int price;
    private int itemsPerBox; // Новое поле

    public Product(String name, int price, int itemsPerBox) {
        this.name = name;
        this.price = price;
        this.itemsPerBox = itemsPerBox;
    }

    public String getName() { return name; }
    public int getPrice() { return price; }
    public int getItemsPerBox() { return itemsPerBox; }
}
