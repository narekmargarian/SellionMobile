package com.sellion.mobile.sync;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.sellion.mobile.api.ApiClient;
import com.sellion.mobile.api.ApiResponse;
import com.sellion.mobile.api.ApiService;
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.OrderEntity;
import com.sellion.mobile.entity.ReturnEntity;

import java.util.List;
import java.util.Map;

import retrofit2.Response;

public class SyncWorker extends Worker {
    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        ApiService api = ApiClient.getClient(getApplicationContext()).create(ApiService.class);

        try {
            // Отправка заказов
            List<OrderEntity> pendingOrders = db.orderDao().getPendingOrdersSync();
            if (!pendingOrders.isEmpty()) {
                Response<ApiResponse<Map<String, Object>>> response = api.sendOrders(pendingOrders).execute();
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> data = response.body().getData();
                    int errors = 0;
                    if (data != null && data.get("errors") instanceof Number) {
                        errors = ((Number) data.get("errors")).intValue();
                    }

                    if (errors == 0) {
                        db.orderDao().markAllAsSent();
                    } else {
                        Log.e("SyncWorker", "Сервер отклонил заказы. Ошибок: " + errors);
                        return Result.retry();
                    }
                } else {
                    return Result.retry();
                }
            }

            // Отправка возвратов
            List<ReturnEntity> pendingReturns = db.returnDao().getPendingReturnsSync();
            if (!pendingReturns.isEmpty()) {
                Response<ApiResponse<Map<String, Object>>> response = api.sendReturns(pendingReturns).execute();
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> data = response.body().getData();
                    int errors = 0;
                    if (data != null && data.get("errors") instanceof Number) {
                        errors = ((Number) data.get("errors")).intValue();
                    }

                    if (errors == 0) {
                        db.returnDao().markAllAsSent();
                    } else {
                        Log.e("SyncWorker", "Сервер отклонил возвраты. Ошибок: " + errors);
                        return Result.retry();
                    }
                } else {
                    return Result.retry();
                }
            }
            return Result.success();
        } catch (Exception e) {
            Log.e("SyncWorker", "Ошибка фоновой синхронизации: " + e.getMessage());
            return Result.retry();
        }
    }
}