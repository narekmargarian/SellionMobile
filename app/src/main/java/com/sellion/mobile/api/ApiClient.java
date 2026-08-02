package com.sellion.mobile.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.sellion.mobile.managers.SessionManager;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String DEFAULT_DOMAIN = "sellion.vip";
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            SharedPreferences prefs = context.getSharedPreferences("SyncSettings", Context.MODE_PRIVATE);
            String savedDomain = prefs.getString("server_ip", DEFAULT_DOMAIN);
            String dynamicBaseUrl = "https://" + savedDomain + "/";

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        String myKey = SessionManager.getInstance().getApiKey();

                        // Логируем ключ для отладки в Logcat (виден с тегом APICLIENT)
                        Log.d("APICLIENT", "Current API Key: " + (myKey != null ? myKey : "NULL"));

                        Request.Builder rb = original.newBuilder()
                                .header("Content-Type", "application/json")
                                .header("Accept", "application/json")
                                .header("X-Sellion-Platform", "Sellion-Android-App-v1");

                        if (myKey != null && !myKey.trim().isEmpty()) {
                            rb.header("X-API-Key", myKey.trim());
                        }

                        return chain.proceed(rb.build());
                    })
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS);

            retrofit = new Retrofit.Builder()
                    .baseUrl(dynamicBaseUrl)
                    .client(httpClientBuilder.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static void resetClient() {
        retrofit = null;
    }
}