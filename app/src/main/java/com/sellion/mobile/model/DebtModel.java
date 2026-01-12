package com.sellion.mobile.model;

public class DebtModel {
    private final String shopName;
    private final String ownerName;
    private final String inn;
    private final String address;
    private final double debtAmount;


    public DebtModel(String shopName, String ownerName, String inn, String address, double debtAmount) {
        this.shopName = shopName;
        this.ownerName = ownerName;
        this.inn = inn;
        this.address = address;
        this.debtAmount = debtAmount;
    }


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