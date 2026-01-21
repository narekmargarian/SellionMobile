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

    // Поиск полной сущности по ID
    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    ProductEntity getProductById(long id);

    // Работа с ценами по ID (Приоритетно для 2026)
    @Query("SELECT price FROM products WHERE id = :productId LIMIT 1")
    double getPriceById(long productId);

    // Работа с остатками по ID
    @Query("SELECT stockQuantity FROM products WHERE id = :productId LIMIT 1")
    int getStockById(long productId);

    // --- МЕТОДЫ ДЛЯ СОВМЕСТИМОСТИ (по имени) ---

    @Query("SELECT price FROM products WHERE name = :productName LIMIT 1")
    double getPriceByName(String productName);

    @Query("SELECT stockQuantity FROM products WHERE name = :productName LIMIT 1")
    int getStockByName(String productName);

    // --- ОЧИСТКА ---

    @Query("DELETE FROM products")
    void deleteAll();
}
