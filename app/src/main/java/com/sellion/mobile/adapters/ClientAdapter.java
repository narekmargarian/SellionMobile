package com.sellion.mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;

import java.util.List;

public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.VH> {

    public interface OnClientClick {
        void onClick(String clientName);
    }

    private final List<String> clients;
    private final OnClientClick listener;

    public ClientAdapter(List<String> clients, OnClientClick listener) {
        this.clients = clients;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_client, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        String name = clients.get(position);
        holder.tvName.setText(name);
        holder.itemView.setOnClickListener(v -> listener.onClick(name));
    }

    @Override
    public int getItemCount() {
        return clients.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName;

        VH(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvClientName);
        }
    }
}