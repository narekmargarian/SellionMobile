package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.sellion.mobile.R;
import com.sellion.mobile.adapters.StorePagerAdapter;
import com.sellion.mobile.entity.CartManager;
import com.sellion.mobile.managers.OrderHistoryManager;


public class StoreDetailsFragment extends Fragment {
    private TextView tvStoreName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_store_details, container, false);

        TabLayout tabLayout = view.findViewById(R.id.storeTabLayout);
        ViewPager2 viewPager = view.findViewById(R.id.storeViewPager);
        ImageButton btnBack = view.findViewById(R.id.btnBackToRoute);
        View btnSave = view.findViewById(R.id.btnSaveFullOrder);

        // 2. ИНИЦИАЛИЗИРУЕМ (без слова TextView впереди)
        tvStoreName = view.findViewById(R.id.tvStoreName);

        if (getArguments() != null) {
            tvStoreName.setText(getArguments().getString("store_name"));
        }

        StorePagerAdapter adapter = new StorePagerAdapter(this);
        viewPager.setAdapter(adapter);

        btnSave.setOnClickListener(v -> {
            if (!checkItemsInBasket()) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Пустой заказ")
                        .setMessage("Вы не выбрали ни одного товара. Добавьте товары перед сохранением.")
                        .setPositiveButton("ОК", null)
                        .show();
            } else {
                saveOrderToDatabase();
            }
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Товары"); break;
                case 1: tab.setText("Заказ"); break;
                case 2: tab.setText("О заказе"); break;
            }
        }).attach();

        btnBack.setOnClickListener(v -> showSaveOrderDialog());

        return view;
    }

    private void showSaveOrderDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Завершение заказа")
                .setMessage("Сохранить заказ перед выходом?")
                .setPositiveButton("Да, сохранить", (dialog, which) -> saveOrderToDatabase())
                .setNegativeButton("Нет", (dialog, which) -> {
                    CartManager.getInstance().clearCart();
                    getParentFragmentManager().popBackStack();
                })
                .setNeutralButton("Отмена", null)
                .show();
    }

    private void saveOrderToDatabase() {
        // 1. Получаем название магазина
        String storeName = tvStoreName.getText().toString();

        // 2. Получаем текущие товары из корзины (делаем копию мапы)
        java.util.Map<String, Integer> currentItems = new java.util.HashMap<>(
                com.sellion.mobile.entity.CartManager.getInstance().getCartItems()
        );

        // 3. Создаем объект OrderModel (теперь типы совпадают)
        com.sellion.mobile.entity.OrderModel newOrder = new com.sellion.mobile.entity.OrderModel(storeName, currentItems);

        // 4. Сохраняем ОБЪЕКТ в историю
        com.sellion.mobile.managers.OrderHistoryManager.getInstance().addOrder(newOrder);

        // 5. Очищаем текущую корзину
        com.sellion.mobile.entity.CartManager.getInstance().clearCart();

        Toast.makeText(getContext(), "Заказ успешно сохранен!", Toast.LENGTH_SHORT).show();

        if (getActivity() != null) {
            // Очищаем стек и возвращаемся на главный экран
            getParentFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new com.sellion.mobile.fragments.OrdersFragment())
                    .commit();
        }
    }

    private boolean checkItemsInBasket() {
        return !CartManager.getInstance().getCartItems().isEmpty();
    }
}