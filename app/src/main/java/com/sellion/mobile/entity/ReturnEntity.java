package com.sellion.mobile.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Map;

@Entity(tableName = "returns")
public class ReturnEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String shopName;
    public Map<String, Integer> items;
    public String returnReason;
    public String returnDate;
    public String status; // PENDING, SENT

    public String managerId;   // КТО отправил
    public double totalAmount; // СУММА возврата
}