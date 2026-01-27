package com.sellion.mobile.managers;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static SessionManager instance;
    private final SharedPreferences prefs;

    // Константы для ключей
    private static final String PREF_NAME = "SellionSession";
    private static final String KEY_MANAGER_ID = "manager_id";
    private static final String KEY_API_KEY = "api_key";
    private static final String KEY_CUSTOM_API_KEY = "custom_api_key";

    // Приватный конструктор с инициализацией SharedPreferences
    private SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Инициализация при запуске приложения (в MainActivity или HostActivity)
    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            throw new RuntimeException("SessionManager must be initialized with init(context) first!");
        }
        return instance;
    }

    // --- ЛОГИКА СОХРАНЕНИЯ (Ваша структура сохранена) ---

    public void setManagerId(String id) {
        prefs.edit().putString(KEY_MANAGER_ID, id).apply();
    }

    public String getManagerId() {
        return prefs.getString(KEY_MANAGER_ID, null);
    }

    public void setApiKey(String key) {
        prefs.edit().putString(KEY_API_KEY, key).apply();
    }

    public String getApiKey() {
        return prefs.getString(KEY_API_KEY, null);
    }

    public void setCustomApiKey(String key) {
        prefs.edit().putString(KEY_CUSTOM_API_KEY, key).apply();
    }

    public String getCustomApiKey() {
        return prefs.getString(KEY_CUSTOM_API_KEY, null);
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}
