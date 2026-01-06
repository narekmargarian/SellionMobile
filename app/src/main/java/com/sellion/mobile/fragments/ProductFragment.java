package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product, container, false);

        if (getArguments() != null) {
            isOrderMode = getArguments().getBoolean("is_order_mode", false);
        }
        String category = getArguments() != null ? getArguments().getString("category_name") : "Товары";

        TextView tvTitle = view.findViewById(R.id.tvCategoryTitle);
        tvTitle.setText(category);

        ImageButton btnBack = view.findViewById(R.id.btnBackProducts);
        setupBackButton(btnBack, false);

        RecyclerView rv = view.findViewById(R.id.recyclerViewProducts);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        List<Product> productList = getProductsByCategory(category);

        adapter = new ProductAdapter(productList, product -> {
            if (isOrderMode) {
                showQuantityDialog(product);
            } else {
                showProductInfo(product);
            }
        });

        rv.setAdapter(adapter);
        return view;
    }

    private List<Product> getProductsByCategory(String category) {
        List<Product> productList = new ArrayList<>();
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
                productList.add(new Product("Пример", 1, 1, "0000000000"));
        }
        return productList;
    }

    private void showProductInfo(Product product) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_quantity, null);
        dialog.setContentView(view);

        view.findViewById(R.id.btnPlus).setVisibility(View.GONE);
        view.findViewById(R.id.btnMinus).setVisibility(View.GONE);
        view.findViewById(R.id.etSheetQuantity).setVisibility(View.GONE);

        TextView tvTitle = view.findViewById(R.id.tvSheetTitle);
        MaterialButton btnConfirm = view.findViewById(R.id.btnConfirm);

        String info = product.getName() +
                "\n\nШтрих-код: " + product.getBarcode() +
                "\nЦена: " + product.getPrice() + " ֏" +
                "\nВ коробке: " + product.getItemsPerBox() + " шт.";

        tvTitle.setText(info);
        btnConfirm.setText("Закрыть");
        btnConfirm.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showQuantityDialog(Product product) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());

        // ПРОВЕРКА РОДИТЕЛЯ: выбираем какой XML загрузить
        int layoutId = R.layout.layout_bottom_sheet_quantity; // Стандарт: Заказ
        if (isInsideReturnProcess()) {
            layoutId = R.layout.layout_bottom_sheet_return; // Новый: Возврат
        }

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
                notifyParentRefresh();
                dialog.dismiss();
            });
        }

        btnConfirm.setOnClickListener(v -> {
            String qtyS = etQuantity.getText().toString();
            int qty = qtyS.isEmpty() ? 0 : Integer.parseInt(qtyS);
            CartManager.getInstance().addProduct(product.getName(), qty);
            adapter.notifyDataSetChanged();
            notifyParentRefresh();
            dialog.dismiss();
        });

        dialog.show();
    }

    // Рекурсивная проверка для ViewPager2 (стандарт 2026)
    private boolean isInsideReturnProcess() {
        Fragment parent = getParentFragment();
        while (parent != null) {
            if (parent instanceof ReturnDetailsFragment) return true;
            parent = parent.getParentFragment();
        }
        return false;
    }

    private void notifyParentRefresh() {
        Fragment parent = getParentFragment();
        // Пытаемся найти родительский контейнер (Store или Return)
        while (parent != null) {
            ViewPager2 vp = null;
            if (parent instanceof StoreDetailsFragment) {
                vp = ((StoreDetailsFragment) parent).getViewPager();
            } else if (parent instanceof ReturnDetailsFragment) {
                // В ReturnDetailsFragment метод getViewPager должен быть public или найден через findViewById
                vp = parent.getView().findViewById(R.id.storeViewPager);
            }

            if (vp != null) {
                // Вкладка 1 всегда "Выбрано" (Заказ или Возврат)
                Fragment tab = parent.getChildFragmentManager().findFragmentByTag("f" + vp.getId() + ":1");
                if (tab instanceof OrderTabFragmentInterface) {
                    ((OrderTabFragmentInterface) tab).refreshOrderList();
                }
                break;
            }
            parent = parent.getParentFragment();
        }
    }

    public interface OrderTabFragmentInterface {
        void refreshOrderList();
    }
}
