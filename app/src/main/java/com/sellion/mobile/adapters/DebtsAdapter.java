package com.sellion.mobile.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;
import com.sellion.mobile.entity.ClientEntity;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class DebtsAdapter extends RecyclerView.Adapter<DebtsAdapter.DebtViewHolder> {

    private List<ClientEntity> clientList;
    private OnShopClickListener listener;
    private boolean showDebtDetails = true;

    // Создаем форматтер для "умного" вывода сумм
    private final DecimalFormat smartFormat;

    public interface OnShopClickListener {
        void onShopClick(ClientEntity client);
    }

    public DebtsAdapter(List<ClientEntity> clientList, OnShopClickListener listener) {
        this.clientList = clientList;
        this.listener = listener;

        // Настройка формата: максимум 2 знака, отсечение лишних нулей
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        this.smartFormat = new DecimalFormat("#.##", symbols);
    }

    @Override
    public void onBindViewHolder(@NonNull DebtViewHolder holder, int position) {
        ClientEntity client = clientList.get(position);

        holder.textName.setText(client.name);

        if (showDebtDetails) {
            holder.textDetails.setVisibility(View.VISIBLE);

            if (client.debt > 0) {
                // ИСПРАВЛЕНО: Используем умный формат вместо String.format
                String formattedDebt = smartFormat.format(client.debt);
                holder.textDetails.setText("Долг: " + formattedDebt + " ֏");
                holder.textDetails.setTextColor(Color.parseColor("#D32F2F")); // Красный
            } else {
                holder.textDetails.setText("Задолженности нет");
                holder.textDetails.setTextColor(Color.parseColor("#388E3C")); // Зеленый
            }
        } else {
            holder.textDetails.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onShopClick(client);
            }
        });
    }

    @Override
    public int getItemCount() {
        return clientList != null ? clientList.size() : 0;
    }

    // Остальные методы без изменений...
    public void setShowDebtDetails(boolean showDetails) {
        this.showDebtDetails = showDetails;
    }

    @NonNull
    @Override
    public DebtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_debt, parent, false);
        return new DebtViewHolder(view);
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