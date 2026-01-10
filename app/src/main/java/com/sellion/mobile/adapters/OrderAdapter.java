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

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ReturnVH> {

    private List<OrderModel> list;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(OrderModel o);
    }

    public OrderAdapter(List<OrderModel> list, OnOrderClickListener l) {
        this.list = list;
        this.listener = l;
    }

    @NonNull
    @Override
    public ReturnVH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_order, p, false);
        return new ReturnVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReturnVH h, int pos) {
        OrderModel o = list.get(pos);

        h.tvShopName.setText(o.shopName);
        // Расчет итоговой суммы
        double total = 0;
        if (o.items != null) {
            for (Map.Entry<String, Integer> entry : o.items.entrySet()) {
                total += (entry.getValue() * getPriceForProduct(entry.getKey()));
            }
        }
        h.tvTotalAmount.setText(String.format("%,.0f ֏", total));

        // Логика статуса
        if (o.status == OrderModel.Status.SENT) {
            h.tvStatus.setText("ОТПРАВЛЕН");
            h.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            h.tvStatus.setText("ОЖИДАЕТ");
            h.tvStatus.setTextColor(Color.parseColor("#2196F3"));
        }


        h.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(o);
            }
        });


    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ReturnVH extends RecyclerView.ViewHolder {
        TextView tvShopName, tvStatus, tvTotalAmount;

        public ReturnVH(View v) {
            super(v);
            tvShopName = v.findViewById(R.id.tvOrderShopName);
            tvStatus = v.findViewById(R.id.tvOrderStatus);
            tvTotalAmount = v.findViewById(R.id.tvOrderTotalAmount);
        }
    }

    private int getPriceForProduct(String name) {
        if (name == null) return 0;
        switch (name) {
            case "Шоколад Аленка":
                return 500;
            case "Шоколад 1":
                return 5574;
            case "Шоколад 2":
                return 45452;
            case "Шоколад 3":
                return 1212;
            case "Конфеты Мишка":
                return 2500;
            case "Вафли Артек":
                return 3500;
            case "Вафли 1":
                return 12560;
            case "Вафли 2":
                return 12121;
            case "Вафли 3":
                return 12;
            case "Lays 1":
                return 785;
            case "Lays 2":
                return 125;
            case "Lays Сметана/Зелень":
                return 10001;
            case "Pringles Оригинал":
                return 789;
            case "Pringles 1":
                return 123;
            case "Pringles 2":
                return 566;
            case "Чай 1":
                return 120;
            case "Чай 2":
                return 698;
            case "Чай 3":
                return 900;
            case "Чай Ахмад":
                return 1100;
            default:
                return 0;
        }
    }
}



