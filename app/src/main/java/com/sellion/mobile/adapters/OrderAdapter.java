package com.sellion.mobile.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.OrderEntity;

import java.util.List;
import java.util.Map;


public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderVH> {

    private List<OrderEntity> list; // Изменено на OrderEntity
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(OrderEntity o);
    }

    public OrderAdapter(List<OrderEntity> list, OnOrderClickListener l) {
        this.list = list;
        this.listener = l;
    }

    @NonNull
    @Override
    public OrderVH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_order, p, false);
        return new OrderVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderVH h, int pos) {
        OrderEntity o = list.get(pos);
        h.tvShopName.setText(o.shopName);

        // Используем уже сохраненную сумму в Entity, не мучаем БД в списке
        h.tvTotalAmount.setText(String.format("%,.0f ֏", o.totalAmount));

        if ("SENT".equals(o.status)) {
            h.tvStatus.setText("ОТПРАВЛЕН");
            h.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            h.tvStatus.setText("ОЖИДАЕТ");
            h.tvStatus.setTextColor(Color.parseColor("#2196F3"));
        }
        h.itemView.setOnClickListener(v -> listener.onOrderClick(o));
    }


    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class OrderVH extends RecyclerView.ViewHolder {
        TextView tvShopName, tvStatus, tvTotalAmount;

        public OrderVH(View v) {
            super(v);
            tvShopName = v.findViewById(R.id.tvOrderShopName);
            tvStatus = v.findViewById(R.id.tvOrderStatus);
            tvTotalAmount = v.findViewById(R.id.tvOrderTotalAmount);
        }
    }


}



