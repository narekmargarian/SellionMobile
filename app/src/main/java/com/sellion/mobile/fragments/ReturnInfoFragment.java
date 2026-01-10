package com.sellion.mobile.fragments;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.sellion.mobile.R;
import com.sellion.mobile.entity.ReturnReason;
import com.sellion.mobile.managers.ReturnManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class ReturnInfoFragment extends BaseFragment {
    private TextView tvDate;
    private RadioGroup rgReason;
    private final SimpleDateFormat df = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_return_info, container, false);
        tvDate = v.findViewById(R.id.tvReturnDate);
        rgReason = v.findViewById(R.id.radioGroupReturnReason);
        LinearLayout lDate = v.findViewById(R.id.layoutSelectReturnDate);

        // 1. Получаем дату из менеджера (там уже будет "завтра", если это новый возврат)
        String sDate = ReturnManager.getInstance().getReturnDate();
        tvDate.setText(sDate);

        // 2. Восстановление причины
        String sReason = ReturnManager.getInstance().getReturnReason();
        for (int i = 0; i < rgReason.getChildCount(); i++) {
            RadioButton rb = (RadioButton) rgReason.getChildAt(i);
            if (rb.getText().toString().equals(sReason)) {
                rb.setChecked(true);
                break;
            }
        }

        lDate.setOnClickListener(view -> showPicker());
        rgReason.setOnCheckedChangeListener((g, id) -> {
            RadioButton rb = g.findViewById(id);
            if (rb != null) ReturnManager.getInstance().setReturnReason(rb.getText().toString());
        });
        return v;
    }

    private void showPicker() {
        MaterialDatePicker<Long> dp = MaterialDatePicker.Builder.datePicker().build();
        dp.addOnPositiveButtonClickListener(sel -> {
            String selectedDate = df.format(new Date(sel));
            tvDate.setText(selectedDate);
            ReturnManager.getInstance().setReturnDate(selectedDate);
        });
        dp.show(getChildFragmentManager(), "DP");
    }
}