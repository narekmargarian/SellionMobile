package com.sellion.mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderHistoryItemsAdapter extends RecyclerView.Adapter<OrderHistoryItemsAdapter.ViewHolder> {
    private final List<String> names;
    private final Map<String, Integer> items;

    public OrderHistoryItemsAdapter(Map<String, Integer> items) {
        this.items = items;
        this.names = new ArrayList<>(items.keySet());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = names.get(position);
        Integer qty = items.get(name);
        holder.tvName.setText(name + " — " + (qty != null ? qty : 0) + " шт.");
    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}