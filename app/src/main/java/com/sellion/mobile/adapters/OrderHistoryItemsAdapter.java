package com.sellion.mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;
import com.sellion.mobile.entity.OrderItemInfo;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
public class OrderHistoryItemsAdapter extends RecyclerView.Adapter<OrderHistoryItemsAdapter.ViewHolder> {

    private final List<OrderItemInfo> preparedItems;
    // Создаем форматтер внутри адаптера для независимости
    private final DecimalFormat smartFormat;

    public OrderHistoryItemsAdapter(List<OrderItemInfo> preparedItems) {
        this.preparedItems = preparedItems != null ? preparedItems : new ArrayList<>();

        // Настройка формата как в BaseFragment (2 знака, отсечение нулей)
        this.smartFormat = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));
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

        // 1. Расчет суммы строки с точностью до 2 знаков (согласно бэкенду)
        double rowTotal = Math.round((item.price * item.quantity) * 100.0) / 100.0;

        // 2. Формирование текста
        // Используем smartFormat вместо String.format
        String priceFormatted = smartFormat.format(rowTotal);

        String info = String.format(Locale.getDefault(), "%s — %d шт.", item.name, item.quantity);
        holder.tvName.setText(info + " (" + priceFormatted + " ֏)");

        // 3. Установка остатка
        if (holder.tvStock != null) {
            holder.tvStock.setVisibility(View.VISIBLE);
            holder.tvStock.setText("На складе: " + item.stock + " шт.");

            // Если товар со скидкой (есть скобка в имени), красим в зеленый
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
