package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;
import com.sellion.mobile.adapters.CategoryAdapter;

import java.util.Arrays;
import java.util.List;


public class CatalogFragment extends BaseFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_catalog, container, false);

        // 1. Кнопка "Назад"
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        setupBackButton(btnBack, false);

        // 2. Настройка списка категорий
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewCatalog);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 3. Проверка контекста (Сбор заказа или Просмотр) для 2026 года
        boolean orderMode = isInsideStoreProcess();

        // Если мы внутри вкладок магазина — скрываем шапку (btnBack)
        if (orderMode) {
            btnBack.setVisibility(View.GONE);
        }

        // 4. Данные категорий
        List<String> categories = Arrays.asList("Сладкое", "Чай", "Чипсы", "Хлопья", "Весовые");

        // 5. Адаптер с логикой перехода и передачей режима
        CategoryAdapter adapter = new CategoryAdapter(categories, category -> {
            ProductFragment fragment = new ProductFragment();
            Bundle args = new Bundle();

            // Передаем название категории
            args.putString("category_name", category);

            // ПЕРЕДАЕМ РЕЖИМ (Важно для разделения Инфо/Заказ)
            args.putBoolean("is_order_mode", orderMode);

            fragment.setArguments(args);

            // Используем транзакцию через Activity для корректной навигации
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        recyclerView.setAdapter(adapter);

        return view;
    }

    /**
     * Надежный метод проверки нахождения внутри StoreDetailsFragment.
     * В 2026 году внутри ViewPager2 обычный instanceof getParentFragment() может вернуть null.
     */
    private boolean isInsideStoreProcess() {
        Fragment parent = getParentFragment();
        while (parent != null) {
            if (parent instanceof StoreDetailsFragment) {
                return true;
            }
            parent = parent.getParentFragment();
        }
        return false;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Дополнительная проверка видимости при создании View
        if (isInsideStoreProcess()) {
            View header = view.findViewById(R.id.btnBack);
            if (header != null) header.setVisibility(View.GONE);
        }
    }


}