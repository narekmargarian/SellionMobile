package com.sellion.mobile.converter;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

public class Converters {
    @TypeConverter
    public static Map<String, Integer> fromString(String value) {
        Type type = new TypeToken<Map<String, Integer>>() {}.getType();
        return new Gson().fromJson(value, type);
    }

    @TypeConverter
    public static String fromMap(Map<String, Integer> map) {
        return new Gson().toJson(map);
    }
}