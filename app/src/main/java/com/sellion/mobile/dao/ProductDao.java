package com.sellion.mobile.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.sellion.mobile.entity.ProductEntity;

import java.util.List;

@Dao
public interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ProductEntity> products);

    @Query("SELECT * FROM products")
    List<ProductEntity> getAllProductsSync();

    @Query("SELECT price FROM products WHERE name = :productName LIMIT 1")
    double getPriceByName(String productName);

    // НОВЫЙ МЕТОД ДЛЯ ПРОВЕРКИ ОСТАТКА
    @Query("SELECT stockQuantity FROM products WHERE name = :productName LIMIT 1")
    int getStockByName(String productName);

    @Query("DELETE FROM products")
    void deleteAll();
}