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
    private boolean orderMode;
    private boolean isReturn ;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_catalog, container, false);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        setupBackButton(btnBack, false);
        orderMode = isInsideAnyProcess();
        isReturn = isInsideReturnProcess();

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewCatalog);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Проверяем, находимся ли мы в любом процессе оформления


        if (orderMode || isReturn) {
            btnBack.setVisibility(View.GONE);
        }else {
            setupBackButton(btnBack, false);
        }

        List<String> categories = Arrays.asList("Сладкое", "Чай", "Чипсы", "Сухофрукты");

        CategoryAdapter adapter = new CategoryAdapter(categories, category -> {
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
        return view;
    }

    private boolean isInsideAnyProcess() {
        Fragment parent = getParentFragment();
        while (parent != null) {
            if (parent instanceof OrderDetailsFragment) {
                return true;
            }
            parent = parent.getParentFragment();
        }
        return false;
    }

    private boolean isInsideReturnProcess() {
        Fragment parent = getParentFragment();
        while (parent != null) {
            if (parent instanceof ReturnDetailsFragment) {
                return true;
            }
            parent = parent.getParentFragment();
        }
        return false;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (isInsideAnyProcess()) {
            View header = view.findViewById(R.id.btnBack);
            if (header != null) header.setVisibility(View.GONE);
        }
    }
}