package com.sellion.mobile.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.sellion.mobile.R;


public class NewOrderFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_order, container, false);

        ImageButton btnBack = view.findViewById(R.id.btnBackToOrders);
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);

        // Кнопка назад
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Логика переключения вкладок
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    // Вкладка "Маршрут"
                    loadRouteData();
                } else {
                    // Вкладка "Клиенты"
                    loadAllClientsData();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // По умолчанию загружаем маршрут
        loadRouteData();

        return view;
    }

    private void loadRouteData() {
        // Здесь будет код загрузки магазинов на сегодня
        Toast.makeText(getContext(), "Показываем маршрут на сегодня", Toast.LENGTH_SHORT).show();
    }

    private void loadAllClientsData() {
        // Здесь будет код загрузки всех магазинов Армении
        Toast.makeText(getContext(), "Показываем всех клиентов", Toast.LENGTH_SHORT).show();
    }
}