package com.sellion.mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;
import com.sellion.mobile.database.AppDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderHistoryItemsAdapter extends RecyclerView.Adapter<OrderHistoryItemsAdapter.ViewHolder> {
    private final List<String> names;
    private final Map<String, Integer> items;

    public OrderHistoryItemsAdapter(Map<String, Integer> items) {
        this.items = items;
        // Если items придет null, создаем пустой список во избежание Crash
        this.names = (items != null) ? new ArrayList<>(items.keySet()) : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Используем тот же макет, что и для категорий/товаров
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = names.get(position);
        Integer qty = items.get(name);
        int currentQty = (qty != null) ? qty : 0;

        // В 2026 году мы не храним цены в коде, а берем их из БД
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(holder.itemView.getContext().getApplicationContext());
            // Получаем цену из созданного нами ProductDao
            double price = db.productDao().getPriceByName(name);
            double rowTotal = price * currentQty;

            // Форматируем строку: Товар — 5 шт. (2,500 ֏)
            String info = String.format("%s — %d шт. (%,.0f ֏)", name, currentQty, rowTotal);

            // Возвращаемся в главный поток для обновления текста
            holder.tvName.post(() -> holder.tvName.setText(info));
        }).start();
    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}