package com.sellion.mobile.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;
import com.sellion.mobile.entity.DebtModel;

import java.util.List;

public class DebtsAdapter extends RecyclerView.Adapter<DebtsAdapter.DebtViewHolder> {

    private List<DebtModel> debtList;
    private OnShopClickListener listener;

    // Интерфейс для обработки нажатий
    public interface OnShopClickListener {
        void onShopClick(DebtModel debt);
    }

    public DebtsAdapter(List<DebtModel> debtList, OnShopClickListener listener) {
        this.debtList = debtList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DebtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Подключаем наш новый файл разметки item_debt.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_debt, parent, false);
        return new DebtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DebtViewHolder holder, int position) {
        DebtModel debt = debtList.get(position);

        // 1. Устанавливаем название магазина (Будет крупным и черным из XML)
        holder.textName.setText(debt.getShopName());

        // 2. Логика отображения долга
        if (debt.getDebtAmount() > 0) {
            // Если есть долг — пишем сумму КРАСНЫМ цветом
            holder.textDetails.setText(String.format("Долг: %,.0f ֏", debt.getDebtAmount()));
            holder.textDetails.setTextColor(Color.parseColor("#B00020")); // Насыщенный красный
        } else {
            // Если долга нет — пишем ЗЕЛЕНЫМ
            holder.textDetails.setText("Задолженности нет");
            holder.textDetails.setTextColor(Color.parseColor("#388E3C")); // Темно-зеленый
        }

        // 3. Обрабатываем нажатие на всю область строки
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onShopClick(debt);
            }
        });
    }

    @Override
    public int getItemCount() {
        return debtList.size();
    }

    static class DebtViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textDetails;

        DebtViewHolder(View v) {
            super(v);
            // Привязываем переменные к ID из item_debt.xml
            textName = v.findViewById(R.id.tvShopName);
            textDetails = v.findViewById(R.id.tvShopDetails);
        }
    }
}