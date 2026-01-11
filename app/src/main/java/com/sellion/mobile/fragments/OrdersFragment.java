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
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.OrderEntity;

public class OrdersFragment extends BaseFragment {

    private RecyclerView recyclerView;
    private OrderAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        ImageButton btnBack = view.findViewById(R.id.btnBackOrders);
        ImageButton btnAddOrder = view.findViewById(R.id.btnAddOrder);
        ImageButton btnFilter = view.findViewById(R.id.btnFilterOrders);
        recyclerView = view.findViewById(R.id.recyclerOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Настройка заголовка Toolbar
        setupToolbarTitle(view);

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

        // Фильтрация
        btnFilter.setOnClickListener(this::showFilterMenu);

        // ВАЖНО: Запускаем наблюдение за базой данных (LiveData)
        updateOrdersList();

        return view;
    }

    private void updateOrdersList() {
        AppDatabase db = AppDatabase.getInstance(requireContext());

        // Наблюдаем за LiveData из Room
        db.orderDao().getAllOrdersLive().observe(getViewLifecycleOwner(), orderEntities -> {
            if (orderEntities != null) {
                // Передаем метод onOrderClick (который с OrderEntity) в адаптер
                adapter = new OrderAdapter(orderEntities, this::onOrderClick);
                recyclerView.setAdapter(adapter);
            }
        });
    }

    // ЕДИНСТВЕННЫЙ И ПРАВИЛЬНЫЙ МЕТОД КЛИКА
    private void onOrderClick(OrderEntity order) {
        OrderDetailsViewFragment fragment = new OrderDetailsViewFragment();
        Bundle args = new Bundle();

        // Передаем данные из Entity
        args.putString("order_shop_name", order.shopName);
        args.putInt("order_id", order.id);

        fragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void setupToolbarTitle(View view) {
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbarOrders);
        if (toolbar != null) {
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
        // В 2026 году при использовании LiveData вызов updateOrdersList() здесь
        // не обязателен, но оставлен для надежности фильтров.
        updateOrdersList();
    }
}