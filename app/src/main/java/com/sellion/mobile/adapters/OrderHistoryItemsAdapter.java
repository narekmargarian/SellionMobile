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
            case "Конфеты Мишка":
                return 2500;
            case "Вафли Артек":
                return 3500;
            case "Lays Сметана/Зелень":
                return 785;
            case "Pringles Оригинал":
                return 789;
            case "Чай Гринфилд":
                return 900;
            case "Чай Ахмад":
                return 1100;
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