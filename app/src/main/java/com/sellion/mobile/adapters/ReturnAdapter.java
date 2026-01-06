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
        // Используем новый макет item_return
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_return, p, false);
        return new ReturnVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReturnVH h, int p) {
        OrderModel o = list.get(p);
        h.tvShopName.setText(o.shopName);
        h.tvStatus.setText("ВОЗВРАТ");

        // Расчет суммы (обязательно перенесите сюда getPriceForProduct из CurrentOrderFragment)
        double total = 0;
        if (o.items != null) {
            for (Map.Entry<String, Integer> entry : o.items.entrySet()) {
                total += (entry.getValue() * getPriceForProduct(entry.getKey()));
            }
        }
        h.tvTotalAmount.setText(String.format("%,.0f ֏", total));

        h.itemView.setOnClickListener(v -> listener.onItemClick(o));
    }

    @Override public int getItemCount() { return list.size(); }

    static class ReturnVH extends RecyclerView.ViewHolder {
        TextView tvShopName, tvTotalAmount, tvStatus;
        ReturnVH(View v) {
            super(v);
            // Привязываем ID из нашего нового макета item_return
            tvShopName = v.findViewById(R.id.tvReturnShopName);
            tvTotalAmount = v.findViewById(R.id.tvReturnTotalAmount);
            tvStatus = v.findViewById(R.id.tvReturnStatus);
        }
    }

    // Вспомогательный метод (скопируйте его из вашего CurrentOrderFragment, где он уже есть)
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