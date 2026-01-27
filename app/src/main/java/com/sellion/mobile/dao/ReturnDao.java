package com.sellion.mobile.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.sellion.mobile.entity.ReturnEntity;

import java.util.List;

@Dao
public interface ReturnDao {
    // OnConflictStrategy.REPLACE — если ID уже есть в базе, Room обновит строку.
    // Если ID нет (равен 0), Room создаст новую запись.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ReturnEntity ret);

    @Query("SELECT * FROM returns ORDER BY id DESC")
    LiveData<List<ReturnEntity>> getAllReturnsLive();

    @Query("SELECT * FROM returns WHERE status = 'PENDING'")
    List<ReturnEntity> getPendingReturnsSync();

    @Query("UPDATE returns SET status = 'SENT' WHERE status = 'PENDING'")
    void markAllAsSent();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ReturnEntity> returns);
    @Query("DELETE FROM returns")
    void deleteAll();

    // Дополнительный метод для удаления конкретного возврата, если понадобится
    @Query("DELETE FROM returns WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT * FROM returns WHERE createdAt >= :start AND createdAt <= :end ORDER BY id DESC")
    LiveData<List<ReturnEntity>> getReturnsBetweenDates(String start, String end);

}