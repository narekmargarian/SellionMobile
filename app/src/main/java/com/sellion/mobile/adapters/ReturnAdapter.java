package com.sellion.mobile.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;
import com.sellion.mobile.entity.ReturnEntity;

import java.util.List;
import java.util.Map;

public class ReturnAdapter  extends RecyclerView.Adapter<ReturnAdapter.ReturnVH> {

    private List<ReturnEntity> list; // Список теперь из базы данных Room
    private final OnItemClickListener listener;

    // Интерфейс для обработки клика, принимает ReturnEntity
    public interface OnItemClickListener {
        void onItemClick(ReturnEntity returnModel);
    }

    public ReturnAdapter(List<ReturnEntity> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReturnVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Используем ваш существующий макет item_return
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_return, parent, false);
        return new ReturnVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReturnVH holder, int position) {
        ReturnEntity item = list.get(position);

        holder.tvShopName.setText(item.shopName);

        // 1. Расчет суммы возврата на основе данных из Room
        double total = 0;
        if (item.items != null) {
            for (Map.Entry<String, Integer> entry : item.items.entrySet()) {
                total += (entry.getValue() * getPriceForProduct(entry.getKey()));
            }
        }

        // Форматирование суммы (Разделители тысяч, символ драма)
        holder.tvTotalAmount.setText(String.format("%,.0f ֏", total));

        // 2. Логика статуса (Синхронизировано с WorkManager)
        // В 2026 году мы используем строковые константы "SENT" и "PENDING"
        if ("SENT".equals(item.status)) {
            holder.tvStatus.setText("ОТПРАВЛЕН");
            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32")); // Темно-зеленый
        } else {
            holder.tvStatus.setText("ОЖИДАЕТ");
            holder.tvStatus.setTextColor(Color.parseColor("#2196F3")); // Синий
        }

        // Клик для открытия деталей возврата
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class ReturnVH extends RecyclerView.ViewHolder {
        TextView tvShopName, tvTotalAmount, tvStatus;

        ReturnVH(View v) {
            super(v);
            tvShopName = v.findViewById(R.id.tvReturnShopName);
            tvTotalAmount = v.findViewById(R.id.tvReturnTotalAmount);
            tvStatus = v.findViewById(R.id.tvReturnStatus);
        }
    }
    public void updateData(List<ReturnEntity> newList) {
        this.list = newList;
        notifyDataSetChanged();
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