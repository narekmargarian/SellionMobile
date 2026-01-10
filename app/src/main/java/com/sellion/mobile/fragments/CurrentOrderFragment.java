package com.sellion.mobile.fragments;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;
import com.sellion.mobile.adapters.CartAdapter;
import com.sellion.mobile.entity.CartManager;
import com.sellion.mobile.entity.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CurrentOrderFragment extends BaseFragment {
    private RecyclerView rv;
    private TextView tvTotalSum;
    private TextView tvEmptyOrder;
    private final List<Product> selectedProducts = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_current_order, container, false);

        rv = view.findViewById(R.id.rvCurrentOrder);
        tvTotalSum = view.findViewById(R.id.tvTotalOrderSum);
        tvEmptyOrder = view.findViewById(R.id.tvOrderDetails); // Текст "Пусто"

        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        // Инициализируем свайп один раз
        initSwipeToDelete();

        return view;
    }

    // Метод для обновления данных
    public void updateUI() {
        if (getContext() == null) return;

        Map<String, Integer> cartData = CartManager.getInstance().getCartItems();
        selectedProducts.clear();
        double totalAmount = 0;

        // Если корзина пуста
        CartAdapter adapter;
        if (cartData.isEmpty()) {
            if (tvTotalSum != null) tvTotalSum.setText("0 ֏");
            if (tvEmptyOrder != null) {
                tvEmptyOrder.setVisibility(View.VISIBLE);
                tvEmptyOrder.setText("В заказе пока ничего нет");
            }
            adapter = new CartAdapter(selectedProducts, this::showEditDialog);
            rv.setAdapter(adapter);
            return;
        }

        // Если товары есть
        if (tvEmptyOrder != null) tvEmptyOrder.setVisibility(View.GONE);

        for (Map.Entry<String, Integer> entry : cartData.entrySet()) {
            String name = entry.getKey();
            int qty = entry.getValue();
            int price = getPriceForProduct(name);

            totalAmount += (price * qty);
            selectedProducts.add(new Product(name, price, 0, ""));
        }

        // Обновляем общую сумму
        if (tvTotalSum != null) {
            tvTotalSum.setText(String.format("%,.0f ֏", totalAmount));
        }

        // Пересоздаем адаптер для гарантии отрисовки
        adapter = new CartAdapter(selectedProducts, this::showEditDialog);
        rv.setAdapter(adapter);
    }



    private void initSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

//            @Override
//            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
//                int position = viewHolder.getAdapterPosition();
//                if (position != RecyclerView.NO_POSITION) {
//                    Product productToDelete = selectedProducts.get(position);
//                    CartManager.getInstance().addProduct(productToDelete.getName(), 0);
//                    updateUI(); // Сразу пересчитываем сумму
//                    Toast.makeText(getContext(), "Удалено: " + productToDelete.getName(), Toast.LENGTH_SHORT).show();
//                }
//            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();

                if (position != RecyclerView.NO_POSITION) {
                    Product productToDelete = selectedProducts.get(position);

                    CartManager.getInstance()
                            .addProduct(productToDelete.getName(), 0);

                    updateUI(); // пересчёт суммы

                    Toast.makeText(
                            getContext(),
                            "Удалено: " + productToDelete.getName(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX < 0) {
                    View itemView = viewHolder.itemView;
                    Paint p = new Paint();
                    p.setColor(Color.RED);
                    c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom(), p);
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        new ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(rv);
    }

    private void showEditDialog(Product product) {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_quantity, null);
        dialog.setContentView(view);

        TextView tvTitle = view.findViewById(R.id.tvSheetTitle);
        EditText etQuantity = view.findViewById(R.id.etSheetQuantity);
        View btnPlus = view.findViewById(R.id.btnPlus);
        View btnMinus = view.findViewById(R.id.btnMinus);
        View btnConfirm = view.findViewById(R.id.btnConfirm);
        TextView btnDelete = view.findViewById(R.id.btnDeleteFromCart);

        tvTitle.setText(product.getName());
        btnDelete.setVisibility(View.VISIBLE);

        Integer currentQty = CartManager.getInstance().getCartItems().get(product.getName());
        etQuantity.setText(String.valueOf(currentQty != null ? currentQty : 1));

        btnPlus.setOnClickListener(v -> {
            int val = Integer.parseInt(etQuantity.getText().toString());
            etQuantity.setText(String.valueOf(val + 1));
        });

        btnMinus.setOnClickListener(v -> {
            int val = Integer.parseInt(etQuantity.getText().toString());
            if (val > 1) etQuantity.setText(String.valueOf(val - 1));
        });

        btnDelete.setOnClickListener(v -> {
            CartManager.getInstance().addProduct(product.getName(), 0);
            updateUI();
            dialog.dismiss();
        });

        btnConfirm.setOnClickListener(v -> {
            String qtyS = etQuantity.getText().toString();
            if (!qtyS.isEmpty()) {
                CartManager.getInstance().addProduct(product.getName(), Integer.parseInt(qtyS));
                updateUI();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private int getPriceForProduct(String name) {
        if (name == null) return 0;
        switch (name) {
            case "Шоколад Аленка":
                return 500;
            case "Шоколад 1":
                return 1000;
            case "Шоколад 2":
                return 1500;
            case "Шоколад 3":
                return 1250;
            case "Конфеты Мишка":
                return 1790;
            case "Вафли Артек":
                return 960;
            case "Вафли 1":
                return 630;
            case "Вафли 2":
                return 2560;
            case "Вафли 3":
                return 2430;
            case "Lays 1":
                return 1020;
            case "Lays 2":
                return 4450;
            case "Lays Сметана/Зелень":
                return 440;
            case "Pringles Оригинал":
                return 750;
            case "Pringles 1":
                return 3390;
            case "Pringles 2":
                return 890;
            case "Чай 1":
                return 220;
            case "Чай 2":
                return 9530;
            case "Чай 3":
                return 1990;
            case "Чай Ахмад":
                return 50000000;
            default:
                return 0;
        }
    }

}