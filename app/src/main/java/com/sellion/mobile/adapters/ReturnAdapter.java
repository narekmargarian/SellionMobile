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
import com.sellion.mobile.entity.ReturnModel;

import java.util.List;
import java.util.Map;

public class ReturnAdapter extends RecyclerView.Adapter<ReturnAdapter.ReturnVH> {
    private List<ReturnModel> list;
    private OnItemClickListener listener;

    // Интерфейс теперь принимает ReturnModel
    public interface OnItemClickListener {
        void onItemClick(ReturnModel returnModel);
    }

    public ReturnAdapter(List<ReturnModel> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReturnVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Используем макет карточки возврата
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_return, parent, false);
        return new ReturnVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReturnVH holder, int position) {
        ReturnModel item = list.get(position);

        holder.tvShopName.setText(item.shopName);
//        holder.tvStatus.setText("ВОЗВРАТ");

        // Расчет суммы
        double total = 0;
        if (item.items != null) {
            for (Map.Entry<String, Integer> entry : item.items.entrySet()) {
                total += (entry.getValue() * getPriceForProduct(entry.getKey()));
            }
        }
        holder.tvTotalAmount.setText(String.format("%,.0f ֏", total));

        if (item.status ==  ReturnModel.Status.SENT) {
            holder.tvStatus.setText("ОТПРАВЛЕН");
            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            holder.tvStatus.setText("ОЖИДАЕТ");
            holder.tvStatus.setTextColor(Color.parseColor("#2196F3"));
        }



        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class ReturnVH extends RecyclerView.ViewHolder {
        TextView tvShopName, tvTotalAmount, tvStatus;

        ReturnVH(View v) {
            super(v);
            tvShopName = v.findViewById(R.id.tvReturnShopName);
            tvTotalAmount = v.findViewById(R.id.tvReturnTotalAmount);
            tvStatus = v.findViewById(R.id.tvReturnStatus);
        }
    }

    // Справочник цен для корректного отображения суммы в списке
    private int getPriceForProduct(String name) {
        if (name == null) return 0;
        switch (name) {
            case "Шоколад Аленка":
                return 500;
            case "Шоколад 1":
                return 1000;
            case "Шоколад 2":
                return 1500;
            case "Шоколад 3":
                return 1250;
            case "Конфеты Мишка":
                return 1790;
            case "Вафли Артек":
                return 960;
            case "Вафли 1":
                return 630;
            case "Вафли 2":
                return 2560;
            case "Вафли 3":
                return 2430;
            case "Lays 1":
                return 1020;
            case "Lays 2":
                return 4450;
            case "Lays Сметана/Зелень":
                return 440;
            case "Pringles Оригинал":
                return 750;
            case "Pringles 1":
                return 3390;
            case "Pringles 2":
                return 890;
            case "Чай 1":
                return 220;
            case "Чай 2":
                return 9530;
            case "Чай 3":
                return 1990;
            case "Чай Ахмад":
                return 50000000;
            default:
                return 0;
        }
    }
}