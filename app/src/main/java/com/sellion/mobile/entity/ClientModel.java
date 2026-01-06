package com.sellion.mobile.entity;

public class ClientModel {
    public String name;
    public String address;
    public String ip;

    public ClientModel(String name, String address, String ip) {
        this.name = name;
        this.address = address;
        this.ip = ip;
    }

    @Override
    public String toString() {
        return name; // Чтобы отображалось имя магазина в списках/диалогах
    }
}

