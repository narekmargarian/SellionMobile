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
import java.util.Map;
public class OrderHistoryItemsAdapter extends RecyclerView.Adapter<OrderHistoryItemsAdapter.ViewHolder> {

    private final List<OrderItemInfo> preparedItems;

    public OrderHistoryItemsAdapter(List<OrderItemInfo> preparedItems) {
        this.preparedItems = preparedItems != null ? preparedItems : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Используем твой стандартный макет для элементов списка
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItemInfo item = preparedItems.get(position);

        // 1. Расчет суммы строки (item.price уже пришла со скидкой из фрагмента)
        double rowTotal = item.price * item.quantity;

        // 2. Формирование основной строки
        // Если в item.name есть "(-", значит там акция, подсветим это визуально в будущем если нужно
        String info = String.format(Locale.getDefault(), "%s — %d шт.", item.name, item.quantity);

        // Форматируем цену отдельно для красоты
        String priceFormatted = String.format(Locale.getDefault(), "%,.0f ֏", rowTotal);

        // Устанавливаем текст: "Название (-10%) — 5 шт. (5,000 ֏)"
        holder.tvName.setText(info + " (" + priceFormatted + ")");

        // 3. Установка остатка (информативно для истории)
        if (holder.tvStock != null) {
            holder.tvStock.setVisibility(View.VISIBLE);
            holder.tvStock.setText("На складе: " + item.stock + " шт.");

            // Если есть пометка о скидке в названии, можно выделить текст остатка цветом
            if (item.name.contains("(-")) {
                holder.tvStock.setTextColor(android.graphics.Color.parseColor("#2E7D32")); // Зеленый для акционных
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


