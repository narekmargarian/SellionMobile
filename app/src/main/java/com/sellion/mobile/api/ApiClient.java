package com.sellion.mobile.api;

import android.content.Context;
import android.content.SharedPreferences;

import com.sellion.mobile.managers.SessionManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ApiClient {
    private static final String DEFAULT_IP = "172.20.10.5";
    private static final String DEFAULT_PORT = "8080"; // Дефолтный порт
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            SharedPreferences prefs = context.getSharedPreferences("SyncSettings", Context.MODE_PRIVATE);

            // Читаем IP и Порт отдельно
            String savedIp = prefs.getString("server_ip", DEFAULT_IP);
            String savedPort = prefs.getString("server_port", DEFAULT_PORT);

            //TODO STEX BDI HTTPS EXNIIIIII
            String dynamicBaseUrl = "http://" + savedIp + ":" + savedPort + "/";

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .followRedirects(false)
                    .followSslRedirects(false)
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        String myKey = SessionManager.getInstance().getApiKey();
                        Request.Builder rb = original.newBuilder()
                                .header("Content-Type", "application/json")
                                .header("Accept", "application/json");

                        if (myKey != null && !myKey.isEmpty()) {
                            rb.header("X-API-Key", myKey);
                        }
                        return chain.proceed(rb.build());
                    })
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(dynamicBaseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static void resetClient() {
        retrofit = null;
    }
}
