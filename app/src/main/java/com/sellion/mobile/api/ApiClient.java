package com.sellion.mobile.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    // ВАЖНО: Убедись, что нет лишних пробелов внутри кавычек
    private static final String BASE_URL = "http://172.20.10.5:8080/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            try {
                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return retrofit;
    }
}