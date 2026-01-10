package com.sellion.mobile.entity;

import java.util.HashMap;
import java.util.Map;

public class CartManager {
    private static CartManager instance;

    // Товары: Название -> Количество
    private Map<String, Integer> cartItems = new HashMap<>();

    // Данные заказа
    private String deliveryDate = "";
    private String paymentMethod = "Наличный расчет";
    private boolean isSeparateInvoice = false;

    public static synchronized CartManager getInstance() {
        if (instance == null) instance = new CartManager();
        return instance;
    }

    // --- ПРОВЕРКА ДЛЯ АДАПТЕРА ---
    public boolean hasProduct(String productName) {
        return cartItems.containsKey(productName);
    }

    // --- РАБОТА С ТОВАРАМИ ---
    public Map<String, Integer> getCartItems() {
        return cartItems;
    }

    public void addProduct(String itemName, int quantity) {
        if (quantity <= 0) {
            cartItems.remove(itemName);
        } else {
            cartItems.put(itemName, quantity);
        }
    }

    // --- ДАННЫЕ ДОСТАВКИ ---
    public String getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(String date) { this.deliveryDate = date; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String method) { this.paymentMethod = method; }

    public boolean isSeparateInvoice() { return isSeparateInvoice; }
    public void setSeparateInvoice(boolean separate) { this.isSeparateInvoice = separate; }

    // --- ОЧИСТКА ---
    public void clearCart() {
        cartItems.clear();
        deliveryDate = "";
        paymentMethod = "Наличный расчет";
        isSeparateInvoice = false;
    }
}