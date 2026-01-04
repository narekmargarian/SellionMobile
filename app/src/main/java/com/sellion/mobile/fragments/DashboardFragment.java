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
import com.sellion.mobile.managers.SessionManager;


public class DashboardFragment extends Fragment {

    public DashboardFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        TextView title = view.findViewById(R.id.managerTitle);

        // КАРТОЧКИ
        MaterialCardView cardClients = view.findViewById(R.id.cardClients);
        MaterialCardView cardOrders = view.findViewById(R.id.cardOrders);
        MaterialCardView cardDebts = view.findViewById(R.id.cardDebts);
        MaterialCardView cardSync = view.findViewById(R.id.cardSync);
        MaterialCardView cardRoutes = view.findViewById(R.id.cardRoutes);
        MaterialCardView cardCatalog = view.findViewById(R.id.cardCatalog);

        // ЛОГИКА ОТОБРАЖЕНИЯ ID МЕНЕДЖЕРА (ИСПРАВЛЕНО)
        // Теперь мы берем ID из сессии, он не пропадет при переходах
        String managerId = SessionManager.getInstance().getManagerId();
        if (managerId != null) {
            title.setText("Менеджер: " + managerId);
        }

        // НАВИГАЦИЯ
        cardCatalog.setOnClickListener(v -> openFragment(new CatalogFragment()));
        cardRoutes.setOnClickListener(v -> openFragment(new RoutesFragment()));
        cardSync.setOnClickListener(v -> openFragment(new SyncFragment()));
        cardDebts.setOnClickListener(v -> openFragment(new DebtsFragment()));
        cardClients.setOnClickListener(v -> openFragment(new ClientsFragment()));
        cardOrders.setOnClickListener(v -> openFragment(new OrdersFragment()));

        return view;
    }

    // Вспомогательный метод для чистоты кода
    private void openFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}

