package com.sellion.mobile.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.sellion.mobile.converter.Converters;
import com.sellion.mobile.dao.CartDao;
import com.sellion.mobile.dao.ClientDao;
import com.sellion.mobile.dao.OrderDao;
import com.sellion.mobile.entity.CartEntity;
import com.sellion.mobile.entity.ClientEntity;
import com.sellion.mobile.entity.OrderEntity;
import com.sellion.mobile.entity.ReturnDao;
import com.sellion.mobile.entity.ReturnEntity;


@Database(entities = {OrderEntity.class, ReturnEntity.class, CartEntity.class, ClientEntity.class}, version = 2, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract OrderDao orderDao();

    public abstract ReturnDao returnDao();

    public abstract CartDao cartDao();
    public abstract ClientDao clientDao();

    private static volatile AppDatabase instance;

    public static AppDatabase getInstance(final Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "trading_db")
//                    instance = Room.databaseBuilder(context, AppDatabase.class, "trading_db_v2")
                            .fallbackToDestructiveMigration(true)
                            .build();
                }
            }
        }
        return instance;
    }
}