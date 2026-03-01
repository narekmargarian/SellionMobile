package com.sellion.mobile.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


@Entity(tableName = "orders")
public class OrderEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String shopName;
    public Map<Long, Integer> items;
    public String status; // "PENDING" или "SENT"
    public String deliveryDate;
    public PaymentMethod paymentMethod;
    public boolean needsSeparateInvoice;
    public String managerId;
    public double totalAmount;
    public String createdAt;
    public String androidId;
    public Map<Long, BigDecimal> appliedPromoItems = new HashMap<>();
    public double discountPercent;



}

