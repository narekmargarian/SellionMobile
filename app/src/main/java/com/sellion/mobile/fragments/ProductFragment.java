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


public class ProductFragment extends BaseFragment {
    private ProductAdapter adapter;
    private boolean isOrderMode = false;
    private boolean isActuallyReturn = false;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product, container, false);

        if (getArguments() != null) {
            isOrderMode = getArguments().getBoolean("is_order_mode", false);
            isActuallyReturn = getArguments().getBoolean("is_actually_return", false);
        }


        String category = getArguments() != null ? getArguments().getString("category_name") : "Товары";

        TextView tvTitle = view.findViewById(R.id.tvCategoryTitle);
        tvTitle.setText(category);

        ImageButton btnBack = view.findViewById(R.id.btnBackProducts);
        setupBackButton(btnBack, false);

        RecyclerView rv = view.findViewById(R.id.recyclerViewProducts);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        if (category != null) {

            List<Product> productList = getProductsByCategory(category);
            adapter = new ProductAdapter(productList, product -> {
                if (isOrderMode || isActuallyReturn) {
                    showQuantityDialog(product);
                } else {
                    showProductInfo(product);
                }
            });


        } else {
            // TODO: 10.01.2026
        }


        rv.setAdapter(adapter);
        return view;
    }

    // ... метод getProductsByCategory остается без изменений ...

    private void showProductInfo(Product product) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_product_info, null);
        dialog.setContentView(view);


        TextView tvTitle = view.findViewById(R.id.tvInfoName);
        TextView tvInfoPrice = view.findViewById(R.id.tvInfoPrice);
        TextView tvBarcode = view.findViewById(R.id.tvInfoBarcode);
        TextView tvInfoDescription = view.findViewById(R.id.tvInfoDescription);
        TextView tvBoxCount = view.findViewById(R.id.tvInfoBoxCount);
        MaterialButton btnConfirm = view.findViewById(R.id.btnCloseInfo);

        String info = "Здесь будет детальное описание: вес, количество в коробке, срок годности и остаток на складе.";

        tvTitle.setText(product.getName());
        tvInfoPrice.setText("Цена: " + product.getPrice());
        tvBarcode.setText("Штрих код: " + product.getBarcode());
        tvInfoDescription.setText(info);
        tvBoxCount.setText("В упаковке: " + product.getItemsPerBox());


        btnConfirm.setText("Закрыть");
        btnConfirm.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showQuantityDialog(Product product) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        int layoutId = isActuallyReturn ?
                R.layout.layout_bottom_sheet_return :
                R.layout.layout_bottom_sheet_quantity;

        View view = getLayoutInflater().inflate(layoutId, null);
        dialog.setContentView(view);

        TextView tvTitle = view.findViewById(R.id.tvSheetTitle);
        EditText etQuantity = view.findViewById(R.id.etSheetQuantity);
        View btnPlus = view.findViewById(R.id.btnPlus);
        View btnMinus = view.findViewById(R.id.btnMinus);
        MaterialButton btnConfirm = view.findViewById(R.id.btnConfirm);
        View btnDelete = view.findViewById(R.id.btnDeleteFromCart);

        tvTitle.setText(product.getName());

        if (CartManager.getInstance().hasProduct(product.getName())) {
            Integer currentQty = CartManager.getInstance().getCartItems().get(product.getName());
            etQuantity.setText(String.valueOf(currentQty != null ? currentQty : 1));
            if (btnDelete != null) btnDelete.setVisibility(View.VISIBLE);

//            btnConfirm.setText("Изменить заказ");
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
//                notifyParentRefresh();
                dialog.dismiss();
            });
        }

        btnConfirm.setOnClickListener(v -> {
            String qtyS = etQuantity.getText().toString();
            int qty = qtyS.isEmpty() ? 0 : Integer.parseInt(qtyS);
            CartManager.getInstance().addProduct(product.getName(), qty);
            adapter.notifyDataSetChanged();
//            notifyParentRefresh();
            dialog.dismiss();
        });

        dialog.show();
    }


    private List<Product> getProductsByCategory(String category) {
        List<Product> productList = new ArrayList<>();

        // В 2026 году используем switch для фильтрации товаров по категориям
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
                // Если категория не найдена, возвращаем пустой список или тестовый товар
                productList.add(new Product("Тестовый товар", 0, 0, "0000000000000"));
                break;
        }
        return productList;
    }

}
