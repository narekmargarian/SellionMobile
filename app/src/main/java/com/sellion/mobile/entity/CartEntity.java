package com.sellion.mobile.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cart")
public class CartEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String productName;
    public int quantity;

    public CartEntity(String productName, int quantity) {
        this.productName = productName;
        this.quantity = quantity;
    }
}