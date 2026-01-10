package com.sellion.mobile.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;
import com.sellion.mobile.entity.OrderModel;
import com.sellion.mobile.entity.ReturnModel;

import java.util.List;
import java.util.Map;

public class ReturnAdapter extends RecyclerView.Adapter<ReturnAdapter.ReturnVH> {
    private List<ReturnModel> list;
    private OnItemClickListener listener;

    // Интерфейс теперь принимает ReturnModel
    public interface OnItemClickListener {
        void onItemClick(ReturnModel returnModel);
    }

    public ReturnAdapter(List<ReturnModel> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReturnVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Используем макет карточки возврата
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_return, parent, false);
        return new ReturnVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReturnVH holder, int position) {
        ReturnModel item = list.get(position);

        holder.tvShopName.setText(item.shopName);
//        holder.tvStatus.setText("ВОЗВРАТ");

        // Расчет суммы
        double total = 0;
        if (item.items != null) {
            for (Map.Entry<String, Integer> entry : item.items.entrySet()) {
                total += (entry.getValue() * getPriceForProduct(entry.getKey()));
            }
        }
        holder.tvTotalAmount.setText(String.format("%,.0f ֏", total));

        if (item.status ==  ReturnModel.Status.SENT) {
            holder.tvStatus.setText("ОТПРАВЛЕН");
            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            holder.tvStatus.setText("ОЖИДАЕТ");
            holder.tvStatus.setTextColor(Color.parseColor("#2196F3"));
        }



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

    // Справочник цен для корректного отображения суммы в списке
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