package com.sellion.mobile.model;


public class Product {
    
    private String name;
    private Double price;
    private Integer itemsPerBox;
    private String barcode;
    private String category;

    // 1. Пустой конструктор для Retrofit
    public Product() {
    }

    // 2. Исправленный конструктор с 5 параметрами
    public Product(String name, Double price, Integer itemsPerBox, String barcode, String category) {
        this.name = name;
        this.price = price;
        this.itemsPerBox = itemsPerBox;
        this.barcode = barcode;
        this.category = category; // Теперь категория будет сохраняться
    }

    // Геттеры
    public String getName() {
        return name;
    }

    public double getPrice() {
        return price != null ? price : 0.0;
    }

    public String getCategory() {
        return category;
    }

    public String getBarcode() {
        return barcode;
    }

    public Integer getItemsPerBox() {
        return itemsPerBox;
    }
}