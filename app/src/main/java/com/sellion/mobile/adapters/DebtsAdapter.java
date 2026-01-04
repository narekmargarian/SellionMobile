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
    // --- ДОБАВЛЕНО ---
    // Флаг, который определяет, нужно ли показывать детали долга.
    // По умолчанию true (показываем).
    private boolean showDebtDetails = true;

    // Интерфейс для обработки нажатий
    public interface OnShopClickListener {
        void onShopClick(DebtModel debt);
    }

    public DebtsAdapter(List<DebtModel> debtList, OnShopClickListener listener) {
        this.debtList = debtList;
        this.listener = listener;
    }

    // --- ДОБАВЛЕНО: Публичный метод для установки режима отображения ---
    public void setShowDebtDetails(boolean showDetails) {
        this.showDebtDetails = showDetails;
    }
    // ----------------------------------------------------------------


    @NonNull
    @Override
    public DebtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_debt, parent, false);
        return new DebtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DebtViewHolder holder, int position) {
        DebtModel debt = debtList.get(position);

        // 1. Всегда устанавливаем название магазина
        holder.textName.setText(debt.getShopName());

        // 2. ПРОВЕРКА ФЛАГА: Показывать долг или нет?
        if (showDebtDetails) {
            // РЕЖИМ "ДОЛГИ": Показываем поле и раскрашиваем его
            holder.textDetails.setVisibility(View.VISIBLE);

            if (debt.getDebtAmount() > 0) {
                // Если есть долг — КРАСНЫЙ цвет
                holder.textDetails.setText(String.format("Долг: %,.0f ֏", debt.getDebtAmount()));
                holder.textDetails.setTextColor(Color.parseColor("#B00020"));
            } else {
                // Если долга нет — ЗЕЛЕНЫЙ цвет
                holder.textDetails.setText("Задолженности нет");
                holder.textDetails.setTextColor(Color.parseColor("#388E3C"));
            }
        } else {
            // РЕЖИМ "МАРШРУТ": Полностью скрываем поле с долгом
            // Это уберет и текст, и красную/зеленую надпись
            holder.textDetails.setVisibility(View.GONE);
        }

        // 3. Обработка нажатия
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
            textName = v.findViewById(R.id.tvShopName);
            textDetails = v.findViewById(R.id.tvShopDetails);
        }
    }
}