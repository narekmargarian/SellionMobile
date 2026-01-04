package com.sellion.mobile.entity;

import java.util.HashMap;
import java.util.Map;

public class CartManager { private static CartManager instance;
    // Храним товар и его количество
    private Map<String, Integer> cartItems = new HashMap<>();

    public static CartManager getInstance() {
        if (instance == null) instance = new CartManager();
        return instance;
    }

    public void addProduct(String productName, int quantity) {
        if (quantity > 0) cartItems.put(productName, quantity);
        else cartItems.remove(productName);
    }

    public boolean hasProduct(String productName) {
        return cartItems.containsKey(productName);
    }

    public Map<String, Integer> getCartItems() {
        return cartItems;
    }

    public void clearCart() {
        cartItems.clear();
    }
}