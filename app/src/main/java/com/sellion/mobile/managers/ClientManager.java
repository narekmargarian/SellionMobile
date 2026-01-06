package com.sellion.mobile.managers;

import java.util.ArrayList;
import java.util.List;

public class ClientManager {
    private static ClientManager instance;
    private final List<String> storeNames = new ArrayList<>();

    private ClientManager() {
        // Добавляем 30 тестовых магазинов
        for (int i = 1; i <= 30; i++) {
            storeNames.add("Магазин №" + i);
        }
    }

    public static ClientManager getInstance() {
        if (instance == null) instance = new ClientManager();
        return instance;
    }

    public List<String> getStoreNames() {
        return storeNames;
    }
}

