package com.sellion.mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private List<String> mData;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(String category);
    }

    public CategoryAdapter(List<String> data, OnItemClickListener listener) {
        this.mData = data;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String category = mData.get(position);
        holder.textView.setText(category);

        // ИСПРАВЛЕНО: Скрываем поле остатка, так как это список категорий
        if (holder.tvStock != null) {
            holder.tvStock.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> mListener.onItemClick(category));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        TextView tvStock; // ИСПРАВЛЕНО: Добавлено поле для остатка

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tvCategoryName);
            tvStock = itemView.findViewById(R.id.tvStockQuantity); // ИСПРАВЛЕНО: Привязка нового ID
        }
    }
}
