package com.sellion.mobile.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;
import com.sellion.mobile.activities.HostActivity;
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.OrderItemInfo;
import com.sellion.mobile.entity.ProductEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;public class OrderHistoryItemsAdapter extends RecyclerView.Adapter<OrderHistoryItemsAdapter.ViewHolder> {

    private final List<OrderItemInfo> preparedItems;

    public OrderHistoryItemsAdapter(List<OrderItemInfo> preparedItems) {
        this.preparedItems = preparedItems != null ? preparedItems : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItemInfo item = preparedItems.get(position);

        // 1. Расчет суммы строки с точностью до 0.1
        // (item.price уже должна быть округлена во фрагменте, но закрепим результат здесь)
        double rowTotal = Math.round((item.price * item.quantity) * 10.0) / 10.0;

        // 2. Формирование основной строки
        String info = String.format(Locale.getDefault(), "%s — %d шт.", item.name, item.quantity);

        // ИСПРАВЛЕНО: %,.1f вместо %,.0f для отображения копеек (например, 1 011.5 ֏)
        String priceFormatted = String.format(Locale.getDefault(), "%,.1f ֏", rowTotal);

        // Устанавливаем текст
        holder.tvName.setText(info + " (" + priceFormatted + ")");

        // 3. Установка остатка
        if (holder.tvStock != null) {
            holder.tvStock.setVisibility(View.VISIBLE);
            holder.tvStock.setText("На складе: " + item.stock + " шт.");

            if (item.name.contains("(-")) {
                holder.tvStock.setTextColor(android.graphics.Color.parseColor("#2E7D32"));
            } else {
                holder.tvStock.setTextColor(android.graphics.Color.GRAY);
            }
        }
    }

    @Override
    public int getItemCount() {
        return preparedItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStock;
        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            tvStock = itemView.findViewById(R.id.tvStockQuantity);
        }
    }
}



