package com.sellion.mobile.fragments;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sellion.mobile.R;
import com.sellion.mobile.adapters.CartAdapter;
import com.sellion.mobile.entity.CartManager;
import com.sellion.mobile.entity.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CurrentOrderFragment extends Fragment {
    private RecyclerView rv;
    private CartAdapter adapter;
    private List<Product> selectedProducts = new ArrayList<>(); // Храним список здесь

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_current_order, container, false);
        rv = view.findViewById(R.id.rvCurrentOrder);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        View oldTv = view.findViewById(R.id.tvOrderDetails);
        if (oldTv != null) oldTv.setVisibility(View.GONE);

        // Инициализируем свайп ОДИН РАЗ при создании экрана
        initSwipeToDelete();

        updateUI();
        return view;
    }

    private void updateUI() {
        Map<String, Integer> cartData = CartManager.getInstance().getCartItems();
        selectedProducts.clear(); // Очищаем старый список

        for (String name : cartData.keySet()) {
            selectedProducts.add(new Product(name, ""));
        }

        adapter = new CartAdapter(selectedProducts, product -> {
            showEditDialog(product);
        });

        rv.setAdapter(adapter);
    }

    private void initSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Product productToDelete = selectedProducts.get(position);

                // Удаляем из памяти
                CartManager.getInstance().addProduct(productToDelete.getName(), 0);

                // Обновляем UI
                updateUI();
                Toast.makeText(getContext(), "Удалено: " + productToDelete.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                // Рисуем красный фон при свайпе влево
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX < 0) {
                    View itemView = viewHolder.itemView;
                    Paint p = new Paint();
                    p.setColor(Color.RED);
                    c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom(), p);
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(rv);
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

        int currentQty = CartManager.getInstance().getCartItems().get(product.getName());
        etQuantity.setText(String.valueOf(currentQty));

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
}