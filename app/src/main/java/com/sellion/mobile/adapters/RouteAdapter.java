package com.sellion.mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;
import com.sellion.mobile.entity.DebtModel;

import java.util.List;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.ViewHolder> {

    private final List<DebtModel> routeList; // Используем ту же модель данных
    private final DebtsAdapter.OnShopClickListener listener; // Используем тот же интерфейс клика

    public RouteAdapter(List<DebtModel> routeList, DebtsAdapter.OnShopClickListener listener) {
        this.routeList = routeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Используем старую, чистую разметку item_category.xml, где нет полей долга
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DebtModel store = routeList.get(position);
        holder.tvName.setText(store.getShopName()); // Выводим только имя

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onShopClick(store);
            }
        });
    }

    @Override
    public int getItemCount() {
        return routeList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;

        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvCategoryName); // ID из item_category.xml
        }
    }
}