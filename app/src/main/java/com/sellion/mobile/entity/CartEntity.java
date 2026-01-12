package com.sellion.mobile.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cart")
public class CartEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String productName;
    public int quantity;
    public double price; // Изменено на double

    public CartEntity(String productName, int quantity, double price) {
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }
}