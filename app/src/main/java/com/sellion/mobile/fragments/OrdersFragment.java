package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.sellion.mobile.R;
import com.sellion.mobile.adapters.DebtsAdapter;
import com.sellion.mobile.adapters.OrderAdapter;
import com.sellion.mobile.entity.DebtModel;
import com.sellion.mobile.entity.OrderModel;
import com.sellion.mobile.managers.OrderHistoryManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrdersFragment extends Fragment {

    private RecyclerView recyclerView;
    private OrderAdapter adapter; // Тип изменен на OrderAdapter

    public OrdersFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        // 1. Кнопки
        ImageButton btnFilterOrders = view.findViewById(R.id.btnFilterOrders);
        ImageButton btnBack = view.findViewById(R.id.btnBackOrders);
        ImageButton btnAddOrder = view.findViewById(R.id.btnAddOrder);

        // 2. Список (RecyclerView)
        recyclerView = view.findViewById(R.id.recyclerOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Загружаем список заказов при открытии
        updateOrdersList();

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        btnAddOrder.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CreateOrderFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnFilterOrders.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(getContext(), v);
            popup.getMenu().add("Выбрать дату");
            popup.getMenu().add("Фильтр: Только оплаченные");
            popup.getMenu().add("Фильтр: Все");

            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Выбрать дату")) {
                    showDatePicker();
                } else {
                    Toast.makeText(getContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
                }
                return true;
            });
            popup.show();
        });

        return view;
    }

    // ИСПРАВЛЕНО: Теперь работаем с OrderModel и OrderAdapter
    private void updateOrdersList() {
        // Получаем список объектов OrderModel из менеджера
        List<OrderModel> orders = OrderHistoryManager.getInstance().getSavedOrders();

        // Инициализируем адаптер с логикой клика
        adapter = new OrderAdapter(orders, order -> {
            // При нажатии вызываем наш метод просмотра заказа
            onOrderClick(order);
        });

        recyclerView.setAdapter(adapter);
    }

//
private void onOrderClick(OrderModel order) {
    // 1. Создаем фрагмент детального просмотра (вместо AlertDialog)
    OrderDetailsViewFragment fragment = new OrderDetailsViewFragment();

    // 2. Передаем данные о заказе
    Bundle args = new Bundle();
    args.putString("order_shop_name", order.shopName);
    fragment.setArguments(args);

    // 3. Совершаем переход на полный экран
    getParentFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null) // Чтобы кнопка "Назад" вернула нас к списку заказов
            .commit();
}

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Выберите дату заказа")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Toast.makeText(getContext(), "Дата выбрана: " + datePicker.getHeaderText(), Toast.LENGTH_SHORT).show();
        });

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }

    @Override
    public void onResume() {
        super.onResume();
        // Обновляем список каждый раз, когда возвращаемся на экран
        updateOrdersList();
    }
}