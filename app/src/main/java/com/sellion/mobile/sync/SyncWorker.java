package com.sellion.mobile.sync;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.OrderEntity;
import com.sellion.mobile.entity.ReturnEntity;

import java.util.List;

public class SyncWorker extends Worker {
    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());

        // 1. Получаем неотправленные данные
        List<OrderEntity> pendingOrders = db.orderDao().getPendingOrdersSync();
        List<ReturnEntity> pendingReturns = db.returnDao().getPendingReturnsSync();

        if (pendingOrders.isEmpty() && pendingReturns.isEmpty()) {
            return Result.success();
        }

        try {
            // 2. Имитация REST запроса к Spring Boot
            // В реальности здесь: Retrofit.send(pendingOrders); Retrofit.send(pendingReturns);
            Thread.sleep(2500);

            // 3. После успешного ответа сервера меняем статусы в БД
            db.orderDao().markAllAsSent();
            db.returnDao().markAllAsSent();

            return Result.success();
        } catch (Exception e) {
            // Если произошла ошибка сети, WorkManager перезапустит задачу позже
            return Result.retry();
        }
    }
}