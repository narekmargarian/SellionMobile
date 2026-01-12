package com.sellion.mobile.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "products")
public class ProductEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public double price;
    public int itemsPerBox;
    public String barcode;
    public String category;

    // НОВОЕ ПОЛЕ
    public int stockQuantity;

    public ProductEntity(String name, double price, int itemsPerBox, String barcode, String category, int stockQuantity) {
        this.name = name;
        this.price = price;
        this.itemsPerBox = itemsPerBox;
        this.barcode = barcode;
        this.category = category;
        this.stockQuantity = stockQuantity;
    }
}