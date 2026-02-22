package com.sellion.mobile.converter;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sellion.mobile.entity.PaymentMethod;
import com.sellion.mobile.entity.ReturnReason;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
public class Converters {
    private static final Gson gson = new Gson();

    // 1. Для товаров в корзине: ID товара (Long) -> Количество (Integer)
    @TypeConverter
    public static String fromMap(Map<Long, Integer> map) {
        return map == null ? null : gson.toJson(map);
    }

    @TypeConverter
    public static Map<Long, Integer> toMap(String data) {
        if (data == null) return new HashMap<>();
        Type type = new TypeToken<Map<Long, Integer>>() {}.getType();
        return gson.fromJson(data, type);
    }

    // 2. НОВОЕ: Для примененных акций: ID товара (Long) -> Процент скидки (BigDecimal)
    @TypeConverter
    public static String fromPromoMap(Map<Long, BigDecimal> map) {
        return map == null ? null : gson.toJson(map);
    }

    @TypeConverter
    public static Map<Long, BigDecimal> toPromoMap(String data) {
        if (data == null) return new HashMap<>();
        Type type = new TypeToken<Map<Long, BigDecimal>>() {}.getType();
        return gson.fromJson(data, type);
    }

    // 3. НОВОЕ: Для одиночных значений BigDecimal (используется внутри Map и может быть в полях)
    @TypeConverter
    public static String fromBigDecimal(BigDecimal value) {
        // toPlainString() — самый надежный способ сохранить 100% точность
        return value == null ? null : value.toPlainString();
    }

    @TypeConverter
    public static BigDecimal toBigDecimal(String value) {
        // Убираем проверку на пустоту или пробелы для стабильности
        if (value == null || value.isEmpty()) return BigDecimal.ZERO;
        return new BigDecimal(value);
    }

    // 4. Способы оплаты
    @TypeConverter
    public static String fromPaymentMethod(PaymentMethod method) {
        return method == null ? null : method.name();
    }

    @TypeConverter
    public static PaymentMethod toPaymentMethod(String method) {
        return method == null ? null : (method.isEmpty() ? PaymentMethod.CASH : PaymentMethod.valueOf(method));
    }

    // 5. Причины возврата
    @TypeConverter
    public static String fromReturnReason(ReturnReason reason) {
        return reason == null ? null : reason.name();
    }

    @TypeConverter
    public static ReturnReason toReturnReason(String reason) {
        return reason == null ? null : (reason.isEmpty() ? null : ReturnReason.valueOf(reason));
    }
}