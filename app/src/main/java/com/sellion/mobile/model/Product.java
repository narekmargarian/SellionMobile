package com.sellion.mobile.model;


public class Product {

    private long id; // ДОБАВЛЕНО
    private String name;
    private Double price;
    private Integer itemsPerBox;
    private String barcode;
    private String category;
    private Integer stockQuantity;

    public Product() {}

    // Обновленный конструктор
    public Product(long id, String name, Double price, Integer itemsPerBox, String barcode, String category, Integer stockQuantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.itemsPerBox = itemsPerBox;
        this.barcode = barcode;
        this.category = category;
        this.stockQuantity = stockQuantity;
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
    public long getId() { return id; } // Геттер


    // ДОБАВЛЕНО: Геттер для остатка
    public int getStockQuantity() {
        return stockQuantity != null ? stockQuantity : 0;
    }
}