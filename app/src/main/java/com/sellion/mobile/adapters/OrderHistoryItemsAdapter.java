package com.sellion.mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderHistoryItemsAdapter extends RecyclerView.Adapter<OrderHistoryItemsAdapter.ViewHolder> {
    private final List<String> names;
    private final Map<String, Integer> items;

    public OrderHistoryItemsAdapter(Map<String, Integer> items) {
        this.items = items;
        // Если items придет null, создаем пустой список во избежание Crash
        this.names = (items != null) ? new ArrayList<>(items.keySet()) : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Используем тот же макет, что и для категорий/товаров
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = names.get(position);
        Integer qty = items.get(name);
        int currentQty = (qty != null) ? qty : 0;

        // Расчет суммы для конкретной строки через справочник
        int price = getPriceForProduct(name);
        double rowTotal = (double) price * currentQty;

        // Формат 2026: Товар — 5 шт. (2,500 ֏)
        String info = String.format("%s — %d шт. (%,.0f ֏)", name, currentQty, rowTotal);
        holder.tvName.setText(info);
    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
        }
    }
    private int getPriceForProduct(String name) {
        if (name == null) return 0;
        switch (name) {
            case "Чипсы кокосовые ВМ Оригинальные":
                return 730;
            case "Чипсы кокосовые ВМ Соленая карамель":
                return 730;
            case "Чипсы кокосовые Costa Cocosta":
                return 430;
            case "Чипсы кокосовые Costa Cocosta Васаби":
                return 430;
            case "Шарики Манго в какао-глазури ВМ":
                return 930;
            case "Шарики Манго в белой глазури ВМ":
                return 930;
            case "Шарики Банано в глазури ВМ":
                return 730;
            case "Шарики Имбирь сладкий в глазури ВМ":
                return 930;
            case "Чай ВМ Лемонграсс и ананас":
                return 1690;
            case "Чай ВМ зеленый с фруктами":
                return 1690;
            case "Чай ВМ черный Мята и апельсин":
                return 1690;
            case "Чай ВМ черный Черника и манго":
                return 1990;
            case "Чай ВМ черный Шишки и саган-дайля":
                return 1990;
            case "Чай ВМ зеленый Жасмин и манго":
                return 1990;
            case "Чай ВМ черный Цветочное манго":
                return 590;
            case "Чай ВМ черный Шишки и клюква":
                return 790;
            case "Чай ВМ черный Нежная черника":
                return 790;
            case "Чай ВМ черный Ассам Цейлон":
                return 1190;
            case "Чай ВМ черный \"Хвойный\"":
                return 790;
            case "Чай ВМ черный \"Русский березовый\"":
                return 790;
            case "Чай ВМ черный Шишки и малина":
                return 790;
            case "Сух. Манго сушеное Вкусы мира":
                return 1490;
            case "Сух. Манго сушеное ВМ Чили":
                return 1490;
            case "Сух. Папайя сушеная Вкусы мира":
                return 1190;
            case "Сух. Манго шарики из сушеного манго":
                return 1190;
            case "Сух. Манго Сушеное LikeDay (250г)":
                return 2490;
            case "Сух. Манго Сушеное LikeDay (100г)":
                return 1190;
            case "Сух.Бананы вяленые Вкусы мира":
                return 1190;
            case "Сух.Джекфрут сушеный Вкусы мира":
                return 1190;
            case "Сух.Ананас сушеный Вкусы мира":


            default:
                return 0;
        }
    }
}