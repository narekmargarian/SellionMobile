package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.sellion.mobile.R;
import com.sellion.mobile.managers.SessionManager;


public class DashboardFragment extends BaseFragment {

    public DashboardFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        TextView title = view.findViewById(R.id.managerTitle);

        MaterialCardView cardClients = view.findViewById(R.id.cardClients);
        MaterialCardView cardOrders = view.findViewById(R.id.cardOrders);
        MaterialCardView cardDebts = view.findViewById(R.id.cardDebts);
        MaterialCardView cardSync = view.findViewById(R.id.cardSync);
        MaterialCardView cardRoutes = view.findViewById(R.id.cardRoutes); // теперь "Возврат"
        MaterialCardView cardCatalog = view.findViewById(R.id.cardCatalog);

        String managerId = SessionManager.getInstance().getManagerId();
        if (managerId != null) {
            title.setText("Менеджер: " + managerId);
        }

        cardClients.setOnClickListener(v -> openFragment(new ClientsFragment()));
        cardOrders.setOnClickListener(v -> openFragment(new OrdersFragment()));
        cardDebts.setOnClickListener(v -> openFragment(new DebtsFragment()));
        cardSync.setOnClickListener(v -> openFragment(new SyncFragment()));
        cardCatalog.setOnClickListener(v -> openFragment(new CatalogFragment()));

        // Карточка "Маршруты" теперь открывает "Возврат" или другую логику
        cardRoutes.setOnClickListener(v -> {
            // Заглушка: позже можно реализовать возврат
            Toast.makeText(getContext(), "Возврат пока не реализован", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    private void openFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}

