package com.sellion.mobile.managers;

import com.sellion.mobile.entity.ClientModel;

import java.util.ArrayList;
import java.util.List;

public class ClientManager {
    private static ClientManager instance;
    private final List<String> storeNames = new ArrayList<>();
    // Также добавим список моделей клиентов для хранения адресов и ИП
    public final List<ClientModel> clientList = new ArrayList<>();



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

    private ClientManager() {
        // Используем реальные названия для тестирования
        clientList.add(new ClientModel("Զովք Շրջանային", "Հայաստան, Երևան", "ԻՊ Հակոբյան"));
        clientList.add(new ClientModel("Զովք Աէրացիա", "Հայաստան, Երևան", "ԻՊ Հակոբյան"));
        clientList.add(new ClientModel("Զովք Ամիրյան", "Հայաստան, Երևան", "ԻՊ Հակոբյան"));
        clientList.add(new ClientModel("Զովք Ավան", "Հայաստան, Երևան", "ԻՊ Հակոբյան"));
        clientList.add(new ClientModel("Զովք Արշակունյաց", "Երևան, ул. Аршакуняц 15", "ԻՊ Հակոբյան"));
        clientList.add(new ClientModel("Զովք Բաբաջանյան", "Հայաստան, Երևան", "ԻՊ Հակոբյան"));
        clientList.add(new ClientModel("Զովք Բագրատունյաց", "Երևան, ул. Багратуняц 20", "ԻՊ Նարինե"));
        clientList.add(new ClientModel("Զովք Բագրատունյաց", "Երևան, ул. Багратуняц 20", "ԻՊ Նարինե")); // Կրկնությունն ավելացված է օրինակով
        clientList.add(new ClientModel("Զովք Բակունց", "Հայաստան, Երևան", "ԻՊ Հակոբյան"));
        clientList.add(new ClientModel("Զովք Բեկնազարյան", "Հայաստան, Երևան", "ԻՊ Հակոբյան"));
        clientList.add(new ClientModel("Զովք Գալշոյան", "Հայաստան, Երևան", "ԻՊ Հակոբյան"));
        clientList.add(new ClientModel("Զովք Գյուլբենկյան", "Հայաստան, Երևան", "ԻՊ Հակոբյան"));
        clientList.add(new ClientModel("Զովք Գյուրջյան", "Հայաստան, Երևան", "ԻՊ Հակոբյան"));
        clientList.add(new ClientModel("Զովք Դավթաշեն", "Հայաստան, Երևան", "ԻՊ Հակոբյան"));
        clientList.add(new ClientModel("Զովք Դավիթ-Բեկ", "Հայաստան, Երևան", "ԻՊ Հակոբյան"));
        clientList.add(new ClientModel("Զովք Դրո", "Հայաստան, Երևան", "ԻՊ Հակոբյան"));
        clientList.add(new ClientModel("Զովք Զվարթնոց26", "Հայաստան, Երևան", "ԻՊ Հակոբյան"));
        clientList.add(new ClientModel("Զովք Լենինգրադյան", "Հայաստան, Երևան", "ԻՊ Հակոբյան"));
        clientList.add(new ClientModel("Զովք Խորենացի", "Հայաստան, Երևան", "ԻՊ Հակոբյան"));
        clientList.add(new ClientModel("Զովք Կոմիտաս", "Հայաստան, Երևան", "ԻՊ Հակոբյան"));
        clientList.add(new ClientModel("Զովք Հ․ Հակոբյան", "Հայաստան, Երևան", "ԻՊ Հակոբյան"));
        clientList.add(new ClientModel("Զովք Հանրապետություն", "Հայաստան, Երևան", "ԻՊ Հակոբյան"));
        clientList.add(new ClientModel("Զովք Մոնումենտ", "Հայաստան, Երևան", "ԻՊ Հակոբյան"));
        clientList.add(new ClientModel("Զովք Մուրացան", "Հայաստան, Երևան", "ԻՊ Հակոբյան"));
        clientList.add(new ClientModel("Զովք Նալբանդյան", "Հայաստան, Երևան", "ԻՊ Հակոբյան"));
        clientList.add(new ClientModel("Զովք Շերամ", "Հայաստան, Երևան", "ԻՊ Հակոբյան"));
        clientList.add(new ClientModel("Զովք Շիրազ", "Հայաստան, Երևան", "ԻՊ Հակոբյան"));
        clientList.add(new ClientModel("Զովք Սարյան", "Հայաստան, Երևան", "ԻՊ Հակոբյան"));
        clientList.add(new ClientModel("Զովք Վերին Պտղնի", "Հայաստան, Երևան", "ԻՊ Հակոբյան"));
        clientList.add(new ClientModel("Զովք Վիլնյուս", "Հայաստան, Երևան", "ԻՊ Հակոբյան"));
        clientList.add(new ClientModel("Զովք Փափազյան", "Հայաստան, Երևան", "ԻՊ Հակոբյան"));
        clientList.add(new ClientModel("Զովք Քաջազնունի", "Հայաստան, Երևան", "ԻՊ Հակոբյան"));
        clientList.add(new ClientModel("Զովք Հյուսիսային", "Հայաստան, Երևան", "ԻՊ Հակոբյան"));
        clientList.add(new ClientModel("Carfur YM", "Երևան, ул. Аршакуняц 34", "ՍՊԸ Ֆուդ"));
        clientList.add(new ClientModel("Carfur Abovyan", "Հայաստան, Աբովյան", "ՍՊԸ Ֆուդ"));
        clientList.add(new ClientModel("Carfur RM", "Հայաստան, Երևան", "ՍՊԸ Ֆուդ"));
        clientList.add(new ClientModel("Carfur hanrapetutyun", "Հայաստան, Երևան", "ՍՊԸ Ֆուդ"));
        clientList.add(new ClientModel("Carfur davtashen", "Հայաստան, Երևան", "ՍՊԸ Ֆուդ"));
        clientList.add(new ClientModel("Carfur azatutyun", "Հայաստան, Երևան", "ՍՊԸ Ֆուդ"));
        clientList.add(new ClientModel("Carfur gyulbekyan", "Հայաստան, Երևան", "ՍՊԸ Ֆուդ"));
        clientList.add(new ClientModel("Carfur antarayin", "Հայաստան, Երևան", "ՍՊԸ Ֆուդ"));
        clientList.add(new ClientModel("Carfur argishti", "Հայաստան, Երևան", "ՍՊԸ Ֆուդ"));
        clientList.add(new ClientModel("Carfur buzand", "Հայաստան, Երևան", "ՍՊԸ Ֆուդ"));
        clientList.add(new ClientModel("carfur paraqar", "Հայաստան, Երևան", "ՍՊԸ Ֆուդ"));
        clientList.add(new ClientModel("Carfur masiv", "Հայաստան, Երևան", "ՍՊԸ Ֆուդ"));
        clientList.add(new ClientModel("Carfur ajapnyak", "Հայաստան, Երևան", "ՍՊԸ Ֆուդ"));
        clientList.add(new ClientModel("Evrika Улыбка", "Երևան, ул. Пушкина 10", "ԻՊ Վարդանյան"));
        clientList.add(new ClientModel("MG Маркет Аван", "Երևան, Аван, 4-й квартал", "ԻՊ Գրիգորյան"));
        clientList.add(new ClientModel("SAS Супермаркет Комитас", "Երևան, пр. Комитаса 1", "ՍՊԸ ՍԱՍ"));

        // Заполняем список имен для маршрутов
        for (ClientModel client : clientList) {
            storeNames.add(client.getName());
        }
    }
}

