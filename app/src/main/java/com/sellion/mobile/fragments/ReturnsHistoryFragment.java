package com.sellion.mobile.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sellion.mobile.R;
import com.sellion.mobile.adapters.ReturnAdapter;
import com.sellion.mobile.entity.OrderModel;
import com.sellion.mobile.managers.OrderHistoryManager;

import java.util.ArrayList;
import java.util.List;


public class ReturnsHistoryFragment extends BaseFragment {

    private RecyclerView recyclerView;
    private ReturnAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Используем ваш XML fragment_orders
        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        // Исправляем работу с Toolbar и заголовком
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbarOrders);
        if (toolbar != null) {
            // Находим TextView внутри Toolbar (в вашем XML он второй по счету в RelativeLayout)
            for (int i = 0; i < toolbar.getChildCount(); i++) {
                View child = toolbar.getChildAt(i);
                if (child instanceof RelativeLayout) {
                    RelativeLayout rl = (RelativeLayout) child;
                    for (int j = 0; j < rl.getChildCount(); j++) {
                        if (rl.getChildAt(j) instanceof TextView) {
                            ((TextView) rl.getChildAt(j)).setText("Возвраты сегодня");
                        }
                    }
                }
            }
        }

        ImageButton btnBack = view.findViewById(R.id.btnBackOrders);
        ImageButton btnAdd = view.findViewById(R.id.btnAddOrder);
        recyclerView = view.findViewById(R.id.recyclerOrders);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        updateReturnsList();

        setupBackButton(btnBack, false);

        btnAdd.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CreateReturnFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void updateReturnsList() {
        // Здесь получаем заказы и фильтруем (нужно добавить поле isReturn в OrderModel)
        List<OrderModel> allOrders = OrderHistoryManager.getInstance().getSavedOrders();
        List<OrderModel> returnsOnly = new ArrayList<>();

        for (OrderModel o : allOrders) {
            // Для теста берем все, но в идеале: if (o.isReturn)
            returnsOnly.add(o);
        }

        adapter = new ReturnAdapter(returnsOnly, order -> openReturnDetailsView(order));
        recyclerView.setAdapter(adapter);
    }

    private void openReturnDetailsView(OrderModel order) {
        ReturnDetailsViewFragment fragment = new ReturnDetailsViewFragment();
        Bundle args = new Bundle();
        args.putString("order_shop_name", order.shopName);
        fragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateReturnsList();
    }
}