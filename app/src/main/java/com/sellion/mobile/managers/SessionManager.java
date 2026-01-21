package com.sellion.mobile.managers;

public class SessionManager {
    private static SessionManager instance;
    private String managerId;
    private String apiKey; // ДОБАВЛЕНО

    private String customApiKey;

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void setCustomApiKey(String key) { this.customApiKey = key; }
    public String getCustomApiKey() { return customApiKey; }

    public void setManagerId(String id) {
        this.managerId = id;
    }

    public String getManagerId() {
        return managerId;
    }

    // ДОБАВЬТЕ ЭТОТ МЕТОД:
    public void clearSession() {
        this.managerId = null;
    }
    // ДОБАВЛЕНО: Геттер и Сеттер для ключа
    public void setApiKey(String key) { this.apiKey = key; }
    public String getApiKey() { return apiKey; }
}