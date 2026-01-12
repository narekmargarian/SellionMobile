package com.sellion.mobile.sync;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.sellion.mobile.api.ApiClient;
import com.sellion.mobile.api.ApiService;
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.OrderEntity;
import com.sellion.mobile.entity.ReturnEntity;

import java.util.List;

import retrofit2.Response;

public class SyncWorker extends Worker {
    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        ApiService api = ApiClient.getClient().create(ApiService.class);

        // --- 1. ОТПРАВКА ЗАКАЗОВ ---
        List<OrderEntity> pendingOrders = db.orderDao().getPendingOrdersSync();
        if (!pendingOrders.isEmpty()) {
            try {
                Response<okhttp3.ResponseBody> response = api.sendOrders(pendingOrders).execute();
                if (response.isSuccessful()) {
                    db.orderDao().markAllAsSent();
                    Log.d("SYNC_DEBUG", "Заказы синхронизированы");
                }
            } catch (Exception e) {
                Log.e("SYNC_DEBUG", "Ошибка заказа: " + e.getMessage());
                return Result.retry();
            }
        }

        // --- 2. ОТПРАВКА ВОЗВРАТОВ (добавляем этот блок) ---
        List<ReturnEntity> pendingReturns = db.returnDao().getPendingReturnsSync();
        if (!pendingReturns.isEmpty()) {
            try {
                // Вызываем тот самый метод, который мы прописали в ApiService
                Response<okhttp3.ResponseBody> response = api.sendReturns(pendingReturns).execute();
                if (response.isSuccessful()) {
                    // Помечаем в Room как отправленные (убедитесь, что метод есть в ReturnDao)
                    db.returnDao().markAllAsSent();
                    Log.d("SYNC_DEBUG", "Возвраты синхронизированы");
                }
            } catch (Exception e) {
                Log.e("SYNC_DEBUG", "Ошибка возврата: " + e.getMessage());
                return Result.retry();
            }
        }

        return Result.success();
    }
}