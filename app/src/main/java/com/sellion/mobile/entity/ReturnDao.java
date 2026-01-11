package com.sellion.mobile.entity;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ReturnDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ReturnEntity ret);

    @Query("SELECT * FROM returns ORDER BY id DESC")
    LiveData<List<ReturnEntity>> getAllReturnsLive();

    @Query("SELECT * FROM returns WHERE status = 'PENDING'")
    List<ReturnEntity> getPendingReturnsSync();

    @Query("UPDATE returns SET status = 'SENT' WHERE status = 'PENDING'")
    void markAllAsSent();

    @Query("DELETE FROM returns")
    void deleteAll();
}