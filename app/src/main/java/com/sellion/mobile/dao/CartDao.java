package com.sellion.mobile.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.sellion.mobile.entity.CartEntity;

import java.util.List;

@Dao
public interface CartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addOrUpdate(CartEntity item);

    @Query("SELECT * FROM cart")
    LiveData<List<CartEntity>> getCartItemsLive();

    @Query("SELECT * FROM cart")
    List<CartEntity> getCartItemsSync();

    @Query("DELETE FROM cart WHERE productName = :name")
    void removeItem(String name);

    @Query("DELETE FROM cart")
    void clearCart();
}