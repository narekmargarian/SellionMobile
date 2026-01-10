package com.sellion.mobile.managers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ReturnManager {

    private static ReturnManager instance;
    private String reason = "Просрочка";
    private String date = "";

    public static synchronized ReturnManager getInstance() {
        if (instance == null) instance = new ReturnManager();
        return instance;
    }

    public String getReturnReason() {
        return reason;
    }

    public void setReturnReason(String r) {
        this.reason = r;
    }

    public String getReturnDate() {
        // Если дата пустая, генерируем "завтра"
        if (date.isEmpty()) {
            SimpleDateFormat df = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru"));
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 1); // +1 день
            date = df.format(cal.getTime());
        }
        return date;
    }

    public void setReturnDate(String d) {
        this.date = d;
    }

    public void clear() {
        reason = "Просрочка";
        date = ""; // Очищаем, чтобы в следующий раз снова сработало завтра
    }
}