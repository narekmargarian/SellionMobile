package com.sellion.mobile.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.sellion.mobile.R;
import com.sellion.mobile.adapters.ProductAdapter;
import com.sellion.mobile.entity.Product;

import java.util.ArrayList;
import java.util.List;


public class ProductFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product, container, false);

        // 1. Получаем название категории
        String category = getArguments() != null ? getArguments().getString("category_name") : "Товары";
        TextView tvTitle = view.findViewById(R.id.tvCategoryTitle);
        tvTitle.setText(category);

        // 2. Кнопка назад
        ImageButton btnBack = view.findViewById(R.id.btnBackProducts);
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // 3. Настройка списка
        RecyclerView rv = view.findViewById(R.id.recyclerViewProducts);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        // Заглушка данных (потом будешь брать из БД)
        List<Product> productList = new ArrayList<>();
        productList.add(new Product("Товар 1 из " + category, "100$"));
        productList.add(new Product("Товар 2 из " + category, "200$"));

        ProductAdapter adapter = new ProductAdapter(productList, product -> {
            showQuantityDialog(product); // Вызов диалога
        });
        rv.setAdapter(adapter);

        return view;
    }

    private void showQuantityDialog(Product product) {
        // 1. Создаем объект диалога
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());

        // 2. Инфлейтим нашу новую разметку
        View view = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_quantity, null);
        dialog.setContentView(view);

        // 3. Находим элементы внутри окна
        TextView tvTitle = view.findViewById(R.id.tvSheetTitle);
        EditText etQuantity = view.findViewById(R.id.etSheetQuantity);
        View btnConfirm = view.findViewById(R.id.btnConfirm);

        // Устанавливаем название товара
        tvTitle.setText(product.getName());

        // 4. Логика кнопки "Добавить"
        btnConfirm.setOnClickListener(v -> {
            String quantity = etQuantity.getText().toString();
            if (!quantity.isEmpty()) {
                // Здесь действие (например, сохранение)
                Toast.makeText(getContext(), "Добавлено " + quantity + " шт.", Toast.LENGTH_SHORT).show();
                dialog.dismiss(); // Закрываем окно
            } else {
                etQuantity.setError("Укажите число");
            }
        });

        // 5. Показываем окно
        dialog.show();
    }
}