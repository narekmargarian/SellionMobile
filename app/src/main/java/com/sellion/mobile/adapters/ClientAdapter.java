package com.sellion.mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;
import com.sellion.mobile.model.ClientModel;

import java.util.List;

public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.VH> {

    // 1. Изменяем интерфейс: теперь он передает объект ClientModel целиком
    public interface OnClientClick {
        void onClick(ClientModel client);
    }

    private final List<ClientModel> clients; // Теперь список моделей, а не строк
    private final OnClientClick listener;

    public ClientAdapter(List<ClientModel> clients, OnClientClick listener) {
        this.clients = clients;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Убедитесь, что используете правильный макет (item_client)
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_client, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ClientModel client = clients.get(position);

        // Устанавливаем имя магазина
        holder.tvName.setText(client.getName());

        // Если в макете есть поле для адреса, можно добавить и его:
        // holder.tvAddress.setText(client.getAddress());

        // 2. Передаем весь объект в слушатель
        holder.itemView.setOnClickListener(v -> listener.onClick(client));
    }

    @Override
    public int getItemCount() {
        return clients != null ? clients.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName;
        // TextView tvAddress; // Добавьте, если нужно

        VH(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvClientName);
            // tvAddress = v.findViewById(R.id.tvClientAddress);
        }
    }
}