package com.sellion.mobile.managers;

import android.content.Context;
import android.util.Log;

import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.CartEntity;
import com.sellion.mobile.entity.PaymentMethod;
import com.sellion.mobile.entity.ReturnReason;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class CartManager {

    private static CartManager instance;
    private final AppDatabase db;
    // Ограничиваем количество потоков для экономии ресурсов
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    private String deliveryDate = "";
    private PaymentMethod paymentMethod = PaymentMethod.TRANSFER;
    private boolean isSeparateInvoice = false;
    private ReturnReason returnReason = ReturnReason.OTHER;
    private String returnDate = "";

    private CartManager(Context context) {
        // Используем getApplicationContext(), чтобы не держать Activity в памяти
        db = AppDatabase.getInstance(context.getApplicationContext());
    }

    public static synchronized CartManager init(Context context) {
        if (instance == null) instance = new CartManager(context);
        return instance;
    }

    public static CartManager getInstance() {
        if (instance == null) {
            Log.e("CartManager", "Instance is null! Вы забыли вызвать init() в HostActivity?");
        }
        return instance;
    }

    public void addProduct(long productId, String itemName, int quantity, double price) {
        executor.execute(() -> {
            if (quantity <= 0) {
                db.cartDao().removeItemById(productId);
            } else {
                db.cartDao().addOrUpdate(new CartEntity(productId, itemName, quantity, price));
            }
        });
    }

    // ИСПРАВЛЕНО: Вместо сложной логики Future лучше использовать LiveData напрямую в UI,
    // но для синхронного получения оставляем Future с ограничением по времени (защита от зависаний)
    public Map<String, Integer> getCartItems() {
        Map<String, Integer> map = new HashMap<>();
        try {
            Future<List<CartEntity>> future = executor.submit(() -> db.cartDao().getCartItemsSync());
            // Ждем максимум 2 секунды, чтобы не вешать приложение навсегда
            List<CartEntity> items = future.get(2, TimeUnit.SECONDS);
            if (items != null) {
                for (CartEntity item : items) {
                    map.put(item.productName, item.quantity);
                }
            }
        } catch (Exception e) {
            Log.e("CartManager", "Ошибка получения корзины: " + e.getMessage());
        }
        return map;
    }

    public void clearCart() {
        executor.execute(() -> db.cartDao().clearCart());
        // Сброс всех полей (ваша логика)
        deliveryDate = "";
        paymentMethod = PaymentMethod.TRANSFER;
        isSeparateInvoice = false;
        returnReason = ReturnReason.OTHER;
        returnDate = "";
    }

    // --- ГЕТТЕРЫ И СЕТТЕРЫ (Без изменений) ---
    public String getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(String d) { this.deliveryDate = d; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod m) { this.paymentMethod = m; }
    public boolean isSeparateInvoice() { return isSeparateInvoice; }
    public void setSeparateInvoice(boolean s) { this.isSeparateInvoice = s; }
    public ReturnReason getReturnReason() { return returnReason; }
    public void setReturnReason(ReturnReason reason) { this.returnReason = reason; }
    public String getReturnDate() { return returnDate; }
    public void setReturnDate(String date) { this.returnDate = date; }
}
