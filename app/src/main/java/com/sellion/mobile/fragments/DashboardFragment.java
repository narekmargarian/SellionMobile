package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;
import com.sellion.mobile.R;
import com.sellion.mobile.helper.NavigationHelper;
import com.sellion.mobile.managers.SessionManager;


public class DashboardFragment extends BaseFragment {

    public DashboardFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Инициализация UI
        TextView title = view.findViewById(R.id.managerTitle);
        MaterialCardView cardClients = view.findViewById(R.id.cardClients);
        MaterialCardView cardOrders = view.findViewById(R.id.cardOrders);
        MaterialCardView cardDebts = view.findViewById(R.id.cardDebts);
        MaterialCardView cardSync = view.findViewById(R.id.cardSync);
        MaterialCardView cardReturn = view.findViewById(R.id.cardReturn);
        MaterialCardView cardCatalog = view.findViewById(R.id.cardCatalog);

        // Установка имени менеджера
        String managerId = SessionManager.getInstance().getManagerId();
        if (managerId != null) {
            title.setText("Менеджер: " + managerId);
        }

        // Использование NavigationHelper для всех переходов
        // Теперь кнопка "Назад" из любого раздела вернет строго сюда

        cardClients.setOnClickListener(v ->
                NavigationHelper.openSection(getParentFragmentManager(), new ClientsFragment()));

        cardOrders.setOnClickListener(v ->
                NavigationHelper.openSection(getParentFragmentManager(), new OrdersFragment()));

        cardDebts.setOnClickListener(v ->
                NavigationHelper.openSection(getParentFragmentManager(), new DebtsFragment()));

        cardSync.setOnClickListener(v ->
                NavigationHelper.openSection(getParentFragmentManager(), new SyncFragment()));

        cardCatalog.setOnClickListener(v ->
                NavigationHelper.openSection(getParentFragmentManager(), new CatalogFragment()));

        cardReturn.setOnClickListener(v ->
                NavigationHelper.openSection(getParentFragmentManager(), new ReturnsFragment()));

        return view;
    }
}