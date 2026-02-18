package com.sellion.mobile.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.sellion.mobile.entity.ClientEntity;

import java.util.List;

@Dao
public interface ClientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ClientEntity> clients);

    @Query("SELECT * FROM clients WHERE debt > 0")
    LiveData<List<ClientEntity>> getClientsWithDebtsLive();

    @Query("SELECT * FROM clients")
    List<ClientEntity> getAllClientsSync();

    @Query("DELETE FROM clients")
    void deleteAll();


    @Query("SELECT * FROM clients WHERE " +
            "(:inn IS NOT NULL AND inn = :inn) OR " +
            "(:name IS NOT NULL AND (name LIKE '%' || :name || '%' OR ownerName LIKE '%' || :name || '%'))")
    List<ClientEntity> searchClients(String inn, String name);


}