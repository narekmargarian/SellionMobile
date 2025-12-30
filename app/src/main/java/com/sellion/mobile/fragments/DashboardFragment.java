package com.sellion.mobile.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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

        // Получаем ID, который мы передали в Шаге 3
        if (getArguments() != null) {
            String managerId = getArguments().getString("MANAGER_ID");
            title.setText("Менеджер: " + managerId);
        }

        // Инициализируй свои карточки (cardOrders, cardSync и т.д.) здесь
        // ...

        return view;
    }
}