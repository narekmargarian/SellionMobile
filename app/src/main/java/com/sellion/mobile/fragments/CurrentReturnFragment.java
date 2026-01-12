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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.sellion.mobile.R;
import com.sellion.mobile.adapters.CartAdapter;
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.CartEntity;
import com.sellion.mobile.model.Product;
import com.sellion.mobile.managers.CartManager;

import java.util.ArrayList;
import java.util.List;

public class CurrentReturnFragment extends BaseFragment {
    private RecyclerView rv;
    private TextView tvTotalSum;
    private TextView tvEmptyOrder;
    private CartAdapter adapter;
    private final List<Product> selectedProducts = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_current_return, container, false);

        rv = view.findViewById(R.id.rvCurrentReturn);
        tvTotalSum = view.findViewById(R.id.tvTotalReturnSum);
        tvEmptyOrder = view.findViewById(R.id.tvReturnDetails);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CartAdapter(selectedProducts, this::showEditDialog);
        rv.setAdapter(adapter);

        initSwipeToDelete();

        AppDatabase.getInstance(requireContext()).cartDao().getCartItemsLive()
                .observe(getViewLifecycleOwner(), this::updateUI);

        return view;
    }



    private void initSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAbsoluteAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Product productToDelete = selectedProducts.get(position);
                    // ИСПРАВЛЕНИЕ 1: Добавлена цена (productToDelete.getPrice())
                    CartManager.getInstance().addProduct(productToDelete.getName(), 0, productToDelete.getPrice());
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
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_return, null);
        dialog.setContentView(view);

        TextView tvTitle = view.findViewById(R.id.tvSheetTitle);
        EditText etQuantity = view.findViewById(R.id.etSheetQuantity);
        View btnPlus = view.findViewById(R.id.btnPlus);
        View btnMinus = view.findViewById(R.id.btnMinus);
        View btnConfirm = view.findViewById(R.id.btnConfirm);

        tvTitle.setText(product.getName());

        new Thread(() -> {
            List<CartEntity> items = AppDatabase.getInstance(requireContext()).cartDao().getCartItemsSync();
            int qty = 1;
            for (CartEntity item : items) {
                if (item.productName.equals(product.getName())) {
                    qty = item.quantity;
                    break;
                }
            }
            int finalQty = qty;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> etQuantity.setText(String.valueOf(finalQty)));
            }
        }).start();

        btnPlus.setOnClickListener(v -> {
            int val = Integer.parseInt(etQuantity.getText().toString());
            etQuantity.setText(String.valueOf(val + 1));
        });

        btnMinus.setOnClickListener(v -> {
            int val = Integer.parseInt(etQuantity.getText().toString());
            if (val > 1) etQuantity.setText(String.valueOf(val - 1));
        });

        btnConfirm.setOnClickListener(v -> {
            String qStr = etQuantity.getText().toString();
            if (!qStr.isEmpty()) {
                // ИСПРАВЛЕНИЕ 2: Добавлена цена (product.getPrice())
                CartManager.getInstance().addProduct(product.getName(), Integer.parseInt(qStr), product.getPrice());
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void updateUI(List<CartEntity> cartItems) {
        selectedProducts.clear();
        double totalAmount = 0;

        if (cartItems == null || cartItems.isEmpty()) {
            tvTotalSum.setText("0 ֏");
            if (tvEmptyOrder != null) {
                tvEmptyOrder.setVisibility(View.VISIBLE);
                tvEmptyOrder.setText("Пусто");
            }
            adapter.notifyDataSetChanged();
            return;
        }

        if (tvEmptyOrder != null) tvEmptyOrder.setVisibility(View.GONE);

        for (CartEntity item : cartItems) {
            totalAmount += (item.price * item.quantity);
            // ИСПРАВЛЕННАЯ СТРОКА (добавлена еще одна пустая строка в конце):
            selectedProducts.add(new Product(item.productName, item.price, 0, "", ""));
        }

        tvTotalSum.setText(String.format("%,.0f ֏", totalAmount));
        adapter.notifyDataSetChanged();
    }
}