package com.sellion.mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;
import com.sellion.mobile.entity.CartManager;
import com.sellion.mobile.entity.Product;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private List<Product> selectedProducts;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    public CartAdapter(List<Product> selectedProducts, OnItemClickListener listener) {
        this.selectedProducts = selectedProducts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = selectedProducts.get(position);

        // 1. Получаем количество из менеджера корзины
        Integer qty = CartManager.getInstance().getCartItems().get(product.getName());
        int currentQty = (qty != null) ? qty : 0;

        // 2. Считаем сумму для текущей строки (Цена * Количество)
        // В 2026 году важно показывать менеджеру, сколько стоит каждая позиция
        double itemTotal = product.getPrice() * currentQty;

        // 3. Форматируем числа (добавляем разделители тысяч для Драма)
        String quantityText = String.valueOf(currentQty);
        String priceText = String.format("%,.0f", itemTotal);

        // 4. Устанавливаем итоговый текст
        // Пример: Шоколад Аленка — 3 шт. (1,500 ֏)
        holder.tvName.setText(product.getName() + " — " + quantityText + " шт. (" + priceText + " ֏)");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(product);
        });
    }

    @Override
    public int getItemCount() {
        return selectedProducts != null ? selectedProducts.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}