package com.sellion.mobile.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "clients")
public class ClientEntity {
    @PrimaryKey
    public int id;
    public String name;
    public String address;
    public String ownerName;
    public String inn;
    public String routeDay;
    public double debt;
}