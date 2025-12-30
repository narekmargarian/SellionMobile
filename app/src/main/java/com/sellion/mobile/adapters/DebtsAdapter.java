package com.sellion.mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.entity.DebtModel;

import java.util.List;

public class DebtsAdapter extends RecyclerView.Adapter<DebtsAdapter.DebtViewHolder> {

    private List<DebtModel> debtList;
    private OnShopClickListener listener;

    // Интерфейс для обработки нажатий (чтобы знать, на какой магазин нажали)
    public interface OnShopClickListener {
        void onShopClick(DebtModel debt);
    }

    // Конструктор адаптера
    public DebtsAdapter(List<DebtModel> debtList, OnShopClickListener listener) {
        this.debtList = debtList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DebtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Создаем стандартную, простую разметку для строки списка Android
        // В будущем можно создать красивый XML-файл с карточкой для долгов
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new DebtViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull DebtViewHolder holder, int position) {
        // Заполняем элементы интерфейса данными из списка
        DebtModel debt = debtList.get(position);
        holder.text1.setText(debt.getShopName());
        holder.text2.setText(String.format("Долг: %,.0f ֏", debt.getDebtAmount())); // Форматируем в армянские драмы

        // Обрабатываем нажатие на строку
        holder.itemView.setOnClickListener(v -> listener.onShopClick(debt));
    }

    @Override
    public int getItemCount() {
        return debtList.size(); // Сколько всего магазинов в списке
    }

    // Внутренний класс для хранения ссылок на элементы в строке (магазине)
    static class DebtViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;
        DebtViewHolder(View v) {
            super(v);
            text1 = v.findViewById(android.R.id.text1); // ID стандартного TextView 1
            text2 = v.findViewById(android.R.id.text2); // ID стандартного TextView 2
        }
    }
}
