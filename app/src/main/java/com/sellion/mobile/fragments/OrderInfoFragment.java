package com.sellion.mobile.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.sellion.mobile.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class OrderInfoFragment extends BaseFragment {
    private TextView tvDate;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru"));
    private View rootView; // Сохраняем view для поиска чекбоксов
    private TextView tvDeliveryDate;
    private RadioGroup radioGroupPaymentMethod;
    private CheckBox checkboxSeparateInvoice;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_info, container, false);

        tvDeliveryDate = view.findViewById(R.id.tvDeliveryDate);
        radioGroupPaymentMethod = view.findViewById(R.id.radioGroupPaymentMethod);
        checkboxSeparateInvoice = view.findViewById(R.id.checkboxSeparateInvoice);
        LinearLayout layoutSelectDeliveryDate = view.findViewById(R.id.layoutSelectDeliveryDate);

        // Устанавливаем завтрашнюю дату по умолчанию
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        tvDeliveryDate.setText(dateFormat.format(calendar.getTime()));

        layoutSelectDeliveryDate.setOnClickListener(v -> showDatePicker());

        return view;
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Выберите день доставки")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            tvDeliveryDate.setText(dateFormat.format(new Date(selection)));
        });

        datePicker.show(getChildFragmentManager(), "DELIVERY_DATE_PICKER");
    }

    // Геттеры для получения данных во время сохранения заказа
    public String getSelectedPaymentMethod() {
        int checkedId = radioGroupPaymentMethod.getCheckedRadioButtonId();
        if (checkedId == R.id.radioCash) return "Наличный расчет";
        return "Банковский перевод";
    }

    public boolean isSeparateInvoiceRequired() {
        return checkboxSeparateInvoice.isChecked();
    }

    public String getDeliveryDate() {
        return tvDate != null ? tvDate.getText().toString() : "";
    }
}