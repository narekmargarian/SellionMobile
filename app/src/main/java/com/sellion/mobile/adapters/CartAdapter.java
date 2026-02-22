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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private final List<Product> selectedProducts;
    private final OnItemClickListener listener;

    // ДОБАВЛЕНО: Умный форматтер (макс 2 знака, пробел как разделитель тысяч)
    private final DecimalFormat smartFormat;

    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    public CartAdapter(List<Product> selectedProducts, OnItemClickListener listener) {
        this.selectedProducts = selectedProducts;
        this.listener = listener;

        // Настройка умного формата
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(' '); // Разделитель тысяч — пробел
        this.smartFormat = new DecimalFormat("#,###.##", symbols);
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
        if (product == null) return;

        // Получаем количество из CartManager
        // Примечание: Убедитесь, что в CartManager ключом является именно Name,
        // если нет — лучше использовать getId()
        Integer qty = CartManager.getInstance().getCartItems().get(product.getName());
        int currentQty = (qty != null) ? qty : 0;

        // Расчет суммы строки с округлением до 2 знаков (как на бэкенде)
        double itemTotal = Math.round((product.getPrice() * currentQty) * 100.0) / 100.0;

        // ИСПРАВЛЕНО: Умный формат вместо String.format("%,.0f")
        // Теперь 1540.00 -> 1 540 | 1540.60 -> 1 540.6 | 1540.12 -> 1 540.12
        String priceFormatted = smartFormat.format(itemTotal);

        holder.tvName.setText(product.getName() + " — " + currentQty + " шт. (" + priceFormatted + " ֏)");

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