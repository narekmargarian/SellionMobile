package com.sellion.mobile.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.sellion.mobile.R;
import com.sellion.mobile.activities.HostActivity;
import com.sellion.mobile.adapters.ProductAdapter;
import com.sellion.mobile.api.ApiClient;
import com.sellion.mobile.api.ApiService;
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.CartEntity;
import com.sellion.mobile.entity.ProductEntity;
import com.sellion.mobile.model.Product;
import com.sellion.mobile.managers.CartManager;

import java.util.ArrayList;
import java.util.List;



public class ProductFragment extends BaseFragment {
    private ProductAdapter adapter;
    private boolean isOrderMode = false;
    private boolean isActuallyReturn = false;
    private String currentCategory;
    private List<Product> allProducts = new ArrayList<>();
    private static final String TAG = "PRODUCT_FRAG";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product, container, false);

        if (getArguments() != null) {
            isOrderMode = getArguments().getBoolean("is_order_mode", false);
            isActuallyReturn = getArguments().getBoolean("is_actually_return", false);
            currentCategory = getArguments().getString("category_name");
        }

        TextView tvTitle = view.findViewById(R.id.tvCategoryTitle);
        tvTitle.setText(currentCategory);

        RecyclerView rv = view.findViewById(R.id.recyclerViewProducts);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        boolean showStock = isOrderMode && !isActuallyReturn;

        // Инициализируем адаптер пустым списком
        adapter = new ProductAdapter(new ArrayList<>(), product -> {
            if (isOrderMode || isActuallyReturn) showQuantityDialog(product);
            else showProductInfo(product);
        }, showStock);

        rv.setAdapter(adapter);

        // Живое наблюдение за корзиной
        AppDatabase.getInstance(requireContext()).cartDao().getCartItemsLive().observe(getViewLifecycleOwner(), cartItems -> {
            if (adapter != null) adapter.setItemsInCart(cartItems);
        });

        loadProductsFromLocalDB();

        ImageButton btnBack = view.findViewById(R.id.btnBackProducts);
        setupBackButton(btnBack, false);

        return view;
    }

    private void loadProductsFromLocalDB() {
        final Context appContext = requireContext().getApplicationContext();
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(appContext);
                List<ProductEntity> entities = db.productDao().getAllProductsSync();

                List<Product> products = new ArrayList<>();
                for (ProductEntity e : entities) {
                    // Передаем e.id для корректной работы подсветки
                    products.add(new Product(e.id, e.name, e.price, e.itemsPerBox, e.barcode, e.category, e.stockQuantity));
                }

                requireActivity().runOnUiThread(() -> {
                    allProducts = products;
                    if (allProducts.isEmpty()) {
                        Toast.makeText(appContext, "Товары не загружены. Сделайте синхронизацию!", Toast.LENGTH_LONG).show();
                    }
                    filterAndDisplayProducts();
                });
            } catch (Exception e) {
                HostActivity.logToFile(appContext, TAG, "Load Error: " + e.getMessage());
            }
        }).start();
    }

    private void filterAndDisplayProducts() {
        if (getView() == null) return;

        List<Product> filteredList = new ArrayList<>();
        for (Product p : allProducts) {
            if (currentCategory != null && currentCategory.equalsIgnoreCase(p.getCategory())) {
                filteredList.add(p);
            }
        }

        boolean showStock = isOrderMode && !isActuallyReturn;

        // Пересоздаем адаптер с отфильтрованным списком
        adapter = new ProductAdapter(filteredList, product -> {
            if (isOrderMode || isActuallyReturn) showQuantityDialog(product);
            else showProductInfo(product);
        }, showStock);

        RecyclerView rv = getView().findViewById(R.id.recyclerViewProducts);
        if (rv != null) {
            rv.setAdapter(adapter);

            // СРАЗУ подтягиваем текущую корзину синхронно, чтобы избежать мигания цвета
            final Context appContext = requireContext().getApplicationContext();
            new Thread(() -> {
                List<CartEntity> currentCart = AppDatabase.getInstance(appContext).cartDao().getCartItemsSync();
                requireActivity().runOnUiThread(() -> {
                    if (adapter != null) adapter.setItemsInCart(currentCart);
                });
            }).start();
        }
    }

    private void showQuantityDialog(Product product) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        int layoutId = isActuallyReturn ? R.layout.layout_bottom_sheet_return : R.layout.layout_bottom_sheet_quantity;
        View view = getLayoutInflater().inflate(layoutId, null);
        dialog.setContentView(view);

        TextView tvTitle = view.findViewById(R.id.tvSheetTitle);
        EditText etQuantity = view.findViewById(R.id.etSheetQuantity);

        View btnPlus = view.findViewById(R.id.btnPlus);
        View btnMinus = view.findViewById(R.id.btnMinus);
        com.google.android.material.button.MaterialButton btnConfirm = view.findViewById(R.id.btnConfirm);
        View btnDelete = view.findViewById(R.id.btnDeleteFromCart);

        tvTitle.setText(product.getName());

        final Context appContext = requireContext().getApplicationContext();
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(appContext);
                List<CartEntity> items = db.cartDao().getCartItemsSync();
                int currentQty = 1;
                boolean found = false;
                for (CartEntity item : items) {
                    if (item.productId == product.getId()) {
                        currentQty = item.quantity;
                        found = true;
                        break;
                    }
                }
                final int finalQty = currentQty;
                final boolean isFound = found;
                requireActivity().runOnUiThread(() -> {
                    etQuantity.setText(String.valueOf(finalQty));
                    if (isFound && btnDelete != null) btnDelete.setVisibility(View.VISIBLE);
                });
            } catch (Exception e) {
                HostActivity.logToFile(appContext, TAG, "Dialog Data Error: " + e.getMessage());
            }
        }).start();

        btnPlus.setOnClickListener(v -> {
            String s = etQuantity.getText().toString();
            int val = Integer.parseInt(s.isEmpty() ? "0" : s);
            // Проверка лимита склада только в режиме заказа
            if (!isActuallyReturn && val >= product.getStockQuantity()) {
                Toast.makeText(getContext(), "Достигнут лимит склада", Toast.LENGTH_SHORT).show();
            } else {
                etQuantity.setText(String.valueOf(val + 1));
            }
        });

        btnMinus.setOnClickListener(v -> {
            String s = etQuantity.getText().toString();
            int val = Integer.parseInt(s.isEmpty() ? "0" : s);
            if (val > 1) etQuantity.setText(String.valueOf(val - 1));
        });

        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> {
                CartManager.getInstance().addProduct(product.getId(), product.getName(), 0, product.getPrice());
                dialog.dismiss();
            });
        }

        btnConfirm.setOnClickListener(v -> {
            String qtyS = etQuantity.getText().toString();
            int qty = qtyS.isEmpty() ? 0 : Integer.parseInt(qtyS);

            if (!isActuallyReturn && qty > product.getStockQuantity()) {
                Toast.makeText(getContext(), "Недостаточно на складе", Toast.LENGTH_SHORT).show();
            } else {
                CartManager.getInstance().addProduct(product.getId(), product.getName(), qty, product.getPrice());
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showProductInfo(Product product) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_product_info, null);
        dialog.setContentView(view);

        ((TextView) view.findViewById(R.id.tvInfoName)).setText(product.getName());
        ((TextView) view.findViewById(R.id.tvInfoPrice)).setText("Цена: " + String.format("%,.0f", product.getPrice()) + " ֏");
        ((TextView) view.findViewById(R.id.tvInfoBarcode)).setText("Штрих код: " + product.getBarcode());
        ((TextView) view.findViewById(R.id.tvInfoBoxCount)).setText("В упаковке: " + product.getItemsPerBox());

        view.findViewById(R.id.btnCloseInfo).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
