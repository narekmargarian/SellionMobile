package com.sellion.mobile.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "managers")
public class ManagerEntity {
    @PrimaryKey
    @NonNull
    public String id; // Сюда запишем "1011", "1012" и т.д.

    public ManagerEntity(@NonNull String id) { this.id = id; }
}