package com.sellion.mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;
import com.sellion.mobile.model.PromoAction;

import java.util.List;

public class PromoAdapter extends RecyclerView.Adapter<PromoAdapter.ViewHolder> {

    private final List<PromoAction> promoList;
    private final OnPromoClickListener listener;
    private int selectedPosition = -1;

    public interface OnPromoClickListener {
        void onPromoClick(PromoAction promo);
    }

    public PromoAdapter(List<PromoAction> promoList, OnPromoClickListener listener) {
        this.promoList = promoList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_promo_selectable, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PromoAction promo = promoList.get(position);

        holder.tvTitle.setText(promo.getTitle());
        holder.tvPeriod.setText("До: " + promo.getEndDate().toString());

        // Логика выбора только одного элемента (RadioGroup effect)
        holder.radioButton.setChecked(position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            selectedPosition = holder.getAbsoluteAdapterPosition();
            notifyDataSetChanged();
            if (listener != null) {
                listener.onPromoClick(promo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return promoList != null ? promoList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPeriod;
        RadioButton radioButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvPromoTitle);
            tvPeriod = itemView.findViewById(R.id.tvPromoPeriod);
            radioButton = itemView.findViewById(R.id.rbSelectPromo);
        }
    }
}