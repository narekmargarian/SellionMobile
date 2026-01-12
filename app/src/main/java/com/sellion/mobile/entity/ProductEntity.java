package com.sellion.mobile.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "products")
public class ProductEntity {
    @PrimaryKey
    @NonNull
    public String name;
    public double price; // Изменено на double
    public int itemsPerBox;
    public String barcode;
    public String category;

    public ProductEntity(@NonNull String name, double price, int itemsPerBox, String barcode, String category) {
        this.name = name;
        this.price = price;
        this.itemsPerBox = itemsPerBox;
        this.barcode = barcode;
        this.category = category;
    }
}