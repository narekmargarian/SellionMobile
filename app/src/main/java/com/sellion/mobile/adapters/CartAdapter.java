package com.sellion.mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;
import com.sellion.mobile.managers.CartManager;
import com.sellion.mobile.model.Product;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private final List<Product> selectedProducts;
    private final OnItemClickListener listener;

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

        Integer qty = CartManager.getInstance().getCartItems().get(product.getName());
        int currentQty = (qty != null) ? qty : 0;

        double itemTotal = product.getPrice() * currentQty;

        // ИСПРАВЛЕНИЕ: %,.0f вместо %,.0d
        String priceText = String.format("%,.0f", itemTotal);

        holder.tvName.setText(product.getName() + " — " + currentQty + " шт. (" + priceText + " ֏)");

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