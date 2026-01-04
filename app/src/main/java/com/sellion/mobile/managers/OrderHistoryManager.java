package com.sellion.mobile.managers;

import com.sellion.mobile.entity.OrderModel;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryManager {
    private static OrderHistoryManager instance;
    private final List<OrderModel> savedOrders = new ArrayList<>();

    public static OrderHistoryManager getInstance() {
        if (instance == null) instance = new OrderHistoryManager();
        return instance;
    }

    public void addOrder(OrderModel order) {
        // Если магазин уже есть (редактирование), заменяем его
        for (int i = 0; i < savedOrders.size(); i++) {
            if (savedOrders.get(i).shopName.equals(order.shopName)) {
                savedOrders.set(i, order);
                return;
            }
        }
        savedOrders.add(order);
    }

    public List<OrderModel> getSavedOrders() {
        return savedOrders;
    }
}

