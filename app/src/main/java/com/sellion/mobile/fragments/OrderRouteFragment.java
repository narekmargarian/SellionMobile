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

import java.util.Calendar;
import java.util.List;

public class OrderRouteFragment extends Fragment {
    private final String[] daysOfWeek = {"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"};
    private int selectedDayIndex; // Убрали инициализацию = 1
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

        // --- НОВАЯ ЛОГИКА ОПРЕДЕЛЕНИЯ ТЕКУЩЕГО ДНЯ (СЕГОДНЯ СУББОТА 10.01.2026) ---
        Calendar calendar = Calendar.getInstance();
        int dayOfWeekNum = calendar.get(Calendar.DAY_OF_WEEK); // В Java: 1=Вс, 2=Пн, 7=Сб

        // Определяем правильный индекс для нашего массива
        switch (dayOfWeekNum) {
            case Calendar.MONDAY:
                selectedDayIndex = 0;
                break;
            case Calendar.TUESDAY:
                selectedDayIndex = 1;
                break;
            case Calendar.WEDNESDAY:
                selectedDayIndex = 2;
                break;
            case Calendar.THURSDAY:
                selectedDayIndex = 3;
                break;
            case Calendar.FRIDAY:
                selectedDayIndex = 4;
                break;
            case Calendar.SATURDAY:
                selectedDayIndex = 5;
                break; // Сегодняшний день
            default:
                selectedDayIndex = 0; // Воскресенье или ошибка -> Понедельник
        }
        // --- КОНЕЦ НОВОЙ ЛОГИКИ ---

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
        // ... (остальной код метода без изменений) ...
        List<String> allClients = ClientManager.getInstance().getStoreNames();
        int start = Math.min(dayIndex * 8, allClients.size());
        int end = Math.min(start + 8, allClients.size());
        List<String> dayClients = allClients.subList(start, end);

        ClientAdapter adapter = new ClientAdapter(dayClients, name -> {
            Fragment parent = getParentFragment();
            if (parent instanceof CreateOrderFragment) {
                ((CreateOrderFragment) parent).onClientSelected(name);
            } else if (parent instanceof CreateReturnFragment) {
                ((CreateReturnFragment) parent).onClientSelected(name);
            }
        });
        recyclerView.setAdapter(adapter);
    }
}