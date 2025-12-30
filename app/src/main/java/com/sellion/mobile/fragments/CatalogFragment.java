package com.sellion.mobile.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.Toolbar;

import com.sellion.mobile.R;
import com.sellion.mobile.adapters.CategoryAdapter;

import java.util.Arrays;
import java.util.List;


public class CatalogFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. Инфлейтим разметку
        View view = inflater.inflate(R.layout.fragment_catalog, container, false);

        // 2. Настраиваем кнопку "Назад"
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                // В 2025 году лучше использовать popBackStack для фрагментов
                getParentFragmentManager().popBackStack();
            }
        });

        // 3. Настраиваем список (RecyclerView)
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewCatalog);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 4. Данные категорий
        List<String> categories = Arrays.asList("Сладкое", "Чай", "Чипсы", "Хлопья", "Весовые");

        // 5. Создаем ОДИН адаптер с логикой перехода
        CategoryAdapter adapter = new CategoryAdapter(categories, category -> {
            // ЛОГИКА ПЕРЕХОДА
            ProductFragment fragment = new ProductFragment();
            Bundle args = new Bundle();
            args.putString("category_name", category);
            fragment.setArguments(args);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();

            // Опционально: показываем сообщение
            Toast.makeText(getContext(), "Открываем: " + category, Toast.LENGTH_SHORT).show();
        });

        // 6. Устанавливаем адаптер в список
        recyclerView.setAdapter(adapter);

        return view;
    }
}