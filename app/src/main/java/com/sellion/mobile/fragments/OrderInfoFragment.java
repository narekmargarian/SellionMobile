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
import com.sellion.mobile.entity.PaymentMethod;
import com.sellion.mobile.managers.CartManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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

        // 2. ИСПРАВЛЕНО: Проверяем оплату через Enum
        PaymentMethod savedPayment = CartManager.getInstance().getPaymentMethod();
        if (savedPayment == PaymentMethod.TRANSFER) {
            radioGroupPaymentMethod.check(R.id.radioTransfer);
        } else {
            radioGroupPaymentMethod.check(R.id.radioCash);
        }

        // 3. Проверяем чекбокс
        checkboxSeparateInvoice.setChecked(CartManager.getInstance().isSeparateInvoice());

        // --- СЛУШАТЕЛИ ---

        layoutSelectDeliveryDate.setOnClickListener(v -> showDatePicker());

        checkboxSeparateInvoice.setOnCheckedChangeListener((button, isChecked) -> {
            CartManager.getInstance().setSeparateInvoice(isChecked);
        });

        // ИСПРАВЛЕНО: Сохраняем в CartManager объект Enum вместо String
        radioGroupPaymentMethod.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioTransfer) {
                CartManager.getInstance().setPaymentMethod(PaymentMethod.TRANSFER);
            } else if (checkedId == R.id.radioCash) {
                CartManager.getInstance().setPaymentMethod(PaymentMethod.CASH);
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
            // Используем UTC, чтобы избежать смещения даты из-за часовых поясов
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);
            String selectedDate = dateFormat.format(calendar.getTime());
            tvDeliveryDate.setText(selectedDate);
            CartManager.getInstance().setDeliveryDate(selectedDate);
        });

        datePicker.show(getChildFragmentManager(), "DELIVERY_DATE_PICKER");
    }
}
