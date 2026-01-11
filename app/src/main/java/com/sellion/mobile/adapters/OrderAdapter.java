package com.sellion.mobile.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;
import com.sellion.mobile.entity.OrderEntity;

import java.util.List;
import java.util.Map;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderVH> {

    private List<OrderEntity> list; // Изменено на OrderEntity
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(OrderEntity o);
    }

    public OrderAdapter(List<OrderEntity> list, OnOrderClickListener l) {
        this.list = list;
        this.listener = l;
    }

    @NonNull
    @Override
    public OrderVH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_order, p, false);
        return new OrderVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderVH h, int pos) {
        OrderEntity o = list.get(pos);

        h.tvShopName.setText(o.shopName);

        // Расчет итоговой суммы из Map в Entity
        double total = 0;
        if (o.items != null) {
            for (Map.Entry<String, Integer> entry : o.items.entrySet()) {
                total += (entry.getValue() * getPriceForProduct(entry.getKey()));
            }
        }
        h.tvTotalAmount.setText(String.format("%,.0f ֏", total));

        // Визуализация статуса (LiveData обновит это мгновенно)
        if ("SENT".equals(o.status)) {
            h.tvStatus.setText("ОТПРАВЛЕН");
            h.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            h.tvStatus.setText("ОЖИДАЕТ");
            h.tvStatus.setTextColor(Color.parseColor("#2196F3"));
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onOrderClick(o);
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class OrderVH extends RecyclerView.ViewHolder {
        TextView tvShopName, tvStatus, tvTotalAmount;
        public OrderVH(View v) {
            super(v);
            tvShopName = v.findViewById(R.id.tvOrderShopName);
            tvStatus = v.findViewById(R.id.tvOrderStatus);
            tvTotalAmount = v.findViewById(R.id.tvOrderTotalAmount);
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



