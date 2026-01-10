package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.sellion.mobile.R;
import com.sellion.mobile.adapters.OrderAdapter;
import com.sellion.mobile.entity.OrderModel;
import com.sellion.mobile.managers.OrderHistoryManager;

import java.util.List;

public class OrdersFragment extends BaseFragment {

    private RecyclerView recyclerView;
    private OrderAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        // 1. Инициализация кнопок

        ImageButton btnBack = view.findViewById(R.id.btnBackOrders);
        ImageButton btnAddOrder = view.findViewById(R.id.btnAddOrder);
        ImageButton btnFilter = view.findViewById(R.id.btnFilterOrders);
        recyclerView = view.findViewById(R.id.recyclerOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbarOrders);
        if (toolbar != null) {
            // Находим TextView внутри Toolbar (в вашем XML он второй по счету в RelativeLayout)
            for (int i = 0; i < toolbar.getChildCount(); i++) {
                View child = toolbar.getChildAt(i);
                if (child instanceof RelativeLayout) {
                    RelativeLayout rl = (RelativeLayout) child;
                    for (int j = 0; j < rl.getChildCount(); j++) {
                        if (rl.getChildAt(j) instanceof TextView) {
                            ((TextView) rl.getChildAt(j)).setText("Заказы сегодня");
                        }
                    }
                }
            }
        }


        // Настройка кнопки назад
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        setupBackButton(btnBack, false);

        // Кнопка создания нового заказа
        btnAddOrder.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CreateOrderFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Фильтрация (просто заглушка по вашему коду)
        btnFilter.setOnClickListener(this::showFilterMenu);

        return view;
    }

    private void updateOrdersList() {
        // ВАЖНО: Используем новый метод getOrders()
        List<OrderModel> orders = OrderHistoryManager.getInstance().getOrders();
        adapter = new OrderAdapter(orders, this::onOrderClick);
        recyclerView.setAdapter(adapter);
    }

    // ЛОГИКА КЛИКА: Сначала всегда открываем ПРОСМОТР
    private void onOrderClick(OrderModel order) {
        OrderDetailsViewFragment fragment = new OrderDetailsViewFragment();

        Bundle args = new Bundle();
        args.putString("order_shop_name", order.shopName);
        // Передаем ID или позицию, если нужно для точного поиска
        fragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void showFilterMenu(View v) {
        PopupMenu popup = new PopupMenu(requireContext(), v);
        popup.getMenu().add("Выбрать дату");
        popup.getMenu().add("Все");
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Выбрать дату")) {
                showDatePicker();
            } else {
                updateOrdersList();
            }
            return true;
        });
        popup.show();
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Заказы за дату")
                .build();
        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }


    @Override
    public void onResume() {
        super.onResume();
        // Обновляем список каждый раз при возврате на экран (например, после редактирования)
        updateOrdersList();
    }
}