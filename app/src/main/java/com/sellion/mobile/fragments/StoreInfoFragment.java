package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.sellion.mobile.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class StoreInfoFragment extends Fragment {

    private TextView tvDeliveryDate;
    private RadioGroup radioGroupPaymentMethod;
    private CheckBox checkboxSeparateInvoice;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru")); // Формат даты на русском

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_store_info, container, false);

        tvDeliveryDate = view.findViewById(R.id.tvDeliveryDate);
        radioGroupPaymentMethod = view.findViewById(R.id.radioGroupPaymentMethod);
        checkboxSeparateInvoice = view.findViewById(R.id.checkboxSeparateInvoice);
        LinearLayout layoutSelectDeliveryDate = view.findViewById(R.id.layoutSelectDeliveryDate);

        // Устанавливаем завтрашнюю дату по умолчанию
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        tvDeliveryDate.setText(dateFormat.format(calendar.getTime()));

        // Обработка клика по дате доставки (открытие календаря)
        layoutSelectDeliveryDate.setOnClickListener(v -> showDatePicker());

        return view;
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Выберите день доставки")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            // Устанавливаем выбранную дату в TextView
            tvDeliveryDate.setText(dateFormat.format(selection));
        });

        datePicker.show(getParentFragmentManager(), "DELIVERY_DATE_PICKER");
    }

    // Метод, который вернет данные о заказе (для сохранения)
    public String getOrderDetails() {
        String paymentMethod;
        if (radioGroupPaymentMethod.getCheckedRadioButtonId() == R.id.radioCash) {
            paymentMethod = "Наличный расчет";
        } else {
            paymentMethod = "Банковский перевод";
        }

        boolean needsInvoice = checkboxSeparateInvoice.isChecked();
        String deliveryDate = tvDeliveryDate.getText().toString();

        return String.format("Дата: %s, Оплата: %s, Фактура: %b", deliveryDate, paymentMethod, needsInvoice);
    }
}