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

    @Query("SELECT * FROM orders ORDER BY id DESC")
    LiveData<List<OrderEntity>> getAllOrdersLive();

    @Query("SELECT * FROM orders WHERE status = 'PENDING'")
    List<OrderEntity> getPendingOrdersSync();

    @Query("UPDATE orders SET status = 'SENT' WHERE status = 'PENDING'")
    void markAllAsSent();

    @Query("DELETE FROM orders")
    void deleteAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<OrderEntity> orders);


    @Query("SELECT * FROM orders WHERE createdAt >= :start AND createdAt <= :end ORDER BY id DESC")
    LiveData<List<OrderEntity>> getOrdersBetweenDates(String start, String end);

}