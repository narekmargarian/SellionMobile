package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.sellion.mobile.R;
import com.sellion.mobile.adapters.DebtsAdapter;
import com.sellion.mobile.entity.DebtModel;

import java.util.ArrayList;
import java.util.List;


public class CreateOrderFragment extends Fragment {
    public CreateOrderFragment() {
    }

    private RecyclerView recyclerView; // Добавь переменную

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_order, container, false);

        ImageButton btnBack = view.findViewById(R.id.btnBackToOrdersList);
        TabLayout tabLayout = view.findViewById(R.id.tabLayoutOrder);

        // Находим RecyclerView
        recyclerView = view.findViewById(R.id.recyclerOrderSelection);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) loadRoute();
                else loadAllClients();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        loadRoute(); // Загружаем при старте
        return view;
    }

    private void loadRoute() {
        // Создаем список магазинов на сегодня
        List<DebtModel> routeList = new ArrayList<>();
        routeList.add(new DebtModel("ZOVQ Arshakunyac", "ИП Акопян", "1122", "Комитаса 15", 0));
        routeList.add(new DebtModel("ZOVQ Bagratunyac", "ИП Нарине", "3344", "Маштоца 20", 15000));

        // Настраиваем адаптер с переходом в детали магазина
        DebtsAdapter adapter = new DebtsAdapter(routeList, store -> {
            // ПЕРЕХОД В ДЕТАЛИ МАГАЗИНА (Товары, Заказ, О заказе)
            openStoreDetails(store.getShopName());
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadAllClients() {
        // Здесь можно добавить другой список (всех клиентов)
        List<DebtModel> allClients = new ArrayList<>();
        allClients.add(new DebtModel("Магазин Армения", "ООО Тест", "0000", "Ереван", 0));

        DebtsAdapter adapter = new DebtsAdapter(allClients, store -> openStoreDetails(store.getShopName()));
        recyclerView.setAdapter(adapter);
    }

    private void openStoreDetails(String storeName) {
        // Создаем экземпляр фрагмента
        StoreDetailsFragment fragment = new StoreDetailsFragment();

        // Передаем данные
        Bundle args = new Bundle();
        args.putString("store_name", storeName);
        fragment.setArguments(args);

        // ВАЖНО: Используем транзакцию правильно
        if (isAdded() && getActivity() != null) { // Проверка, что фрагмент "жив"
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out) // Добавим анимацию для плавности
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}