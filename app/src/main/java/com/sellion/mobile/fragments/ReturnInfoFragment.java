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
        String savedReason = CartManager.getInstance().getReturnReason();
        for (int i = 0; i < radioGroupReason.getChildCount(); i++) {
            RadioButton rb = (RadioButton) radioGroupReason.getChildAt(i);
            if (rb.getText().toString().equals(savedReason)) {
                rb.setChecked(true);
                break;
            }
        }

        // --- СЛУШАТЕЛИ ---
        layoutSelectDate.setOnClickListener(v -> showDatePicker());
        radioGroupReason.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton rb = group.findViewById(checkedId);
            if (rb != null) {
                CartManager.getInstance().setReturnReason(rb.getText().toString());
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