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


public class CreateOrderFragment extends Fragment {
    public CreateOrderFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_order, container, false);

        ImageButton btnBack = view.findViewById(R.id.btnBackToOrdersList);
        TabLayout tabLayout = view.findViewById(R.id.tabLayoutOrder);

        // Кнопка назад
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // СЛУШАТЕЛЬ ПЕРЕКЛЮЧЕНИЯ ВКЛАДОК
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    // Менеджер нажал "Маршрут"
                    loadRoute();
                } else {
                    // Менеджер нажал "Клиенты"
                    loadAllClients();
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // По умолчанию (при открытии) загружаем маршрут
        loadRoute();

        return view;
    }

    private void loadRoute() {
        // Здесь мы будем подгружать только магазины из плана на сегодня (30 декабря 2025)
        Toast.makeText(getContext(), "Загрузка маршрута на сегодня...", Toast.LENGTH_SHORT).show();
    }

    private void loadAllClients() {
        // Здесь мы будем подгружать ВСЕ магазины из базы данных Армении
        Toast.makeText(getContext(), "Показываем всех клиентов", Toast.LENGTH_SHORT).show();
    }
}