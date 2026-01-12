package com.sellion.mobile.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.sellion.mobile.entity.OrderEntity;

import java.util.List;
import java.util.Map;

@Dao
public interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(OrderEntity order);

    @Query("UPDATE orders SET shopName = :shopName, items = :items, status = :status, deliveryDate = :deliveryDate, paymentMethod = :paymentMethod, needsSeparateInvoice = :needsSeparateInvoice, managerId = :managerId WHERE id = :orderId")
    void updateOrder(int orderId, String shopName, Map<String, Integer> items, String status, String deliveryDate, String paymentMethod, boolean needsSeparateInvoice, String managerId);
    @Query("SELECT * FROM orders ORDER BY id DESC")
    LiveData<List<OrderEntity>> getAllOrdersLive();

    @Query("SELECT * FROM orders WHERE status = 'PENDING'")
    List<OrderEntity> getPendingOrdersSync();

    @Query("UPDATE orders SET status = 'SENT' WHERE status = 'PENDING'")
    void markAllAsSent();

    @Query("DELETE FROM orders")
    void deleteAll();
}