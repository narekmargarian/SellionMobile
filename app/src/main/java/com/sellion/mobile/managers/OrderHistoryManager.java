package com.sellion.mobile.managers;

public class OrderHistoryManager {
//    private static OrderHistoryManager instance;
//    private final List<OrderEntity> orders = new ArrayList<>();
//
//    private OrderHistoryManager() {
//    }
//
//    public static synchronized OrderHistoryManager getInstance() {
//        if (instance == null) instance = new OrderHistoryManager();
//        return instance;
//    }
//
//    public void addOrder(OrderModel order) {
//        // ИСПРАВЛЕНО: Удаляем старую запись ТОЛЬКО если она еще не отправлена (редактирование черновика)
//        // Если заказ уже SENT, мы его не трогаем, а просто добавляем новый в список
//        orders.removeIf(o -> o.shopName.equals(order.shopName) && o.status == OrderModel.Status.PENDING);
//        orders.add(order);
//    }
//
//    public List<OrderModel> getOrders() {
//        return orders;
//    }
//
//    // ИСПРАВЛЕНО: Ищем самый последний (новый) заказ, просматривая список с конца
//    public OrderModel getOrder(String shopName) {
//        for (int i = orders.size() - 1; i >= 0; i--) {
//            OrderModel o = orders.get(i);
//            if (o.shopName.equals(shopName)) return o;
//        }
//        return null;
//    }
}
