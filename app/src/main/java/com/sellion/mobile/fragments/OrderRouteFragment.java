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
import com.sellion.mobile.adapters.ClientAdapter;
import com.sellion.mobile.managers.ClientManager;

import java.util.List;

public class OrderRouteFragment extends Fragment {
    private final String[] daysOfWeek = {"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"};
    private int selectedDayIndex = 1;
    private RecyclerView recyclerView;
    private TextView tvCurrentDay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_route, container, false);
        tvCurrentDay = view.findViewById(R.id.OrderTvCurrentDay);
        recyclerView = view.findViewById(R.id.OrderRecyclerRoutes);
        LinearLayout selectDay = view.findViewById(R.id.OrderLayoutSelectDay);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tvCurrentDay.setText(daysOfWeek[selectedDayIndex]);
        loadClientsForDay(selectedDayIndex);

        selectDay.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle("День маршрута")
                .setItems(daysOfWeek, (dialog, which) -> {
                    selectedDayIndex = which;
                    tvCurrentDay.setText(daysOfWeek[which]);
                    loadClientsForDay(which);
                }).show());

        return view;
    }

    private void loadClientsForDay(int dayIndex) {
        List<String> allClients = ClientManager.getInstance().getStoreNames();
        int start = Math.min(dayIndex * 5, allClients.size());
        int end = Math.min(start + 5, allClients.size());
        List<String> dayClients = allClients.subList(start, end);

        ClientAdapter adapter = new ClientAdapter(dayClients, name -> {
            // Получаем родителя фрагмента (это будет CreateOrderFragment или CreateReturnFragment)
            Fragment parent = getParentFragment();

            if (parent instanceof CreateOrderFragment) {
                // Если открыли через "Заказ"
                ((CreateOrderFragment) parent).onClientSelected(name);
            } else if (parent instanceof CreateReturnFragment) {
                // Если открыли через "Возврат"
                ((CreateReturnFragment) parent).onClientSelected(name);
            }
        });
        recyclerView.setAdapter(adapter);
    }
}