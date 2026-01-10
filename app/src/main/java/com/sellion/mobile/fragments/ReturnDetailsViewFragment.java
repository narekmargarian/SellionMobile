package com.sellion.mobile.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sellion.mobile.R;
import com.sellion.mobile.adapters.OrderHistoryItemsAdapter;
import com.sellion.mobile.entity.CartManager;
import com.sellion.mobile.entity.ReturnModel;
import com.sellion.mobile.helper.NavigationHelper;
import com.sellion.mobile.managers.ReturnHistoryManager;
import com.sellion.mobile.managers.ReturnManager;

import java.util.Map;


public class ReturnDetailsViewFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_return_details_view, container, false);
        String name = getArguments() != null ? getArguments().getString("order_shop_name") : "";

        TextView tvTitle = v.findViewById(R.id.RtvViewReturnTitle);
        TextView tvReason = v.findViewById(R.id.tvViewReturnMethod);
        TextView tvDate = v.findViewById(R.id.tvViewReturnDate);
        TextView tvTotal = v.findViewById(R.id.RtvViewReturnTotalSum);
        RecyclerView rv = v.findViewById(R.id.RrvViewOrderItems);
        Button btnEdit = v.findViewById(R.id.RbtnEditThisReturn);
        ImageButton btnBack = v.findViewById(R.id.RbtnBackFromView);

        tvTitle.setText(name);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        ReturnModel rOrder = ReturnHistoryManager.getInstance().getReturn(name);
        if (rOrder != null) {
            tvReason.setText("Причина: " + rOrder.returnReason);
            tvDate.setText("Дата: " + rOrder.returnDate);

            // Список товаров (используем адаптер заказов)
            rv.setAdapter(new OrderHistoryItemsAdapter(rOrder.items));

            // Расчет итога
            double total = 0;
            for (Map.Entry<String, Integer> e : rOrder.items.entrySet()) {
                total += (e.getValue() * 500); // Замените 500 на getPriceForProduct если нужно
            }
            tvTotal.setText(String.format("Итоговая сумма: %,.0f ֏", total));

            btnEdit.setOnClickListener(view -> {
                CartManager.getInstance().clearCart();
                CartManager.getInstance().getCartItems().putAll(rOrder.items);
                ReturnManager.getInstance().setReturnReason(rOrder.returnReason);
                ReturnManager.getInstance().setReturnDate(rOrder.returnDate);

                ReturnDetailsFragment frag = new ReturnDetailsFragment();
                Bundle b = new Bundle(); b.putString("store_name", rOrder.shopName);
                frag.setArguments(b);
                NavigationHelper.openSection(getParentFragmentManager(), frag);
            });
        }
        btnBack.setOnClickListener(view -> getParentFragmentManager().popBackStack());
        return v;
    }

    private int getPriceForProduct(String name) {
        if (name == null) return 0;
        switch (name) {
            case "Шоколад Аленка": return 500;
            case "Шоколад 1": return 5574;
            case "Шоколад 2": return 45452;
            case "Шоколад 3": return 1212;
            case "Конфеты Мишка": return 2500;
            case "Вафли Артек": return 3500;
            case "Вафли 1": return 12560;
            case "Вафли 2": return 12121;
            case "Вафли 3": return 12;
            case "Lays 1": return 785;
            case "Lays 2": return 125;
            case "Lays Сметана/Зелень": return 10001;
            case "Pringles Оригинал": return 789;
            case "Pringles 1": return 123;
            case "Pringles 2": return 566;
            case "Чай 1": return 120;
            case "Чай 2": return 698;
            case "Чай 3": return 900;
            case "Чай Ахмад": return 1100;
            default: return 0;
        }
    }
}