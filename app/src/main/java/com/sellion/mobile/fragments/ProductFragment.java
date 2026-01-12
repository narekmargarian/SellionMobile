package com.sellion.mobile.fragments;

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

        adapter = new ProductAdapter(new ArrayList<>(), product -> {
            if (isOrderMode || isActuallyReturn) showQuantityDialog(product);
            else showProductInfo(product);
        });
        rv.setAdapter(adapter);

        AppDatabase.getInstance(requireContext()).cartDao().getCartItemsLive().observe(getViewLifecycleOwner(), cartItems -> {
            if (adapter != null) adapter.setItemsInCart(cartItems);
        });

        // ИЗМЕНЕНО: Теперь загружаем из локальной базы данных
        loadProductsFromLocalDB();

        ImageButton btnBack = view.findViewById(R.id.btnBackProducts);
        setupBackButton(btnBack, false);

        return view;
    }

    private void loadProductsFromLocalDB() {
        // Фоновый поток для Room
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext().getApplicationContext());
            // Получаем сущности из базы (те, что скачали в SyncFragment)
            List<ProductEntity> entities = db.productDao().getAllProductsSync();

            List<Product> products = new ArrayList<>();
            for (ProductEntity e : entities) {
                // Преобразуем Entity обратно в модель Product
                products.add(new Product(e.name, e.price, e.itemsPerBox, e.barcode, e.category));
            }

            // Обновляем UI
            requireActivity().runOnUiThread(() -> {
                allProducts = products;
                if (allProducts.isEmpty()) {
                    Toast.makeText(getContext(), "Товары не загружены. Сделайте синхронизацию!", Toast.LENGTH_LONG).show();
                }
                filterAndDisplayProducts();
            });
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

        // Обновляем список в существующем адаптере или создаем новый
        adapter = new ProductAdapter(filteredList, product -> {
            if (isOrderMode || isActuallyReturn) showQuantityDialog(product);
            else showProductInfo(product);
        });

        RecyclerView rv = getView().findViewById(R.id.recyclerViewProducts);
        if (rv != null) rv.setAdapter(adapter);
    }

    // Методы showQuantityDialog и showProductInfo остаются без изменений,
    // так как они и так работали с локальными данными и диалогами.

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

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            java.util.List<CartEntity> items = db.cartDao().getCartItemsSync();
            int currentQty = 1;
            boolean found = false;
            for (CartEntity item : items) {
                if (item.productName.equals(product.getName())) {
                    currentQty = item.quantity;
                    found = true;
                    break;
                }
            }
            final int finalQty = currentQty;
            final boolean isFound = found;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    etQuantity.setText(String.valueOf(finalQty));
                    if (isFound && btnDelete != null) btnDelete.setVisibility(View.VISIBLE);
                });
            }
        }).start();

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
                CartManager.getInstance().addProduct(product.getName(), 0, product.getPrice());
                dialog.dismiss();
            });
        }

        btnConfirm.setOnClickListener(v -> {
            String qtyS = etQuantity.getText().toString();
            int qty = qtyS.isEmpty() ? 0 : Integer.parseInt(qtyS);
            CartManager.getInstance().addProduct(product.getName(), qty, product.getPrice());
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showProductInfo(Product product) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_product_info, null);
        dialog.setContentView(view);
        ((TextView)view.findViewById(R.id.tvInfoName)).setText(product.getName());
        ((TextView)view.findViewById(R.id.tvInfoPrice)).setText("Цена: " + product.getPrice() + " ֏");
        ((TextView)view.findViewById(R.id.tvInfoBarcode)).setText("Штрих код: " + product.getBarcode());
        ((TextView)view.findViewById(R.id.tvInfoBoxCount)).setText("В упаковке: " + product.getItemsPerBox());
        view.findViewById(R.id.btnCloseInfo).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}