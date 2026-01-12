package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;
import com.sellion.mobile.adapters.ClientAdapter;
import com.sellion.mobile.api.ApiClient;
import com.sellion.mobile.api.ApiService;
import com.sellion.mobile.model.ClientModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderRouteFragment extends Fragment {
    private final String[] daysOfWeek = {"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"};
    private int selectedDayIndex;
    private RecyclerView recyclerView;
    private TextView tvCurrentDay;
    private List<ClientModel> allClientsFromServer = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_route, container, false);
        tvCurrentDay = view.findViewById(R.id.OrderTvCurrentDay);
        recyclerView = view.findViewById(R.id.OrderRecyclerRoutes);
        LinearLayout selectDay = view.findViewById(R.id.OrderLayoutSelectDay);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Логика определения дня недели
        Calendar calendar = Calendar.getInstance();
        int dayOfWeekNum = calendar.get(Calendar.DAY_OF_WEEK);
        switch (dayOfWeekNum) {
            case Calendar.MONDAY: selectedDayIndex = 0; break;
            case Calendar.TUESDAY: selectedDayIndex = 1; break;
            case Calendar.WEDNESDAY: selectedDayIndex = 2; break;
            case Calendar.THURSDAY: selectedDayIndex = 3; break;
            case Calendar.FRIDAY: selectedDayIndex = 4; break;
            case Calendar.SATURDAY: selectedDayIndex = 5; break;
            default: selectedDayIndex = 0;
        }

        tvCurrentDay.setText(daysOfWeek[selectedDayIndex]);

        // Сначала загружаем ВСЕХ клиентов с сервера
        loadAllClients();

        selectDay.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle("День маршрута")
                .setItems(daysOfWeek, (dialog, which) -> {
                    selectedDayIndex = which;
                    tvCurrentDay.setText(daysOfWeek[which]);
                    filterClientsByDay(selectedDayIndex); // Фильтруем уже загруженные данные
                }).show());

        return view;
    }

    private void loadAllClients() {
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getClients().enqueue(new Callback<List<ClientModel>>() {
            @Override
            public void onResponse(Call<List<ClientModel>> call, Response<List<ClientModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allClientsFromServer = response.body();
                    filterClientsByDay(selectedDayIndex); // Показываем клиентов на текущий день
                }
            }

            @Override
            public void onFailure(Call<List<ClientModel>> call, Throwable t) {
                if (getContext() != null) Toast.makeText(getContext(), "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterClientsByDay(int dayIndex) {
        String targetDay = daysOfWeek[dayIndex];
        List<ClientModel> filteredList = new ArrayList<>();

        for (ClientModel client : allClientsFromServer) {
            // В 2026 году мы проверяем поле routeDay, которое пришло из MySQL
            if (targetDay.trim().equalsIgnoreCase(client.routeDay.trim())) {
                filteredList.add(client);
            }
        }

        // Исправление ошибки: передаем список объектов и достаем имя при клике
        ClientAdapter adapter = new ClientAdapter(filteredList, client -> {
            String name = client.getName(); // Теперь ошибки не будет

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