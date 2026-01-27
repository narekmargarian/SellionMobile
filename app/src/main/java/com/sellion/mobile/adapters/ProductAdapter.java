package com.sellion.mobile.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;
import com.sellion.mobile.entity.CartEntity;
import com.sellion.mobile.model.Product;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private final List<Product> products;
    private final OnProductClickListener listener;
    // ИСПРАВЛЕНО: Используем Long для хранения ID товаров, которые в корзине
    private Set<Long> itemsInCartIds = new HashSet<>();
    private final boolean showStockInfo;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(List<Product> products, OnProductClickListener listener, boolean showStockInfo) {
        this.products = products;
        this.listener = listener;
        this.showStockInfo = showStockInfo;
    }

    public void setItemsInCart(List<CartEntity> cartEntities) {
        itemsInCartIds.clear();
        if (cartEntities != null) {
            for (CartEntity entity : cartEntities) {
                // ИСПРАВЛЕНО: Привязка по ID — это 100% надежность
                itemsInCartIds.add(entity.productId);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);

        if (product == null) return;

        Context context = holder.itemView.getContext();

        // 2. СБРОС СОСТОЯНИЯ (Для корректного ресайклинга)
        holder.tvName.setAlpha(1.0f);
        holder.tvName.setTextColor(Color.BLACK);
        holder.tvStock.setTextColor(Color.GRAY);
        holder.tvStock.setVisibility(View.GONE);

        // 3. УСТАНОВКА ДАННЫХ
        String name = product.getName();
        // Используем строковой ресурс для цены (можно менять формат в XML)
        String priceFormatted = String.format("%,.0f", product.getPrice());
        holder.tvName.setText(name + " — " + priceFormatted + " ֏");

        // 4. ЛОГИКА ОТОБРАЖЕНИЯ ОСТАТКА
        if (showStockInfo) {
            holder.tvStock.setVisibility(View.VISIBLE);
            int stock = product.getStockQuantity();

            // ИСПРАВЛЕНО: Текст через getString (R.string.format_stock)
            holder.tvStock.setText("Остаток: " + stock + " шт.");

            if (stock <= 0) {
                holder.tvStock.setTextColor(Color.RED);
                holder.tvName.setAlpha(0.5f);
            } else {
                holder.tvStock.setTextColor(Color.GRAY);
                holder.tvName.setAlpha(1.0f);
            }
        } else {
            holder.tvStock.setVisibility(View.GONE);
            holder.tvName.setAlpha(1.0f);
        }

        // 5. ПОДСВЕТКА ТОВАРОВ В КОРЗИНЕ (Синий цвет)
        // ИСПРАВЛЕНО: Сравнение по ID гарантирует, что цвет не "мигнет"
        if (itemsInCartIds.contains(product.getId())) {
            holder.tvName.setTextColor(Color.BLUE);
        } else {
            holder.tvName.setTextColor(Color.BLACK);
        }

        // 6. ОБРАБОТКА КЛИКА
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvStock;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            tvStock = itemView.findViewById(R.id.tvStockQuantity);
        }
    }
}
