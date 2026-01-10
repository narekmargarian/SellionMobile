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
import com.sellion.mobile.entity.CartManager;
import com.sellion.mobile.entity.ReturnModel;
import com.sellion.mobile.managers.ReturnHistoryManager;

import java.util.Map;


public class ReturnDetailsViewFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_return_details_view, container, false);
        String shopName = getArguments() != null ? getArguments().getString("order_shop_name") : "";

        TextView tvTitle = view.findViewById(R.id.RtvViewReturnTitle);
        TextView tvMethod = view.findViewById(R.id.tvViewReturnMethod);
        TextView tvDate = view.findViewById(R.id.tvViewReturnDate);
        TextView tvTotalSum = view.findViewById(R.id.RtvViewReturnTotalSum);
        RecyclerView rv = view.findViewById(R.id.RrvViewOrderItems);
        Button btnEdit = view.findViewById(R.id.RbtnEditThisReturn);
        ImageButton btnBack = view.findViewById(R.id.RbtnBackFromView);

        tvTitle.setText(shopName);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        // Поиск в списке возвратов
        ReturnModel returnOrder = ReturnHistoryManager.getInstance().getReturn(shopName);

        if (returnOrder != null) {
            // ВАЖНО: Создаем финальную копию
            final ReturnModel finalReturn = returnOrder;

            btnEdit.setOnClickListener(v -> {
                // 1. Загружаем товары в корзину
                CartManager.getInstance().clearCart();
                CartManager.getInstance().getCartItems().putAll(finalReturn.items);

                // 2. Открываем экран сборки
                ReturnDetailsFragment storeFrag = new ReturnDetailsFragment();
                Bundle b = new Bundle();
                b.putString("store_name", finalReturn.shopName);
                b.putBoolean("is_actually_return", true);

                // ПЕРЕДАЕМ ДАННЫЕ ВОЗВРАТА
                b.putString("edit_reason", finalReturn.returnReason);
                b.putString("edit_date", finalReturn.returnDate);

                storeFrag.setArguments(b);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, storeFrag)
                        .addToBackStack(null)
                        .commit();
            });

            double total = calculateTotal(returnOrder.items);

            // Устанавливаем результат в TextView
            tvTotalSum.setText(String.format("%,.0f ֏", total));
        }
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        return view;
    }

    private double calculateTotal(Map<String, Integer> items) {
        double total = 0;
        for (Map.Entry<String, Integer> e : items.entrySet()) total += (getPriceForProduct(e.getKey()) * e.getValue());
        return total;
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