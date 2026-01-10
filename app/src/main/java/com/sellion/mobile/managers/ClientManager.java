package com.sellion.mobile.managers;

import com.sellion.mobile.entity.ClientModel;

import java.util.ArrayList;
import java.util.List;

public class ClientManager {
    private static ClientManager instance;
    private final List<String> storeNames = new ArrayList<>();
    // Также добавим список моделей клиентов для хранения адресов и ИП
    public final List<ClientModel> clientList = new ArrayList<>();

    private ClientManager() {
        // Используем реальные названия для тестирования
        clientList.add(new ClientModel("ZOVQ Arshakunyac", "Ереван, ул. Аршакуняц 15", "ИП Акопян"));
        clientList.add(new ClientModel("ZOVQ Bagratunyac", "Ереван, ул. Багратуняц 20", "ИП Нарине"));
        clientList.add(new ClientModel("Carrefour ТЦ Ереван Мол", "Ереван, ул. Аршакуняц 34", "ООО Фуд"));
        clientList.add(new ClientModel("Evrika Улыбка", "Ереван, ул. Пушкина 10", "ИП Варданян"));
        clientList.add(new ClientModel("MG Маркет Аван", "Ереван, Аван, 4-й квартал", "ИП Григорян"));
        clientList.add(new ClientModel("SAS Супермаркет Комитас", "Ереван, пр. Комитаса 1", "ООО САС"));

        // Заполняем список имен для маршрутов
        for (ClientModel client : clientList) {
            storeNames.add(client.getName());
        }
    }

    public static ClientManager getInstance() {
        if (instance == null) instance = new ClientManager();
        return instance;
    }

    public List<String> getStoreNames() {
        return storeNames;
    }

    // Новый метод для получения полной модели клиента
    public ClientModel getClientByName(String name) {
        for (ClientModel client : clientList) {
            if (client.getName().equals(name)) {
                return client;
            }
        }
        return null;
    }
}

