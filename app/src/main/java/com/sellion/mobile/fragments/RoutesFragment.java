package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;
import com.sellion.mobile.adapters.DebtsAdapter;
import com.sellion.mobile.entity.DebtModel;

import java.util.ArrayList;
import java.util.List;

public class RoutesFragment extends BaseFragment {

    private TextView tvCurrentDay;
    private RecyclerView recyclerView;
    private final String[] daysOfWeek = {"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_routes, container, false);

        tvCurrentDay = view.findViewById(R.id.tvCurrentDay);
        LinearLayout layoutSelectDay = view.findViewById(R.id.layoutSelectDay);

        recyclerView = view.findViewById(R.id.recyclerRoutes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Кнопка назад
        view.findViewById(R.id.btnBackRoutes).setOnClickListener(v -> setupBackButton(v, false));


        layoutSelectDay.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Выберите день недели")
                    .setItems(daysOfWeek, (dialog, which) -> {
                        String selectedDay = daysOfWeek[which];
                        tvCurrentDay.setText(selectedDay);
                        loadStoresForDay(selectedDay);
                    })
                    .show();
        });

        // Установка дня по умолчанию (2026 год)
        tvCurrentDay.setText("Вторник");
        loadStoresForDay("Вторник");

        return view;
    }

    private void loadStoresForDay(String day) {
        List<DebtModel> filteredList = new ArrayList<>();

        // Наполнение списка в зависимости от выбранного дня
        switch (day) {
            case "Понедельник":
                filteredList.add(new DebtModel("ZOVQ Arshakunyac", "ИП Акопян", "1122", "Комитаса 15", 0));
                filteredList.add(new DebtModel("ZOVQ Bagratunyac", "ИП Нарине", "3344", "Маштоца 20", 15000));
                break;
            case "Вторник":
                filteredList.add(new DebtModel("ZOVQ Qajaznuni", "ИП Карапетян", "5566", "Ширакаци 5", 45000));
                filteredList.add(new DebtModel("ZOVQ Gyurjyan", "ИП Саакян", "7788", "Абовяна 1", 0));
                filteredList.add(new DebtModel("Evrika", "ИП Саакян", "7788", "Абовяна 1", 0));
                filteredList.add(new DebtModel("MG Gyurjyan", "ИП Саакян", "7788", "Абовяна 1", 0));
                break;
            case "Среда":
                filteredList.add(new DebtModel("Carrefour", "ООО Фуд", "9900", "Тиграна Меца 4", 120000));
                break;
            case "Четверг":
                filteredList.add(new DebtModel("Evrika Улыбка", "ИП Варданян", "1212", "Пушкина 10", 5000));
                break;
            case "Пятница":
                filteredList.add(new DebtModel("Evrika 24/7", "ИП Оганесян", "3434", "Баграмяна 2", 0));
                break;
            case "Суббота":
                filteredList.add(new DebtModel("MG Маркет", "ИП Григорян", "5656", "Аван, 4-й квартал", 25000));
                break;
        }

        // 1. Создаем адаптер
        DebtsAdapter adapter = new DebtsAdapter(filteredList, store -> {
            // Логика перехода в детали магазина
            StoreDetailsFragment fragment = new StoreDetailsFragment();
            Bundle args = new Bundle();
            args.putString("store_name", store.getShopName());
            fragment.setArguments(args);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // 2. !!! САМАЯ ВАЖНАЯ СТРОКА ДЛЯ 2026 ГОДА !!!
        // Мы принудительно отключаем показ долгов для экрана "Маршруты"
        adapter.setShowDebtDetails(false);

        // 3. Устанавливаем адаптер в RecyclerView
        if (recyclerView != null) {
            recyclerView.setAdapter(adapter);
        }
    }
}