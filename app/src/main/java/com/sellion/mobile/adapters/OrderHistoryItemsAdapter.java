package com.sellion.mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderHistoryItemsAdapter extends RecyclerView.Adapter<OrderHistoryItemsAdapter.ViewHolder> {
    private final List<String> names;
    private final Map<String, Integer> items;

    public OrderHistoryItemsAdapter(Map<String, Integer> items) {
        this.items = items;
        this.names = new ArrayList<>(items.keySet());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = names.get(position);
        Integer qty = items.get(name);
        int currentQty = (qty != null) ? qty : 0;

        // Расчет суммы для конкретной строки
        int price = getPriceForProduct(name);
        double rowTotal = price * currentQty;

        // Формат: Товар — 5 шт. (2,500 ֏)
        String info = String.format("%s — %d шт. (%,.0f ֏)", name, currentQty, rowTotal);
        holder.tvName.setText(info);
    }

    private int getPriceForProduct(String name) {
        if (name == null) return 0;
        switch (name) {
            case "Шоколад Аленка":
                return 500;
            case "Шоколад 1":
                return 1000;
            case "Шоколад 2":
                return 1500;
            case "Шоколад 3":
                return 1250;
            case "Конфеты Мишка":
                return 1790;
            case "Вафли Артек":
                return 960;
            case "Вафли 1":
                return 630;
            case "Вафли 2":
                return 2560;
            case "Вафли 3":
                return 2430;
            case "Lays 1":
                return 1020;
            case "Lays 2":
                return 4450;
            case "Lays Сметана/Зелень":
                return 440;
            case "Pringles Оригинал":
                return 750;
            case "Pringles 1":
                return 3390;
            case "Pringles 2":
                return 890;
            case "Чай 1":
                return 220;
            case "Чай 2":
                return 9530;
            case "Чай 3":
                return 1990;
            case "Чай Ахмад":
                return 50000000;
            default:
                return 0;
        }
    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}