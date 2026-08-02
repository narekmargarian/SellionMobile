package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;
import com.sellion.mobile.adapters.OrderAdapter;
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.OrderEntity;

import java.util.ArrayList;
import java.util.List;

public class DebtDetailsFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_debt_details, container, false);

        TextView tvName = view.findViewById(R.id.tvDetailShopName);
        TextView tvOwner = view.findViewById(R.id.tvDetailOwnerName);
        TextView tvInn = view.findViewById(R.id.tvDetailINN);
        TextView tvAddress = view.findViewById(R.id.tvDetailAddress);
        TextView tvAmount = view.findViewById(R.id.tvDetailAmount);
        ImageButton btnBack = view.findViewById(R.id.btnBackToDebtsList);

        RecyclerView recyclerClientOrders = view.findViewById(R.id.recyclerClientOrders);
        if (recyclerClientOrders != null) {
            recyclerClientOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        final String shopName;

        if (getArguments() != null) {
            shopName = getArguments().getString("SHOP_NAME");
            tvName.setText(shopName);
            tvOwner.setText("Имя ИП/ООО: " + getArguments().getString("OWNER_NAME"));
            tvInn.setText("ИНН/ՀՎՀՀ: " + getArguments().getString("INN"));
            tvAddress.setText(getArguments().getString("ADDRESS"));

            double amount = getArguments().getDouble("AMOUNT");
            tvAmount.setText(formatSmart(amount) + " ֏");
        } else {
            shopName = "";
        }

        if (shopName != null && !shopName.isEmpty() && recyclerClientOrders != null) {
            AppDatabase.getInstance(requireContext()).orderDao().getAllOrdersLive()
                    .observe(getViewLifecycleOwner(), orders -> {
                        if (orders != null) {
                            List<OrderEntity> storeOrders = new ArrayList<>();
                            for (OrderEntity order : orders) {
                                if (order.shopName != null && order.shopName.trim().equalsIgnoreCase(shopName.trim())) {
                                    storeOrders.add(order);
                                }
                            }
                            OrderAdapter storeOrdersAdapter = new OrderAdapter(storeOrders, order -> {
                            });
                            recyclerClientOrders.setAdapter(storeOrdersAdapter);
                        }
                    });
        }

        setupBackButton(btnBack, false);
        return view;
    }
}