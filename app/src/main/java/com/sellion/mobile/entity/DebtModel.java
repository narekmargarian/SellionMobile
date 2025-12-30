package com.sellion.mobile.entity;

public class DebtModel {
    private String shopName;
    private String ownerName;
    private String inn;
    private String address;
    private double debtAmount;

    // Конструктор: помогает легко создавать новые объекты DebtModel
    public DebtModel(String shopName, String ownerName, String inn, String address, double debtAmount) {
        this.shopName = shopName;
        this.ownerName = ownerName;
        this.inn = inn;
        this.address = address;
        this.debtAmount = debtAmount;
    }

    // Геттеры (методы для получения данных из объекта)
    public String getShopName() {
        return shopName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getInn() {
        return inn;
    }

    public String getAddress() {
        return address;
    }

    public double getDebtAmount() {
        return debtAmount;
    }
}