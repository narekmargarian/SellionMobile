package com.sellion.mobile.entity;

public class Product {

    private String name;
    private int price;
    private int itemsPerBox;
    private String barcode;
    private String category;

    // 1. Пустой конструктор (нужен для Retrofit)
    public Product() {
    }

    // 2. Конструктор с 4 параметрами (исправит твою ошибку)
    public Product(String name, int price, int itemsPerBox, String barcode) {
        this.name = name;
        this.price = price;
        this.itemsPerBox = itemsPerBox;
        this.barcode = barcode;
    }

    // Геттеры (убедись, что они есть)
    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public String getBarcode() {
        return barcode;
    }

    public int getItemsPerBox() {
        return itemsPerBox;
    }
}
