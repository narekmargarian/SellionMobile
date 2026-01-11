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
import com.sellion.mobile.entity.Product;
import com.sellion.mobile.managers.CartManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private final List<Product> products;
    private final OnProductClickListener listener;
    // Используем Set для быстрого поиска товаров, которые уже в корзине
    private Set<String> itemsInCart = new HashSet<>();

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(List<Product> products, OnProductClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    // --- НОВЫЙ МЕТОД ДЛЯ ОБНОВЛЕНИЯ ЦВЕТОВ ---
    // Вызывайте его из фрагмента через LiveData
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

        // ИСПРАВЛЕНИЕ ОШИБКИ ФОРМАТИРОВАНИЯ:
        // Используем String.valueOf для цены, чтобы избежать конфликта типов в getString
        String name = product.getName();
        String price = String.format("%,d ֏", product.getPrice());

        // В strings.xml должно быть: <string name="product_format">%1$s — %2$s</string>
        holder.tvName.setText(name + " — " + price);

        // ИСПРАВЛЕНИЕ hasProduct: Проверяем наличие в нашем сете
        if (itemsInCart.contains(product.getName())) {
            holder.tvName.setTextColor(Color.BLUE);
        } else {
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
        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}