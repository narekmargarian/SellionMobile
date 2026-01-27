package com.sellion.mobile.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.ProductEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderHistoryItemsAdapter extends RecyclerView.Adapter<OrderHistoryItemsAdapter.ViewHolder> {
    private final List<Long> productIds;
    private final Map<Long, Integer> items;

    public OrderHistoryItemsAdapter(Map<Long, Integer> items) {
        this.items = items;
        this.productIds = (items != null) ? new ArrayList<>(items.keySet()) : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Используем макет, где есть tvStockQuantity
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Long productId = productIds.get(position);
        Integer qty = items.get(productId);
        int currentQty = (qty != null) ? qty : 0;

        Context context = holder.itemView.getContext().getApplicationContext();

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            ProductEntity product = db.productDao().getProductById(productId);

            if (product != null) {
                double rowTotal = product.price * currentQty;
                String info = String.format("%s — %d шт. (%,.0f ֏)", product.name, currentQty, rowTotal);
                String stock = "Остаток: " + product.stockQuantity + " шт.";

                holder.tvName.post(() -> {
                    holder.tvName.setText(info);
                    if (holder.tvStock != null) {
                        holder.tvStock.setVisibility(View.VISIBLE);
                        holder.tvStock.setText(stock);
                    }
                });
            }
        }).start();
    }

    @Override
    public int getItemCount() { return productIds.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStock;
        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            tvStock = itemView.findViewById(R.id.tvStockQuantity);
        }
    }
}

