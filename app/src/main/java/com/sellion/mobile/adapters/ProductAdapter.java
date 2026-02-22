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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private final List<Product> products;
    private final OnProductClickListener listener;
    private Set<Long> itemsInCartIds = new HashSet<>();
    private final boolean showStockInfo;

    // ДОБАВЛЕНО: Умный форматтер (макс 2 знака, пробел как разделитель тысяч)
    private final DecimalFormat smartFormat;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(List<Product> products, OnProductClickListener listener, boolean showStockInfo) {
        this.products = products;
        this.listener = listener;
        this.showStockInfo = showStockInfo;

        // Настройка умного формата
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(' '); // Разделитель тысяч — пробел
        this.smartFormat = new DecimalFormat("#,###.##", symbols);
    }

    public void setItemsInCart(List<CartEntity> cartEntities) {
        itemsInCartIds.clear();
        if (cartEntities != null) {
            for (CartEntity entity : cartEntities) {
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

        // СБРОС СОСТОЯНИЯ
        holder.tvName.setAlpha(1.0f);
        holder.tvName.setTextColor(Color.BLACK);
        holder.tvStock.setTextColor(Color.GRAY);
        holder.tvStock.setVisibility(View.GONE);

        // УСТАНОВКА ДАННЫХ
        String name = product.getName();

        // ИСПРАВЛЕНО: Умный формат вместо String.format("%,.0f")
        // Теперь 1540.00 -> 1 540 | 1540.60 -> 1 540.6 | 1540.12 -> 1 540.12
        String priceFormatted = smartFormat.format(product.getPrice());
        holder.tvName.setText(name + " — " + priceFormatted + " ֏");

        // ЛОГИКА ОТОБРАЖЕНИЯ ОСТАТКА
        if (showStockInfo) {
            holder.tvStock.setVisibility(View.VISIBLE);
            int stock = product.getStockQuantity();
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

        // ПОДСВЕТКА ТОВАРОВ В КОРЗИНЕ (Синий цвет)
        if (itemsInCartIds.contains(product.getId())) {
            holder.tvName.setTextColor(Color.BLUE);
        } else {
            holder.tvName.setTextColor(Color.BLACK);
        }

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