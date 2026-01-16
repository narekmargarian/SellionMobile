package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.sellion.mobile.R;
import com.sellion.mobile.adapters.OrderAdapter;
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.OrderEntity;
import com.sellion.mobile.helper.NavigationHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class OrdersFragment extends BaseFragment {
    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private TextView tvTitle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        recyclerView = view.findViewById(R.id.recyclerOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Поиск TextView в Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbarOrders);
        if (toolbar != null && toolbar.getChildAt(0) instanceof RelativeLayout) {
            RelativeLayout rl = (RelativeLayout) toolbar.getChildAt(0);
            for (int i = 0; i < rl.getChildCount(); i++) {
                if (rl.getChildAt(i) instanceof TextView) {
                    tvTitle = (TextView) rl.getChildAt(i);
                    break;
                }
            }
        }

        view.findViewById(R.id.btnBackOrders).setOnClickListener(v -> NavigationHelper.backToDashboard(getParentFragmentManager()));
        view.findViewById(R.id.btnAddOrder).setOnClickListener(v ->
                getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, new CreateOrderFragment()).addToBackStack(null).commit()
        );

        view.findViewById(R.id.btnFilterOrders).setOnClickListener(this::showFilterMenu);

        // При входе показываем заказы за сегодня (2026 год)
        showOrdersToday();

        return view;
    }

    private void showOrdersToday() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        loadOrders(today + "T00:00:00", today + "T23:59:59", "Заказы сегодня");
    }

    private void showOrdersThisMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        String start = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime()) + "T00:00:00";

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        String end = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime()) + "T23:59:59";

        loadOrders(start, end, "Заказы за месяц");
    }

    private void showDateRangePicker() {
        MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Выберите период")
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection.first != null && selection.second != null) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String start = df.format(new Date(selection.first)) + "T00:00:00";
                String end = df.format(new Date(selection.second)) + "T23:59:59";
                loadOrders(start, end, "Период: " + df.format(new Date(selection.first)) + " - " + df.format(new Date(selection.second)));
            }
        });
        picker.show(getParentFragmentManager(), "RANGE_PICKER");
    }

    private void loadOrders(String start, String end, String title) {
        if (tvTitle != null) tvTitle.setText(title);
        AppDatabase.getInstance(requireContext()).orderDao()
                .getOrdersBetweenDates(start, end)
                .observe(getViewLifecycleOwner(), list -> {
                    adapter = new OrderAdapter(list, this::onOrderClick);
                    recyclerView.setAdapter(adapter);
                });
    }

    private void showFilterMenu(View v) {
        PopupMenu popup = new PopupMenu(requireContext(), v);
        popup.getMenu().add("За сегодня");
        popup.getMenu().add("За этот месяц");
        popup.getMenu().add("Выбрать период");
        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.equals("За сегодня")) showOrdersToday();
            else if (title.equals("За этот месяц")) showOrdersThisMonth();
            else if (title.equals("Выбрать период")) showDateRangePicker();
            return true;
        });
        popup.show();
    }

    private void onOrderClick(OrderEntity order) {
        OrderDetailsViewFragment fragment = new OrderDetailsViewFragment();
        Bundle args = new Bundle();
        args.putString("order_shop_name", order.shopName);
        args.putInt("order_id", order.id);
        fragment.setArguments(args);
        getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
    }
}