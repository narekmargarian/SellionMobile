package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.sellion.mobile.R;
import com.sellion.mobile.entity.CartManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class OrderInfoFragment extends BaseFragment {
    private TextView tvDeliveryDate;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_info, container, false);

        tvDeliveryDate = view.findViewById(R.id.tvDeliveryDate);
        RadioGroup radioGroupPaymentMethod = view.findViewById(R.id.radioGroupPaymentMethod);
        CheckBox checkboxSeparateInvoice = view.findViewById(R.id.checkboxSeparateInvoice);
        LinearLayout layoutSelectDeliveryDate = view.findViewById(R.id.layoutSelectDeliveryDate);

        // --- ВОССТАНОВЛЕНИЕ ДАННЫХ ПРИ ВХОДЕ ---

        // 1. Проверяем дату
        String savedDate = CartManager.getInstance().getDeliveryDate();
        if (savedDate != null && !savedDate.isEmpty()) {
            tvDeliveryDate.setText(savedDate);
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            String defaultDate = dateFormat.format(calendar.getTime());
            tvDeliveryDate.setText(defaultDate);
            CartManager.getInstance().setDeliveryDate(defaultDate);
        }

        // 2. Проверяем оплату
        String savedPayment = CartManager.getInstance().getPaymentMethod();
        if ("Банковский перевод".equals(savedPayment)) {
            radioGroupPaymentMethod.check(R.id.radioTransfer);
        } else {
            radioGroupPaymentMethod.check(R.id.radioCash);
        }

        // 3. Проверяем чекбокс
        checkboxSeparateInvoice.setChecked(CartManager.getInstance().isSeparateInvoice());

        // --- СЛУШАТЕЛИ (ЗАПИСЬ ИЗМЕНЕНИЙ) ---

        layoutSelectDeliveryDate.setOnClickListener(v -> showDatePicker());

        checkboxSeparateInvoice.setOnCheckedChangeListener((button, isChecked) -> {
            CartManager.getInstance().setSeparateInvoice(isChecked);
        });

        radioGroupPaymentMethod.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton rb = group.findViewById(checkedId);
            if (rb != null) {
                CartManager.getInstance().setPaymentMethod(rb.getText().toString());
            }
        });

        return view;
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Выберите день доставки")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            String selectedDate = dateFormat.format(new Date(selection));
            tvDeliveryDate.setText(selectedDate);
            CartManager.getInstance().setDeliveryDate(selectedDate);
        });

        datePicker.show(getChildFragmentManager(), "DELIVERY_DATE_PICKER");
    }
}