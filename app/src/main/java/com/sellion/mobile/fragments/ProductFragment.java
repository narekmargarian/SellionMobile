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
import com.sellion.mobile.entity.ProductInfoSheet;

import java.util.ArrayList;
import java.util.List;


public class ProductFragment extends Fragment {
    private ProductAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product, container, false);

        String category = getArguments() != null ? getArguments().getString("category_name") : "Товары";
        TextView tvTitle = view.findViewById(R.id.tvCategoryTitle);
        tvTitle.setText(category);

        ImageButton btnBack = view.findViewById(R.id.btnBackProducts);
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        RecyclerView rv = view.findViewById(R.id.recyclerViewProducts);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        List<Product> productList = new ArrayList<>();
        // ... (ваш существующий switch с наполнением productList остается без изменений) ...
        switch (category) {
            case "Сладкое":
                productList.add(new Product("Шоколад Аленка", 500,20));
                productList.add(new Product("Конфеты Мишка", 2500,10));
                productList.add(new Product("Вафли Артек", 3500,15));
                break;
            case "Чипсы":
                productList.add(new Product("Lays Сметана/Зелень", 785,3));
                productList.add(new Product("Pringles Оригинал", 789,6));
                break;
            // Добавьте остальные кейсы по аналогии
        }

        // ИСПРАВЛЕНО: Теперь при клике открываем Инфо-карточку
        adapter = new ProductAdapter(productList, product -> {
            // ПО ОБЫЧНОМУ КЛИКУ — ТОЛЬКО ИНФОРМАЦИЯ
            ProductInfoSheet infoSheet = new ProductInfoSheet(product);
            infoSheet.show(getChildFragmentManager(), "product_info");
        });

        rv.setAdapter(adapter);
        return view;
    }


    // 2. Ваш существующий метод заказа (теперь вызывается из карточки или по вашему желанию)
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

        if (CartManager.getInstance().hasProduct(product.getName())) {
            Integer currentQty = CartManager.getInstance().getCartItems().get(product.getName());
            etQuantity.setText(String.valueOf(currentQty != null ? currentQty : 1));
            btnDelete.setVisibility(View.VISIBLE);
            ((MaterialButton)btnConfirm).setText("Изменить");
        }

        btnPlus.setOnClickListener(v -> {
            int val = Integer.parseInt(etQuantity.getText().toString().isEmpty() ? "0" : etQuantity.getText().toString());
            etQuantity.setText(String.valueOf(val + 1));
        });

        btnMinus.setOnClickListener(v -> {
            int val = Integer.parseInt(etQuantity.getText().toString().isEmpty() ? "0" : etQuantity.getText().toString());
            if (val > 1) etQuantity.setText(String.valueOf(val - 1));
        });

        btnDelete.setOnClickListener(v -> {
            CartManager.getInstance().addProduct(product.getName(), 0);
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