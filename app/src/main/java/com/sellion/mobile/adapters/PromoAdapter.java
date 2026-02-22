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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class PromoAdapter extends RecyclerView.Adapter<PromoAdapter.ViewHolder> {

    private final List<PromoAction> promoList;
    private final OnPromoClickListener listener;
    // Используем Set для хранения нескольких выбранных позиций
    private final Set<Integer> selectedPositions = new HashSet<>();

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
        holder.tvPeriod.setText("До: " + promo.getEndDate());

        // ИСПРАВЛЕНО: Теперь работаем с CheckBox, чтобы избежать ClassCastException
        holder.checkBox.setChecked(selectedPositions.contains(position));

        holder.itemView.setOnClickListener(v -> {
            int currentPos = holder.getAbsoluteAdapterPosition();

            if (selectedPositions.contains(currentPos)) {
                selectedPositions.remove(currentPos);
            } else {
                selectedPositions.add(currentPos);
            }

            // Обновляем только этот элемент
            notifyItemChanged(currentPos);

            if (listener != null) {
                listener.onPromoClick(promo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return promoList != null ? promoList.size() : 0;
    }

    public List<PromoAction> getSelectedPromos() {
        List<PromoAction> selected = new ArrayList<>();
        for (Integer pos : selectedPositions) {
            selected.add(promoList.get(pos));
        }
        return selected;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPeriod;
        // ИСПРАВЛЕНО: Заменили RadioButton на CheckBox
        android.widget.CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvPromoTitle);
            tvPeriod = itemView.findViewById(R.id.tvPromoPeriod);
            // ИСПРАВЛЕНО: Находим CheckBox по ID из твоего XML
            checkBox = itemView.findViewById(R.id.rbSelectPromo);
        }
    }
}
