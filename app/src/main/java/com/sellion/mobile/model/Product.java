package com.sellion.mobile.model;


public class Product {

    private String name;
    private Double price;
    private Integer itemsPerBox;
    private String barcode;
    private String category;
    // ДОБАВЛЕНО: Поле для остатка
    private Integer stockQuantity;

    public Product() {
    }

    // Обновленный конструктор (теперь 6 параметров)
    public Product(String name, Double price, Integer itemsPerBox, String barcode, String category, Integer stockQuantity) {
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

    // ДОБАВЛЕНО: Геттер для остатка
    public int getStockQuantity() {
        return stockQuantity != null ? stockQuantity : 0;
    }
}