package com.sellion.mobile.model;

public class ClientModel {
    public int id;
    public String name;
    public String address;
    public String ownerName;
    public String inn;
    public String phone;
    public String routeDay;
    public double debt;

    // Пустой конструктор для Retrofit
    public ClientModel() {}

    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getIp() { return ownerName; } // Оставляем getIp для совместимости с вашим старым кодом



    public ClientModel(String name, String address, String inn) {
        this.name = name;
        this.address = address;
        this.inn = inn;
    }
}

