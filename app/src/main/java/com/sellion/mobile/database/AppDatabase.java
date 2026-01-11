package com.sellion.mobile.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.sellion.mobile.converter.Converters;
import com.sellion.mobile.entity.CartDao;
import com.sellion.mobile.entity.CartEntity;
import com.sellion.mobile.entity.OrderDao;
import com.sellion.mobile.entity.OrderEntity;
import com.sellion.mobile.entity.ReturnDao;
import com.sellion.mobile.entity.ReturnEntity;


@Database(entities = {OrderEntity.class, ReturnEntity.class, CartEntity.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract OrderDao orderDao();
    public abstract ReturnDao returnDao();
    public abstract CartDao cartDao();

    private static volatile AppDatabase instance;

    public static AppDatabase getInstance(final Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "trading_db")
                            .fallbackToDestructiveMigration(true)
                            .build();
                }
            }
        }
        return instance;
    }
}