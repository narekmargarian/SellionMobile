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
import com.sellion.mobile.entity.ReturnEntity;

import java.util.List;
import java.util.Map;

public class ReturnAdapter  extends RecyclerView.Adapter<ReturnAdapter.ReturnVH> {

    private List<ReturnEntity> list; // Список теперь из базы данных Room
    private final OnItemClickListener listener;

    // Интерфейс для обработки клика, принимает ReturnEntity
    public interface OnItemClickListener {
        void onItemClick(ReturnEntity returnModel);
    }

    public ReturnAdapter(List<ReturnEntity> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReturnVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Используем ваш существующий макет item_return
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_return, parent, false);
        return new ReturnVH(v);
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
    public void updateData(List<ReturnEntity> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }




    @Override
    public void onBindViewHolder(@NonNull ReturnVH holder, int position) {
        ReturnEntity item = list.get(position);
        if (item == null) return;

        holder.tvShopName.setText(item.shopName);

        // Если в Entity уже сохранена сумма (мы добавили расчет при сохранении),
        // берем её сразу, чтобы не нагружать БД в списке.
        if (item.totalAmount > 0) {
            holder.tvTotalAmount.setText(String.format("%,.0f ֏", item.totalAmount));
        } else {
            // Фоновый расчет, если сумма почему-то равна 0 (для старых записей)
            new Thread(() -> {
                double total = 0;
                AppDatabase db = AppDatabase.getInstance(holder.itemView.getContext().getApplicationContext());

                if (item.items != null) {
                    // ИСПРАВЛЕНО: Используем Long для ключа Map
                    for (Map.Entry<Long, Integer> entry : item.items.entrySet()) {
                        // ИСПРАВЛЕНО: Ищем по ID (entry.getKey())
                        double priceFromDb = db.productDao().getPriceById(entry.getKey());
                        total += (entry.getValue() * priceFromDb);
                    }
                }

                final String totalStr = String.format("%,.0f ֏", total);
                holder.tvTotalAmount.post(() -> holder.tvTotalAmount.setText(totalStr));
            }).start();
        }

        // Логика статуса (без изменений)
        if ("SENT".equals(item.status)) {
            holder.tvStatus.setText("ОТПРАВЛЕН");
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#2E7D32"));
        } else {
            holder.tvStatus.setText("ОЖИДАЕТ");
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#2196F3"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

}