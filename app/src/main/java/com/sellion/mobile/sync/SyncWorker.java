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

import okhttp3.ResponseBody;
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
                Response<ResponseBody> response = api.sendOrders(pendingOrders).execute();
                if (response.isSuccessful()) {
                    db.orderDao().markAllAsSent();
                } else {
                    return Result.retry(); // Если сервер занят, попробуем позже
                }
            }

            // Отправка возвратов
            List<ReturnEntity> pendingReturns = db.returnDao().getPendingReturnsSync();
            if (!pendingReturns.isEmpty()) {
                Response<ResponseBody> response = api.sendReturns(pendingReturns).execute();
                if (response.isSuccessful()) {
                    db.returnDao().markAllAsSent();
                }
            }
            return Result.success();
        } catch (Exception e) {
            // Если нет интернета, WorkManager сам переназначит задачу
            return Result.retry();
        }
    }
}