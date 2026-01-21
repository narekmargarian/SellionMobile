package com.sellion.mobile.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "products")
public class ProductEntity {
    @PrimaryKey // Убираем autoGenerate, так как ID придет с сервера
    public long id;
    public String name;
    public double price;
    public int itemsPerBox;
    public String barcode;
    public String category;
    public int stockQuantity;

    // Обновите конструктор
    public ProductEntity(long id, String name, double price, int itemsPerBox, String barcode, String category, int stockQuantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.itemsPerBox = itemsPerBox;
        this.barcode = barcode;
        this.category = category;
        this.stockQuantity = stockQuantity;
    }
}