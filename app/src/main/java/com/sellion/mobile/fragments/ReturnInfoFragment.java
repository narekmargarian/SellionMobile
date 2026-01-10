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
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.sellion.mobile.R;
import com.sellion.mobile.entity.ReturnReason;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class ReturnInfoFragment extends BaseFragment {
    private TextView tvDate;
    private TextView tvReasonValue;
    private ReturnReason selectedReason = ReturnReason.EXPIRED;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Используем ваш fragment_store_info
        View rootView = inflater.inflate(R.layout.fragment_return_info, container, false);

        tvDate = rootView.findViewById(R.id.tvReturnDate);
        LinearLayout layoutDate = rootView.findViewById(R.id.layoutSelectReturnDate);

        // Ставим 7 января 2026
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        tvDate.setText(dateFormat.format(cal.getTime()));

//        // Скрываем оплату (ID из вашего XML)
//        if (rootView.findViewById(R.id.radioGroupPaymentMethod) != null) {
//            rootView.findViewById(R.id.radioGroupPaymentMethod).setVisibility(View.GONE);
//        }
//        if (rootView.findViewById(R.id.checkboxSeparateInvoice) != null) {
//            rootView.findViewById(R.id.checkboxSeparateInvoice).setVisibility(View.GONE);
//        }

        // Находим заголовок программно, так как у него нет ID в вашем XML
        LinearLayout root = (LinearLayout) ((ScrollView) rootView).getChildAt(0);
        for (int i = 0; i < root.getChildCount(); i++) {
            if (root.getChildAt(i) instanceof TextView) {
                TextView tv = (TextView) root.getChildAt(i);
                if (tv.getText().toString().contains("Способ оплаты")) {
                    tv.setText("Причина возврата:");
                }
            }
        }

        // Создаем поле выбора причины
        tvReasonValue = new TextView(getContext());
        tvReasonValue.setText(selectedReason.getTitle());
        tvReasonValue.setTextColor(Color.parseColor("#2196F3"));


        // Вставляем после заголовка причины
        root.addView(tvReasonValue, 3);

        layoutDate.setOnClickListener(v -> showDatePicker());
        tvReasonValue.setOnClickListener(v -> showReasonDialog());

        return rootView;
    }

    private void showReasonDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Выберите причину")
                .setItems(ReturnReason.getAllTitles(), (d, i) -> {
                    selectedReason = ReturnReason.values()[i];
                    tvReasonValue.setText(selectedReason.getTitle());
                }).show();
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker().build();
        picker.addOnPositiveButtonClickListener(s -> tvDate.setText(dateFormat.format(new Date(s))));
        picker.show(getChildFragmentManager(), "DATE");
    }



    public String getSelectedReason() {
        if (tvReasonValue != null) {
            return tvReasonValue.getText().toString();
        }
        return selectedReason.getTitle();
    }

    public String getDeliveryDate() {
        return tvDate != null ? tvDate.getText().toString() : "";
    }
}