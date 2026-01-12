package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;
import com.sellion.mobile.adapters.CategoryAdapter;
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.ProductEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class CatalogFragment extends BaseFragment {
    private boolean orderMode;
    private boolean isReturn;
    private RecyclerView recyclerView;
    private CategoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_catalog, container, false);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        recyclerView = view.findViewById(R.id.recyclerViewCatalog);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        orderMode = isInsideAnyProcess();
        isReturn = isInsideReturnProcess();

        // Если мы внутри процесса заказа/возврата, скрываем кнопку назад (управляет HostActivity)
        if (orderMode || isReturn) {
            btnBack.setVisibility(View.GONE);
        } else {
            btnBack.setVisibility(View.VISIBLE);
            setupBackButton(btnBack, false);
        }

        // ИЗМЕНЕНО: Загружаем категории из локальной базы Room
        loadCategoriesFromLocalDB();

        return view;
    }

    private void loadCategoriesFromLocalDB() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext().getApplicationContext());
            // Получаем все товары, чтобы вытащить из них уникальные категории
            List<ProductEntity> allProducts = db.productDao().getAllProductsSync();

            Set<String> uniqueCategories = new HashSet<>();
            for (ProductEntity p : allProducts) {
                if (p.category != null && !p.category.isEmpty()) {
                    uniqueCategories.add(p.category);
                }
            }

            // Преобразуем Set в отсортированный список
            List<String> categories = new ArrayList<>(uniqueCategories);
            Collections.sort(categories);

            requireActivity().runOnUiThread(() -> {
                if (categories.isEmpty()) {
                    Toast.makeText(getContext(), "Категории не найдены. Синхронизируйте данные.", Toast.LENGTH_LONG).show();
                }

                adapter = new CategoryAdapter(categories, category -> {
                    ProductFragment fragment = new ProductFragment();
                    Bundle args = new Bundle();
                    args.putString("category_name", category);
                    args.putBoolean("is_order_mode", orderMode);
                    args.putBoolean("is_actually_return", isReturn);
                    fragment.setArguments(args);

                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit();
                });
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }

    private boolean isInsideAnyProcess() {
        Fragment parent = getParentFragment();
        while (parent != null) {
            if (parent instanceof OrderDetailsFragment || parent instanceof ReturnDetailsFragment) return true;
            parent = parent.getParentFragment();
        }
        return false;
    }

    private boolean isInsideReturnProcess() {
        Fragment parent = getParentFragment();
        while (parent != null) {
            if (parent instanceof ReturnDetailsFragment) return true;
            parent = parent.getParentFragment();
        }
        return false;
    }
}