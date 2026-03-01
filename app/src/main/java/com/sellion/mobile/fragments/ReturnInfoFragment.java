package com.sellion.mobile.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.sellion.mobile.R;
import com.sellion.mobile.entity.ReturnReason;
import com.sellion.mobile.managers.CartManager;
import com.sellion.mobile.managers.ReturnManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class ReturnInfoFragment extends BaseFragment {
    private TextView tvReturnDate;

    // Формат для отображения в интерфейсе (красивый)
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru"));
    // Формат для сервера (стандарт ISO 8601), чтобы избежать ошибки JSON parse error в 2026 году
    private final SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_return_info, container, false);

        tvReturnDate = view.findViewById(R.id.tvReturnDate);
        RadioGroup radioGroupReason = view.findViewById(R.id.radioGroupReturnReason);
        LinearLayout layoutSelectDate = view.findViewById(R.id.layoutSelectReturnDate);

        // --- ЛОГИКА ДАТЫ ---
        String savedDate = CartManager.getInstance().getReturnDate();
        if (savedDate != null && !savedDate.isEmpty()) {
            try {
                Date date = serverFormat.parse(savedDate);
                tvReturnDate.setText(displayFormat.format(date));
            } catch (Exception e) {
                tvReturnDate.setText(savedDate);
            }
        } else {
            // Проверяем настройку рабочей недели (по умолчанию 5-дневка)
            SharedPreferences prefs = requireContext().getSharedPreferences("SyncSettings", Context.MODE_PRIVATE);
            boolean isSixDayWorkWeek = prefs.getBoolean("is_six_day_work", false);

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 1); // Берем завтра

            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

            if (isSixDayWorkWeek) {
                // Логика 6 дней: если завтра воскресенье -> перенос на понедельник
                if (dayOfWeek == Calendar.SUNDAY) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
            } else {
                // Логика 5 дней: если суббота или воскресенье -> перенос на понедельник
                if (dayOfWeek == Calendar.SATURDAY) {
                    calendar.add(Calendar.DAY_OF_MONTH, 2);
                } else if (dayOfWeek == Calendar.SUNDAY) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
            }

            String dateForServer = serverFormat.format(calendar.getTime());
            String dateForDisplay = displayFormat.format(calendar.getTime());

            tvReturnDate.setText(dateForDisplay);
            CartManager.getInstance().setReturnDate(dateForServer);
        }

        // --- ВОССТАНОВЛЕНИЕ ПРИЧИНЫ ---
        ReturnReason savedReason = CartManager.getInstance().getReturnReason();
        if (savedReason != null) {
            for (int i = 0; i < radioGroupReason.getChildCount(); i++) {
                View child = radioGroupReason.getChildAt(i);
                if (child instanceof RadioButton) {
                    RadioButton rb = (RadioButton) child;
                    if (rb.getText().toString().equals(savedReason.getTitle())) {
                        rb.setChecked(true);
                        break;
                    }
                }
            }
        }

        // --- СЛУШАТЕЛИ ---
        layoutSelectDate.setOnClickListener(v -> showDatePicker());

        radioGroupReason.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton rb = group.findViewById(checkedId);
            if (rb != null) {
                String text = rb.getText().toString();
                for (ReturnReason reason : ReturnReason.values()) {
                    if (reason.getTitle().equals(text)) {
                        CartManager.getInstance().setReturnReason(reason);
                        break;
                    }
                }
            }
        });

        return view;
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Выберите дату возврата")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);
            Date date = calendar.getTime();

            String dateForServer = serverFormat.format(date);
            CartManager.getInstance().setReturnDate(dateForServer);

            String dateForDisplay = displayFormat.format(date);
            tvReturnDate.setText(dateForDisplay);
        });

        datePicker.show(getChildFragmentManager(), "RETURN_DATE_PICKER");
    }
}