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
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.ClientEntity;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_route, container, false);
        tvCurrentDay = view.findViewById(R.id.OrderTvCurrentDay);
        recyclerView = view.findViewById(R.id.OrderRecyclerRoutes);
        LinearLayout selectDay = view.findViewById(R.id.OrderLayoutSelectDay);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Определение текущего дня
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

        // Загружаем из локальной БД
        loadRouteFromDb();

        selectDay.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle("День маршрута")
                .setItems(daysOfWeek, (dialog, which) -> {
                    selectedDayIndex = which;
                    tvCurrentDay.setText(daysOfWeek[which]);
                    loadRouteFromDb();
                }).show());

        return view;
    }

    private void loadRouteFromDb() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext().getApplicationContext());
            List<ClientEntity> allClients = db.clientDao().getAllClientsSync();
            String targetDay = daysOfWeek[selectedDayIndex];

            List<ClientModel> filteredList = new ArrayList<>();
            for (ClientEntity e : allClients) {
                if (e.routeDay != null && e.routeDay.trim().equalsIgnoreCase(targetDay.trim())) {
                    ClientModel m = new ClientModel();
                    m.id = e.id; m.name = e.name; m.address = e.address;
                    filteredList.add(m);
                }
            }

            requireActivity().runOnUiThread(() -> {
                ClientAdapter adapter = new ClientAdapter(filteredList, client -> {
                    Fragment parent = getParentFragment();
                    if (parent instanceof CreateOrderFragment) {
                        ((CreateOrderFragment) parent).onClientSelected(client.getName());
                    } else if (parent instanceof CreateReturnFragment) {
                        ((CreateReturnFragment) parent).onClientSelected(client.getName());
                    }
                });
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }
}