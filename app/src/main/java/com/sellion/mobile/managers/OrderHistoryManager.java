package com.sellion.mobile.managers;

import com.sellion.mobile.entity.OrderModel;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryManager {
    private static OrderHistoryManager instance;
    private final List<OrderModel> orders = new ArrayList<>();

    private OrderHistoryManager() {
    }

    public static synchronized OrderHistoryManager getInstance() {
        if (instance == null) instance = new OrderHistoryManager();
        return instance;
    }

    // РАБОТА С ЗАКАЗАМИ
    public void addOrder(OrderModel order) {
        // Если такой магазин уже есть в заказах, удаляем старый (режим редактирования)
        orders.removeIf(o -> o.shopName.equals(order.shopName));
        orders.add(order);
    }

    public List<OrderModel> getOrders() {
        return orders;
    }

    public OrderModel getOrder(String shopName) {
        for (OrderModel o : orders) {
            if (o.shopName.equals(shopName)) return o;
        }
        return null;
    }


}

