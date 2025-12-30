package com.sellion.mobile.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sellion.mobile.R;

public class RoutesFragment extends Fragment {

    private TextView tvCurrentDay;
    private final String[] daysOfWeek = {"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_routes, container, false);

        tvCurrentDay = view.findViewById(R.id.tvCurrentDay);
        LinearLayout layoutSelectDay = view.findViewById(R.id.layoutSelectDay);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerRoutes);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Кнопка назад
        view.findViewById(R.id.btnBackRoutes).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Нажатие на день недели по центру
        layoutSelectDay.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Выберите день недели")
                    .setItems(daysOfWeek, (dialog, which) -> {
                        String selectedDay = daysOfWeek[which];
                        tvCurrentDay.setText(selectedDay);
                        loadStoresForDay(selectedDay);
                    })
                    .show();
        });

        // По умолчанию загружаем магазины для текущего дня (сегодня вторник, 30 дек 2025)
        tvCurrentDay.setText("Вторник");
        loadStoresForDay("Вторник");

        return view;
    }

    private void loadStoresForDay(String day) {
        // Здесь будет логика фильтрации списка из базы
        Toast.makeText(getContext(), "Загрузка магазинов на: " + day, Toast.LENGTH_SHORT).show();

        // Например:
        // if(day.equals("Вторник")) { показать Магазин Ани, Магазин Ашот... }
    }
}