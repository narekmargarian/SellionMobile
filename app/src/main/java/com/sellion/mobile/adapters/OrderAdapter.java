package com.sellion.mobile.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;
import com.sellion.mobile.entity.OrderModel;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private List<OrderModel> ordersList;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(OrderModel order);
    }

    public OrderAdapter(List<OrderModel> ordersList, OnOrderClickListener listener) {
        this.ordersList = ordersList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Используем вашу стандартную разметку item_category
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderModel order = ordersList.get(position);

        // Устанавливаем название магазина
        holder.tvName.setText(order.shopName);

        // ЛОГИКА ЦВЕТА СТАТУСА:
        if (order.status == OrderModel.Status.SENT) {
            // Зеленый цвет для отправленных заказов
            holder.tvName.setTextColor(Color.parseColor("#2E7D32"));
            holder.tvName.setText(order.shopName + " (Отправлен)");
        } else {
            // Синий цвет для тех, что сохранены локально (PENDING)
            holder.tvName.setTextColor(Color.BLUE);
            holder.tvName.setText(order.shopName + " (Ожидает)");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onOrderClick(order);
        });
    }

    @Override
    public int getItemCount() {
        return ordersList != null ? ordersList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}
