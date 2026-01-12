package com.sellion.mobile.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Map;


@Entity(tableName = "orders")
public class OrderEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String shopName;
    public Map<String, Integer> items;
    public String status; // "PENDING" или "SENT"
    public String deliveryDate;
    public String paymentMethod;
    public boolean needsSeparateInvoice;
    public String managerId; // Добавь это поле!
    public double totalAmount;

}

