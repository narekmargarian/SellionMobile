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

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.sellion.mobile.R;
import com.sellion.mobile.adapters.CartAdapter;
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.CartEntity;
import com.sellion.mobile.entity.Product;
import com.sellion.mobile.managers.CartManager;

import java.util.ArrayList;
import java.util.List;

public class CurrentOrderFragment extends BaseFragment {
    private RecyclerView rv;
    private TextView tvTotalSum;
    private TextView tvEmptyOrder;
    private CartAdapter adapter;
    private final List<Product> selectedProducts = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_current_order, container, false);

        rv = view.findViewById(R.id.rvCurrentOrder);
        tvTotalSum = view.findViewById(R.id.tvTotalOrderSum);
        tvEmptyOrder = view.findViewById(R.id.tvOrderDetails);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        // Инициализируем адаптер
        adapter = new CartAdapter(selectedProducts, this::showEditDialog);
        rv.setAdapter(adapter);

        initSwipeToDelete();

        // --- ПОДПИСКА НА LIVEDATA (ROOM) ---
        // Используем ссылку на метод :: для автоматического обновления
        AppDatabase.getInstance(requireContext()).cartDao().getCartItemsLive()
                .observe(getViewLifecycleOwner(), this::updateUI);

        return view;
    }

    // Метод обновлен для автоматического получения данных из LiveData
    public void updateUI(List<CartEntity> cartItems) {
        selectedProducts.clear();
        double totalAmount = 0;

        if (cartItems == null || cartItems.isEmpty()) {
            tvTotalSum.setText("0 ֏");
            if (tvEmptyOrder != null) {
                tvEmptyOrder.setVisibility(View.VISIBLE);
                tvEmptyOrder.setText("В заказе пока ничего нет");
            }
            adapter.notifyDataSetChanged();
            return;
        }

        if (tvEmptyOrder != null) tvEmptyOrder.setVisibility(View.GONE);

        for (CartEntity item : cartItems) {
            int price = getPriceForProduct(item.productName);
            totalAmount += (price * item.quantity);
            // Создаем временный объект Product для корректного отображения в адаптере
            selectedProducts.add(new Product(item.productName, price, 0, ""));
        }

        tvTotalSum.setText(String.format("%,.0f ֏", totalAmount));
        adapter.notifyDataSetChanged();
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

                    // Удаляем через CartManager (который удалит запись из Room)
                    CartManager.getInstance().addProduct(productToDelete.getName(), 0);
                    Toast.makeText(getContext(), "Удалено: " + productToDelete.getName(), Toast.LENGTH_SHORT).show();
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

        // Получаем текущее количество из Room асинхронно
        new Thread(() -> {
            List<CartEntity> items = AppDatabase.getInstance(requireContext()).cartDao().getCartItemsSync();
            int currentQty = 1;
            for (CartEntity item : items) {
                if (item.productName.equals(product.getName())) {
                    currentQty = item.quantity;
                    break;
                }
            }
            final int finalQty = currentQty;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> etQuantity.setText(String.valueOf(finalQty)));
            }
        }).start();

        btnPlus.setOnClickListener(v -> {
            String valStr = etQuantity.getText().toString();
            int val = Integer.parseInt(valStr.isEmpty() ? "0" : valStr);
            etQuantity.setText(String.valueOf(val + 1));
        });

        btnMinus.setOnClickListener(v -> {
            String valStr = etQuantity.getText().toString();
            int val = Integer.parseInt(valStr.isEmpty() ? "0" : valStr);
            if (val > 1) etQuantity.setText(String.valueOf(val - 1));
        });

        btnDelete.setOnClickListener(v -> {
            CartManager.getInstance().addProduct(product.getName(), 0);
            dialog.dismiss();
        });

        btnConfirm.setOnClickListener(v -> {
            String qtyS = etQuantity.getText().toString();
            if (!qtyS.isEmpty()) {
                CartManager.getInstance().addProduct(product.getName(), Integer.parseInt(qtyS));
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private int getPriceForProduct(String name) {
        if (name == null) return 0;
        switch (name) {
            case "Чипсы кокосовые ВМ Оригинальные":
                return 730;
            case "Чипсы кокосовые ВМ Соленая карамель":
                return 730;
            case "Чипсы кокосовые Costa Cocosta":
                return 430;
            case "Чипсы кокосовые Costa Cocosta Васаби":
                return 430;
            case "Шарики Манго в какао-глазури ВМ":
                return 930;
            case "Шарики Манго в белой глазури ВМ":
                return 930;
            case "Шарики Банано в глазури ВМ":
                return 730;
            case "Шарики Имбирь сладкий в глазури ВМ":
                return 930;
            case "Чай ВМ Лемонграсс и ананас":
                return 1690;
            case "Чай ВМ зеленый с фруктами":
                return 1690;
            case "Чай ВМ черный Мята и апельсин":
                return 1690;
            case "Чай ВМ черный Черника и манго":
                return 1990;
            case "Чай ВМ черный Шишки и саган-дайля":
                return 1990;
            case "Чай ВМ зеленый Жасмин и манго":
                return 1990;
            case "Чай ВМ черный Цветочное манго":
                return 590;
            case "Чай ВМ черный Шишки и клюква":
                return 790;
            case "Чай ВМ черный Нежная черника":
                return 790;
            case "Чай ВМ черный Ассам Цейлон":
                return 1190;
            case "Чай ВМ черный \"Хвойный\"":
                return 790;
            case "Чай ВМ черный \"Русский березовый\"":
                return 790;
            case "Чай ВМ черный Шишки и малина":
                return 790;
            case "Сух. Манго сушеное Вкусы мира":
                return 1490;
            case "Сух. Манго сушеное ВМ Чили":
                return 1490;
            case "Сух. Папайя сушеная Вкусы мира":
                return 1190;
            case "Сух. Манго шарики из сушеного манго":
                return 1190;
            case "Сух. Манго Сушеное LikeDay (250г)":
                return 2490;
            case "Сух. Манго Сушеное LikeDay (100г)":
                return 1190;
            case "Сух.Бананы вяленые Вкусы мира":
                return 1190;
            case "Сух.Джекфрут сушеный Вкусы мира":
                return 1190;
            case "Сух.Ананас сушеный Вкусы мира":
            default:
                return 0;
        }
    }

}