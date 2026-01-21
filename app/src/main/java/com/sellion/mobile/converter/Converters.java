package com.sellion.mobile.converter;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sellion.mobile.entity.PaymentMethod;
import com.sellion.mobile.entity.ReturnReason;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class Converters {
    private static final Gson gson = new Gson();

    @TypeConverter
    public static String fromMap(Map<Long, Integer> map) {
        return map == null ? null : gson.toJson(map);
    }

    @TypeConverter
    public static Map<Long, Integer> toMap(String data) {
        if (data == null) return new HashMap<>();
        Type listType = new TypeToken<Map<Long, Integer>>() {}.getType();
        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String fromPaymentMethod(PaymentMethod method) {
        return method == null ? null : method.name();
    }

    @TypeConverter
    public static PaymentMethod toPaymentMethod(String method) {
        return method == null ? null : PaymentMethod.valueOf(method);
    }

    @TypeConverter
    public static String fromReturnReason(ReturnReason reason) {
        return reason == null ? null : reason.name();
    }

    @TypeConverter
    public static ReturnReason toReturnReason(String reason) {
        return reason == null ? null : ReturnReason.valueOf(reason);
    }
}
