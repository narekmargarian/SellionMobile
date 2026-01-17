package com.sellion.mobile.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.sellion.mobile.entity.ManagerEntity;

import java.util.List;

@Dao
public interface ManagerDao {
    @Query("SELECT id FROM managers")
    LiveData<List<String>> getAllManagersLive();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ManagerEntity> managers);

    @Query("DELETE FROM managers")
    void deleteAll();
}
