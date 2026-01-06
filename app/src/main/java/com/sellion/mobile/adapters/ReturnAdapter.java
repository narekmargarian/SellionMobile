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
import java.util.Map;

public class ReturnAdapter extends  RecyclerView.Adapter<ReturnAdapter.ReturnVH> {
    private List<OrderModel> list;
    private OnItemClickListener listener;

    public interface OnItemClickListener { void onItemClick(OrderModel order); }

    public ReturnAdapter(List<OrderModel> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull @Override
    public ReturnVH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        // Используем стандартный item_order или создайте простой макет
        View v = LayoutInflater.from(p.getContext()).inflate(android.R.layout.simple_list_item_2, p, false);
        return new ReturnVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReturnVH h, int p) {
        OrderModel o = list.get(p);
        h.text1.setText(o.shopName);
        h.text2.setText("Сумма возврата: " + o.paymentMethod); // Для примера
        h.itemView.setOnClickListener(v -> listener.onItemClick(o));
    }

    @Override public int getItemCount() { return list.size(); }

    static class ReturnVH extends RecyclerView.ViewHolder {
        TextView text1, text2;
        ReturnVH(View v) {
            super(v);
            text1 = v.findViewById(android.R.id.text1);
            text2 = v.findViewById(android.R.id.text2);
        }
    }
}