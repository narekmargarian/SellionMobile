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

    // Интерфейс для клика по заказу
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
        // Используем твою стандартную разметку элемента списка
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderModel order = ordersList.get(position);
        holder.tvName.setText(order.shopName);

        // ЛОГИКА ЦВЕТА 2026:
        if (order.status == OrderModel.Status.PENDING) {
            holder.tvName.setTextColor(Color.BLUE); // Синий - не отправлен
        } else if (order.status == OrderModel.Status.SENT) {
            holder.tvName.setTextColor(Color.parseColor("#2E7D32")); // Зеленый - отправлен
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
