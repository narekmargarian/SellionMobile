package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.sellion.mobile.R;

public class OrdersFragment extends Fragment {


    public OrdersFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        ImageButton btnFilterOrders = view.findViewById(R.id.btnFilterOrders);
        ImageButton btnBack = view.findViewById(R.id.btnBackOrders);

        ImageButton btnAddOrder = view.findViewById(R.id.btnAddOrder);

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());


        btnAddOrder.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CreateOrderFragment())
                    .addToBackStack(null)
                    .commit();
        });


        // 3. СТАВИМ ТВОЙ КОД СЮДА (внутри onCreateView)
        btnFilterOrders.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(getContext(), v);
            popup.getMenu().add("Выбрать дату");
            popup.getMenu().add("Фильтр: Только оплаченные");
            popup.getMenu().add("Фильтр: Все");

            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Выбрать дату")) {
                    // Вызываем метод календаря
                    showDatePicker();
                } else {
                    Toast.makeText(getContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
                }
                return true;
            });
            popup.show();
        });

        return view;
    }

    // 4. МЕТОД showDatePicker ПИШЕМ ОТДЕЛЬНО (вне onCreateView, но внутри класса)
    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Выберите дату заказа")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds()) // Выбрать сегодняшнюю дату по умолчанию
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            // Здесь будет логика фильтрации списка по выбранной дате
            Toast.makeText(getContext(), "Дата выбрана: " + datePicker.getHeaderText(), Toast.LENGTH_SHORT).show();
        });

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }


}
