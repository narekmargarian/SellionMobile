package com.sellion.mobile.fragments;

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


public class ReturnInfoFragment extends BaseFragment {
    private TextView tvReturnDate;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_return_info, container, false);

        tvReturnDate = view.findViewById(R.id.tvReturnDate);
        RadioGroup radioGroupReason = view.findViewById(R.id.radioGroupReturnReason);
        LinearLayout layoutSelectDate = view.findViewById(R.id.layoutSelectReturnDate);

        // --- ВАША ЛОГИКА (Дата на 1 день вперед) ---
        String savedDate = CartManager.getInstance().getReturnDate();
        if (savedDate != null && !savedDate.isEmpty()) {
            tvReturnDate.setText(savedDate);
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 1); // Устанавливаем ЗАВТРА
            String defaultDate = dateFormat.format(calendar.getTime());
            tvReturnDate.setText(defaultDate);
            CartManager.getInstance().setReturnDate(defaultDate);
        }

        // --- ВОССТАНОВЛЕНИЕ ПРИЧИНЫ ---
        ReturnReason savedReason = CartManager.getInstance().getReturnReason();
        if (savedReason != null) {
            for (int i = 0; i < radioGroupReason.getChildCount(); i++) {
                RadioButton rb = (RadioButton) radioGroupReason.getChildAt(i);
                // 2. Вызываем .getTitle(), чтобы получить строку для сравнения
                if (rb.getText().toString().equals(savedReason.getTitle())) {
                    rb.setChecked(true);
                    break;
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
                        // ПЕРЕДАЕМ ОБЪЕКТ reason (тип ReturnReason), а не строку
                        CartManager.getInstance().setReturnReason(reason);
                        break;
                    }
                }
            }
        });

        return view;
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
            String selectedDate = dateFormat.format(new Date(selection));
            tvReturnDate.setText(selectedDate);
            CartManager.getInstance().setReturnDate(selectedDate);
        });
        datePicker.show(getChildFragmentManager(), "DATE_PICKER");
    }
}