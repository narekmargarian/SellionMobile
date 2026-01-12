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

import java.util.List;

public class DebtsAdapter extends RecyclerView.Adapter<DebtsAdapter.DebtViewHolder> {

    private List<ClientEntity> clientList;
    private OnShopClickListener listener;
    private boolean showDebtDetails = true;

    // Интерфейс теперь тоже работает с ClientEntity
    public interface OnShopClickListener {
        void onShopClick(ClientEntity client);
    }

    public DebtsAdapter(List<ClientEntity> clientList, OnShopClickListener listener) {
        this.clientList = clientList;
        this.listener = listener;
    }

    public void setShowDebtDetails(boolean showDetails) {
        this.showDebtDetails = showDetails;
    }

    @NonNull
    @Override
    public DebtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_debt, parent, false);
        return new DebtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DebtViewHolder holder, int position) {
        ClientEntity client = clientList.get(position);

        // 2. Устанавливаем название (в Entity это поле .name)
        holder.textName.setText(client.name);

        if (showDebtDetails) {
            holder.textDetails.setVisibility(View.VISIBLE);

            // Используем поле .debt из ClientEntity
            if (client.debt > 0) {
                holder.textDetails.setText(String.format("Долг: %,.0f ֏", client.debt));
                holder.textDetails.setTextColor(Color.parseColor("#D32F2F"));
            } else {
                holder.textDetails.setText("Задолженности нет");
                holder.textDetails.setTextColor(Color.parseColor("#388E3C"));
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

    static class DebtViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textDetails;

        DebtViewHolder(View v) {
            super(v);
            textName = v.findViewById(R.id.tvShopName);
            textDetails = v.findViewById(R.id.tvShopDetails);
        }
    }
}