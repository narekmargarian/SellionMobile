package com.sellion.mobile.entity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.sellion.mobile.R;

public class ProductInfoSheet extends BottomSheetDialogFragment {
    private final Product product;

    public ProductInfoSheet(Product product) {
        this.product = product;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Используем макет информационной карточки
        View v = inflater.inflate(R.layout.dialog_product_info, container, false);

        TextView name = v.findViewById(R.id.tvInfoName);
        TextView price = v.findViewById(R.id.tvInfoPrice);
        TextView desc = v.findViewById(R.id.tvInfoDescription);
        Button btnClose = v.findViewById(R.id.btnCloseInfo);

        // Наполняем данными
        name.setText(product.getName());
        price.setText("Цена: " + product.getPrice() + " ֏");

        // Выводим информацию о коробке и другие детали
        String info = "В упаковке: " + product.getItemsPerBox() + " шт.\n" +
                "Артикул: " + Math.abs(product.getName().hashCode() % 10000) + "\n" +
                "Срок годности: 12 месяцев\n" +
                "Условия хранения: от +5 до +25°C";
        desc.setText(info);

        // КНОПКА ПРОСТО ЗАКРЫВАЕТ ОКНО
        btnClose.setText("Понятно");
        btnClose.setOnClickListener(view -> dismiss());

        return v;
    }
}