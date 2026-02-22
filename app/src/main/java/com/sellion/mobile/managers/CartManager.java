package com.sellion.mobile.managers;

import android.content.Context;
import android.util.Log;

import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.CartEntity;
import com.sellion.mobile.entity.PaymentMethod;
import com.sellion.mobile.entity.ReturnReason;

import java.math.BigDecimal;
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
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    private String deliveryDate = "";
    private PaymentMethod paymentMethod = PaymentMethod.TRANSFER; // По умолчанию наличные
    private boolean isSeparateInvoice = false;
    private ReturnReason returnReason = ReturnReason.OTHER;
    private String returnDate = "";

    // --- НОВЫЕ ПОЛЯ ДЛЯ СКИДОК И АКЦИЙ ---
    private BigDecimal clientDefaultPercent = BigDecimal.ZERO; // Процент магазина
    private Long selectedPromoId = null; // ID выбранной акции
    private Map<Long, BigDecimal> appliedPromoItems = new HashMap<>(); // Скидки акции по ID товаров

    private CartManager(Context context) {
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

    // --- ЛОГИКА СКИДОК ---

    public void setClientDefaultPercent(BigDecimal percent) {
        this.clientDefaultPercent = (percent != null) ? percent : BigDecimal.ZERO;
    }

    public BigDecimal getClientDefaultPercent() {
        return clientDefaultPercent;
    }

    public void setPromo(Long promoId, Map<Long, BigDecimal> promoItems) {
        this.selectedPromoId = promoId;
        this.appliedPromoItems = (promoItems != null) ? promoItems : new HashMap<>();
    }

    public Long getSelectedPromoId() {
        return selectedPromoId;
    }

    public Map<Long, BigDecimal> getAppliedPromoItems() {
        return appliedPromoItems;
    }

    // --- РАБОТА С КОРЗИНОЙ ---

    public void addProduct(long productId, String itemName, int quantity, double price) {
        executor.execute(() -> {
            if (quantity <= 0) {
                db.cartDao().removeItemById(productId);
            } else {
                db.cartDao().addOrUpdate(new CartEntity(productId, itemName, quantity, price));
            }
        });
    }

    public Map<String, Integer> getCartItems() {
        Map<String, Integer> map = new HashMap<>();
        try {
            Future<List<CartEntity>> future = executor.submit(() -> db.cartDao().getCartItemsSync());
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
        deliveryDate = "";
        paymentMethod = PaymentMethod.CASH;
        isSeparateInvoice = false;
        returnReason = ReturnReason.OTHER;
        returnDate = "";

        // Очистка скидок
        clientDefaultPercent = BigDecimal.ZERO;
        selectedPromoId = null;
        appliedPromoItems.clear();
    }

    // --- ГЕТТЕРЫ И СЕТТЕРЫ ---
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