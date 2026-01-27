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
import java.util.Map;

public class OrderHistoryItemsAdapter extends RecyclerView.Adapter<OrderHistoryItemsAdapter.ViewHolder> {

    // 1. Модель данных уже подготовлена во Фрагменте
    private final List<OrderItemInfo> preparedItems;

    // ИСПРАВЛЕНО: Теперь конструктор принимает список готовых объектов
    public OrderHistoryItemsAdapter(List<OrderItemInfo> preparedItems) {
        this.preparedItems = preparedItems != null ? preparedItems : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Используем макет item_category (где tvCategoryName и tvStockQuantity)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Берем уже готовые данные
        OrderItemInfo item = preparedItems.get(position);

        // 2. ФОРМИРОВАНИЕ СУММЫ (100% сохранение твоей логики)
        double rowTotal = item.price * item.quantity;

        // Формат: Название — 5 шт. (2,500 ֏)
        String info = String.format("%s — %d шт. (%,.0f ֏)", item.name, item.quantity, rowTotal);
        String stockInfo = "Остаток: " + item.stock + " шт.";

        // 3. УСТАНОВКА В UI (Без потоков, мгновенно!)
        holder.tvName.setText(info);

        if (holder.tvStock != null) {
            holder.tvStock.setVisibility(View.VISIBLE);
            holder.tvStock.setText(stockInfo);
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


