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
import com.google.android.material.button.MaterialButton;
import com.sellion.mobile.R;
import com.sellion.mobile.adapters.ProductAdapter;
import com.sellion.mobile.entity.CartManager;
import com.sellion.mobile.entity.Product;

import java.util.ArrayList;
import java.util.List;


public class ProductFragment extends Fragment {
    private ProductAdapter adapter; // Поле класса

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

        // 3. Настройка RecyclerView
        RecyclerView rv = view.findViewById(R.id.recyclerViewProducts);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        // 4. Подготовка данных
        List<Product> productList = new ArrayList<>();

        switch (category) {
            case "Сладкое":
                productList.add(new Product("Шоколад Аленка", "500 др."));
                productList.add(new Product("Конфеты Мишка", "1200 др."));
                productList.add(new Product("Вафли Артек", "300 др."));
                productList.add(new Product("Печенье Овсяное", "450 др."));
                productList.add(new Product("Зефир в шоколаде", "800 др."));
                break;
            case "Чай":
                productList.add(new Product("Чай Гринфилд", "900 др."));
                productList.add(new Product("Чай Ахмад", "1100 др."));
                productList.add(new Product("Чай Липтон 50 пак", "700 др."));
                productList.add(new Product("Чай Тесс", "850 др."));
                productList.add(new Product("Чай Травяной", "600 др."));
                break;
            case "Чипсы":
                productList.add(new Product("Lays Сметана/Зелень", "600 др."));
                productList.add(new Product("Pringles Оригинал", "1500 др."));
                productList.add(new Product("Люкс Бекон", "400 др."));
                productList.add(new Product("Cheetos Сыр", "350 др."));
                productList.add(new Product("Doritos", "950 др."));
                break;
            case "Хлопья":
                productList.add(new Product("Овсяные хлопья 1кг", "400 др."));
                productList.add(new Product("Кукурузные шарики", "550 др."));
                productList.add(new Product("Гречневые хлопья", "600 др."));
                productList.add(new Product("Мюсли с фруктами", "850 др."));
                productList.add(new Product("Хлопья Фитнес", "1100 др."));
                break;
            case "Весовые":
                productList.add(new Product("Сахар (вес)", "450 др."));
                productList.add(new Product("Мука в/с (вес)", "350 др."));
                productList.add(new Product("Рис Басмати (вес)", "1200 др."));
                productList.add(new Product("Макароны (вес)", "400 др."));
                productList.add(new Product("Гречка (вес)", "500 др."));
                break;
            default:
                productList.add(new Product("Товар не найден", "0"));
                break;
        }

        // 5. Инициализация адаптера (используем ПРАВИЛЬНЫЙ конструктор)
        // ВАЖНО: мы записываем его в ПЕРЕМЕННУЮ КЛАССА, а не создаем новую локальную
        adapter = new ProductAdapter(productList, product -> {
            showQuantityDialog(product);
        });

        rv.setAdapter(adapter);

        return view;
    }

    private void showQuantityDialog(Product product) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_quantity, null);
        dialog.setContentView(view);

        TextView tvTitle = view.findViewById(R.id.tvSheetTitle);
        EditText etQuantity = view.findViewById(R.id.etSheetQuantity);
        View btnPlus = view.findViewById(R.id.btnPlus);
        View btnMinus = view.findViewById(R.id.btnMinus);
        View btnConfirm = view.findViewById(R.id.btnConfirm);
        TextView btnDelete = view.findViewById(R.id.btnDeleteFromCart);

        tvTitle.setText(product.getName());

        // ПРОВЕРКА: Если товар уже заказан — подгружаем количество
        if (CartManager.getInstance().hasProduct(product.getName())) {
            int currentQty = CartManager.getInstance().getCartItems().get(product.getName());
            etQuantity.setText(String.valueOf(currentQty));
            btnDelete.setVisibility(View.VISIBLE);
            ((MaterialButton)btnConfirm).setText("Изменить количество");
        }

        // Логика кнопок + и -
        btnPlus.setOnClickListener(v -> {
            int val = Integer.parseInt(etQuantity.getText().toString().isEmpty() ? "0" : etQuantity.getText().toString());
            etQuantity.setText(String.valueOf(val + 1));
        });

        btnMinus.setOnClickListener(v -> {
            int val = Integer.parseInt(etQuantity.getText().toString().isEmpty() ? "0" : etQuantity.getText().toString());
            if (val > 1) etQuantity.setText(String.valueOf(val - 1));
        });

        // Удаление из заказа
        btnDelete.setOnClickListener(v -> {
            CartManager.getInstance().addProduct(product.getName(), 0); // 0 удаляет из мапы в нашем CartManager
            adapter.notifyDataSetChanged();
            dialog.dismiss();
        });

        btnConfirm.setOnClickListener(v -> {
            String qtyS = etQuantity.getText().toString();
            if (!qtyS.isEmpty() && !qtyS.equals("0")) {
                CartManager.getInstance().addProduct(product.getName(), Integer.parseInt(qtyS));
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}