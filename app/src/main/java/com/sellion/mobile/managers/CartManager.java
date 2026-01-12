package com.sellion.mobile.managers;

import android.content.Context;

import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.CartEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CartManager {
    private static CartManager instance;
    private final AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private String deliveryDate = "";
    private String paymentMethod = "Наличный расчет";
    private boolean isSeparateInvoice = false;

    private CartManager(Context context) {
        db = AppDatabase.getInstance(context);
    }

    public static synchronized CartManager init(Context context) {
        if (instance == null) instance = new CartManager(context);
        return instance;
    }

    public static CartManager getInstance() {
        return instance;
    }

    // ТЕПЕРЬ ПРИНИМАЕМ ЦЕНУ ИЗ PRODUCT
    public void addProduct(String itemName, int quantity, double price) {
        executor.execute(() -> {
            if (quantity <= 0) {
                db.cartDao().removeItem(itemName);
            } else {
                db.cartDao().addOrUpdate(new CartEntity(itemName, quantity, price));
            }
        });
    }

    public Map<String, Integer> getCartItems() {
        Map<String, Integer> map = new HashMap<>();
        try {
            Future<List<CartEntity>> future = executor.submit(() -> db.cartDao().getCartItemsSync());
            List<CartEntity> items = future.get();
            for (CartEntity item : items) {
                map.put(item.productName, item.quantity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public void clearCart() {
        executor.execute(() -> db.cartDao().clearCart());
        deliveryDate = "";
        paymentMethod = "Наличный расчет";
        isSeparateInvoice = false;
    }

    public String getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(String d) { this.deliveryDate = d; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String m) { this.paymentMethod = m; }
    public boolean isSeparateInvoice() { return isSeparateInvoice; }
    public void setSeparateInvoice(boolean s) { this.isSeparateInvoice = s; }
}