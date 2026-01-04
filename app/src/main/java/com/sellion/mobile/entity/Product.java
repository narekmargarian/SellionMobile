package com.sellion.mobile.entity;

public class Product {
    private String name;
    private int price;
    private int itemsPerBox;
    private String barcode; // Новое поле для штрих-кода

    public Product(String name, int price, int itemsPerBox, String barcode) {
        this.name = name;
        this.price = price;
        this.itemsPerBox = itemsPerBox;
        this.barcode = barcode;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public int getItemsPerBox() {
        return itemsPerBox;
    }

    public String getBarcode() {
        return barcode;
    }
}