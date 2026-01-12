package com.sellion.mobile.adapters;

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
    private Set<String> itemsInCart = new HashSet<>();
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
        itemsInCart.clear();
        if (cartEntities != null) {
            for (CartEntity entity : cartEntities) {
                itemsInCart.add(entity.productName);
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

        String name = product.getName();
        String price = String.format("%,.0f ֏", product.getPrice());
        holder.tvName.setText(name + " — " + price);

        // ЛОГИКА ОТОБРАЖЕНИЯ ОСТАТКА
        if (showStockInfo) {
            holder.tvStock.setVisibility(View.VISIBLE);
            holder.tvStock.setText("Остаток: " + product.getStockQuantity() + " шт.");

            if (product.getStockQuantity() <= 0) {
                holder.tvStock.setTextColor(Color.RED);
                holder.tvName.setAlpha(0.5f);
            } else {
                holder.tvStock.setTextColor(Color.GRAY);
                holder.tvName.setAlpha(1.0f);
            }
        } else {
            // ПРИНУДИТЕЛЬНО СКРЫВАЕМ В КАТЕГОРИЯХ И ВОЗВРАТАХ
            holder.tvStock.setVisibility(View.GONE);
            holder.tvName.setAlpha(1.0f);
        }

        // Подсветка товаров в корзине
        if (itemsInCart.contains(product.getName())) {
            holder.tvName.setTextColor(Color.BLUE);
        } else {
            // Обязательно сбрасываем в черный, чтобы при прокрутке цвета не путались
            holder.tvName.setTextColor(Color.BLACK);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProductClick(product);
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