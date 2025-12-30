package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.sellion.mobile.R;


public class DashboardFragment extends Fragment {

    public DashboardFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        TextView title = view.findViewById(R.id.managerTitle);

        // 1. Находим обе карточки
        MaterialCardView cardClients = view.findViewById(R.id.cardClients);
        MaterialCardView cardOrders = view.findViewById(R.id.cardOrders); // ДОБАВЛЕНО

        MaterialCardView cardDebts = view.findViewById(R.id.cardDebts);

        cardDebts.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DebtsFragment())
                    .addToBackStack(null)
                    .commit();
        });
        // Получаем ID менеджера
        if (getArguments() != null) {
            String managerId = getArguments().getString("MANAGER_ID");
            title.setText("Менеджер: " + managerId);
        }

        // 2. Обработка нажатия на КЛИЕНТЫ
        cardClients.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ClientsFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // 3. Обработка нажатия на ЗАКАЗЫ (ДОБАВЛЕНО)
        cardOrders.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new OrdersFragment()) // Переходим в список заказов
                    .addToBackStack(null)
                    .commit();
        });

        return view;




    }

}