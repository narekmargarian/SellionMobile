package com.sellion.mobile.converter;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sellion.mobile.entity.PaymentMethod;
import com.sellion.mobile.entity.ReturnReason;

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

    // --- ИСПРАВЛЕНО: Для Способа Оплаты (Enum) ---
    @TypeConverter
    public static String fromPaymentMethod(PaymentMethod method) {
        return method == null ? null : method.name();
    }

    @TypeConverter
    public static PaymentMethod toPaymentMethod(String value) {
        return value == null ? null : PaymentMethod.valueOf(value);
    }

    // --- ИСПРАВЛЕНО: Для Причины Возврата (Enum) ---
    @TypeConverter
    public static String fromReturnReason(ReturnReason reason) {
        return reason == null ? null : reason.name();
    }

    @TypeConverter
    public static ReturnReason toReturnReason(String value) {
        return value == null ? null : ReturnReason.valueOf(value);
    }
}