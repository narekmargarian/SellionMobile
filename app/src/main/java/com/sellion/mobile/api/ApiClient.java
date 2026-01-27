package com.sellion.mobile.api;

import android.content.Context;
import android.provider.Settings;

import com.sellion.mobile.managers.SessionManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "http://172.20.10.5:8080/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        // Убираем static Retrofit и if(null), создаем всегда свежий
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    // Берем ключ прямо в момент запроса!
                    String myKey = SessionManager.getInstance().getApiKey();

                    Request.Builder rb = original.newBuilder()
                            .header("Content-Type", "application/json");

                    if (myKey != null && !myKey.isEmpty()) {
                        rb.header("X-API-Key", myKey);
                    }
                    return chain.proceed(rb.build());
                })
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }



    // Добавьте этот метод для возможности смены IP или очистки сессии
    public static void resetClient() {
        retrofit = null;
    }
}
