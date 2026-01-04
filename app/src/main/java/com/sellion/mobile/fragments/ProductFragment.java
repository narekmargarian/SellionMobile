package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.sellion.mobile.R;
import com.sellion.mobile.adapters.ProductAdapter;
import com.sellion.mobile.entity.CartManager;
import com.sellion.mobile.entity.Product;

import java.util.ArrayList;
import java.util.List;


public class ProductFragment extends Fragment {
    private ProductAdapter adapter;
    private boolean isOrderMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product, container, false);

        // 1. Получаем режим (Заказ или Просмотр) и категорию
        if (getArguments() != null) {
            isOrderMode = getArguments().getBoolean("is_order_mode", false);
        }
        String category = getArguments() != null ? getArguments().getString("category_name") : "Товары";

        TextView tvTitle = view.findViewById(R.id.tvCategoryTitle);
        tvTitle.setText(category);

        ImageButton btnBack = view.findViewById(R.id.btnBackProducts);
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        RecyclerView rv = view.findViewById(R.id.recyclerViewProducts);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        List<Product> productList = new ArrayList<>();
        // Наполнение данными (Имя, Цена, Коробка, Штрих-код)
        switch (category) {
            case "Сладкое":
                productList.add(new Product("Шоколад Аленка", 5060, 20, "46450123456701"));
                productList.add(new Product("Шоколад 1", 2560, 10, "4601234567021"));
                productList.add(new Product("Шоколад 2", 250, 10, "46012345612702"));
                productList.add(new Product("Шоколад 3", 4853, 10, "4601234256702"));
                productList.add(new Product("Конфеты Мишка", 2500, 10, "4601230456702"));
                productList.add(new Product("Вафли Артек", 254154, 15, "4601234156703"));
                productList.add(new Product("Вафли 1", 1, 15, "4601234156703"));
                productList.add(new Product("Вафли 2", 2, 15, "4601234156703"));
                productList.add(new Product("Вафли 3", 5, 15, "4601234156703"));
                break;
            case "Чипсы":
                productList.add(new Product("Lays 1", 7566, 3, "4601234856704"));
                productList.add(new Product("Lays 2", 4785, 3, "4601234456704"));
                productList.add(new Product("Lays Сметана/Зелень", 755, 3, "4601234456704"));
                productList.add(new Product("Pringles 1", 589, 6, "4601234566705"));
                productList.add(new Product("Pringles 2", 289, 6, "4601234856705"));
                productList.add(new Product("Pringles Оригинал", 389, 6, "460123689456705"));
                break;
            case "Чай":
                productList.add(new Product("Чай 3", 9010, 12, "44601234411556706"));
                productList.add(new Product("Чай 2", 70, 12, "4604123456706"));
                productList.add(new Product("Чай 1", 900, 12, "1460123456706"));
                productList.add(new Product("Чай Ахмад", 1100, 12, "4601233456707"));
                break;
            default:
                productList.add(new Product("Пример ", 1, 1, "0000000000"));

                break;
        }

        // 2. Инициализация адаптера
        adapter = new ProductAdapter(productList, product -> {
            if (isOrderMode) {
                showQuantityDialog(product); // Режим ЗАКАЗА (Без штрих-кода)
            } else {
                showProductInfo(product);    // Режим КАТАЛОГА (Со штрих-кодом)
            }
        });

        rv.setAdapter(adapter);
        return view;
    }

    // КАРТОЧКА 1: ПРОСМОТР (ИНФОРМАЦИЯ + ШТРИХ-КОД)
    private void showProductInfo(Product product) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_quantity, null);
        dialog.setContentView(view);

        // Прячем кнопки заказа
        view.findViewById(R.id.btnPlus).setVisibility(View.GONE);
        view.findViewById(R.id.btnMinus).setVisibility(View.GONE);
        view.findViewById(R.id.etSheetQuantity).setVisibility(View.GONE);

        TextView tvTitle = view.findViewById(R.id.tvSheetTitle);
        MaterialButton btnConfirm = view.findViewById(R.id.btnConfirm);

        // Формируем текст: здесь штрих-код НУЖЕН
        String info = product.getName() +
                "\n\nШтрих-код: " + product.getBarcode() +
                "\nЦена: " + product.getPrice() + " ֏" +
                "\nВ коробке: " + product.getItemsPerBox() + " шт.";

        tvTitle.setText(info);
        btnConfirm.setText("Закрыть");
        btnConfirm.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // КАРТОЧКА 2: ЗАКАЗ (ТОЛЬКО НАЗВАНИЕ И КНОПКИ)
    private void showQuantityDialog(Product product) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_quantity, null);
        dialog.setContentView(view);

        TextView tvTitle = view.findViewById(R.id.tvSheetTitle);
        EditText etQuantity = view.findViewById(R.id.etSheetQuantity);
        View btnPlus = view.findViewById(R.id.btnPlus);
        View btnMinus = view.findViewById(R.id.btnMinus);
        MaterialButton btnConfirm = view.findViewById(R.id.btnConfirm);
        View btnDelete = view.findViewById(R.id.btnDeleteFromCart);

        // УСТАНАВЛИВАЕМ ТОЛЬКО НАЗВАНИЕ (Штрих-код здесь не нужен)
        tvTitle.setText(product.getName());

        if (CartManager.getInstance().hasProduct(product.getName())) {
            Integer currentQty = CartManager.getInstance().getCartItems().get(product.getName());
            etQuantity.setText(String.valueOf(currentQty != null ? currentQty : 1));
            if (btnDelete != null) btnDelete.setVisibility(View.VISIBLE);
            btnConfirm.setText("Изменить");
        }

        btnPlus.setOnClickListener(v -> {
            String s = etQuantity.getText().toString();
            int val = Integer.parseInt(s.isEmpty() ? "0" : s);
            etQuantity.setText(String.valueOf(val + 1));
        });

        btnMinus.setOnClickListener(v -> {
            String s = etQuantity.getText().toString();
            int val = Integer.parseInt(s.isEmpty() ? "0" : s);
            if (val > 1) etQuantity.setText(String.valueOf(val - 1));
        });

        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> {
                CartManager.getInstance().addProduct(product.getName(), 0);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            });
        }

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