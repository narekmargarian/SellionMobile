package com.sellion.mobile.adapters;

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

    // ИСПРАВЛЕНО: Теперь используем Long для ID товара
    private final List<Long> productIds;
    private final Map<Long, Integer> items;

    public OrderHistoryItemsAdapter(Map<Long, Integer> items) {
        this.items = items;
        this.productIds = (items != null) ? new ArrayList<>(items.keySet()) : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Long productId = productIds.get(position);
        Integer qty = items.get(productId);
        int currentQty = (qty != null) ? qty : 0;

        // В 2026 году мы берем данные из БД по ID в фоновом потоке
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(holder.itemView.getContext().getApplicationContext());

            // Получаем полную информацию о товаре по его ID
            ProductEntity product = db.productDao().getProductById(productId);

            String info;
            if (product != null) {
                double rowTotal = product.price * currentQty;
                // Формат: Название — 5 шт. (2,500 ֏)
                info = String.format("%s — %d шт. (%,.0f ֏)", product.name, currentQty, rowTotal);
            } else {
                info = "Товар удален — " + currentQty + " шт.";
            }

            // Возвращаемся в главный поток для обновления UI
            holder.tvName.post(() -> holder.tvName.setText(info));
        }).start();
    }

    @Override
    public int getItemCount() {
        return productIds.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}
