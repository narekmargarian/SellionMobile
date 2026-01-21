package com.sellion.mobile.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cart")
public class CartEntity {
    @PrimaryKey
    public long productId; // Используем ID товара как первичный ключ корзины
    public String productName;
    public int quantity;
    public double price;

    public CartEntity(long productId, String productName, int quantity, double price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }
}