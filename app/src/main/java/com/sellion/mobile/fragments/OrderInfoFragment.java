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

    // Форматы для 2026 года: красивый для экрана и ISO для сервера
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru"));
    private final SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_info, container, false);

        tvDeliveryDate = view.findViewById(R.id.tvDeliveryDate);
        RadioGroup radioGroupPaymentMethod = view.findViewById(R.id.radioGroupPaymentMethod);
        CheckBox checkboxSeparateInvoice = view.findViewById(R.id.checkboxSeparateInvoice);
        LinearLayout layoutSelectDeliveryDate = view.findViewById(R.id.layoutSelectDeliveryDate);

        // 1. ЛОГИКА ДАТЫ (Разделение экрана и сервера)
        String savedDate = CartManager.getInstance().getDeliveryDate(); // Ожидаем yyyy-MM-dd
        if (savedDate != null && !savedDate.isEmpty()) {
            try {
                // Парсим техническую дату для отображения
                Date date = serverFormat.parse(savedDate);
                tvDeliveryDate.setText(displayFormat.format(date));
            } catch (Exception e) {
                tvDeliveryDate.setText(savedDate);
            }
        } else {
            // По умолчанию ставим ЗАВТРА
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 1);

            String dateForServer = serverFormat.format(calendar.getTime());
            String dateForDisplay = displayFormat.format(calendar.getTime());

            tvDeliveryDate.setText(dateForDisplay);
            CartManager.getInstance().setDeliveryDate(dateForServer);
        }

        // 2. ВОССТАНОВЛЕНИЕ ОПЛАТЫ
        PaymentMethod savedPayment = CartManager.getInstance().getPaymentMethod();
        if (savedPayment == PaymentMethod.TRANSFER) {
            radioGroupPaymentMethod.check(R.id.radioTransfer);
        } else {
            radioGroupPaymentMethod.check(R.id.radioCash);
        }

        // 3. ВОССТАНОВЛЕНИЕ ЧЕКБОКСА
        checkboxSeparateInvoice.setChecked(CartManager.getInstance().isSeparateInvoice());

        // --- СЛУШАТЕЛИ ---

        layoutSelectDeliveryDate.setOnClickListener(v -> showDatePicker());

        checkboxSeparateInvoice.setOnCheckedChangeListener((button, isChecked) -> {
            CartManager.getInstance().setSeparateInvoice(isChecked);
        });

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
            Date date = calendar.getTime();

            // Сохраняем технический формат для сервера (2026-01-20)
            CartManager.getInstance().setDeliveryDate(serverFormat.format(date));

            // Показываем красивый формат пользователю (20 января 2026)
            tvDeliveryDate.setText(displayFormat.format(date));
        });

        datePicker.show(getChildFragmentManager(), "DELIVERY_DATE_PICKER");
    }
}