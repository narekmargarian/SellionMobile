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
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.sellion.mobile.R;
import com.sellion.mobile.adapters.StorePagerAdapter;
import com.sellion.mobile.entity.CartManager;
import com.sellion.mobile.entity.Product;
import com.sellion.mobile.managers.OrderHistoryManager;

import java.util.List;


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

        tvStoreName = view.findViewById(R.id.tvStoreName);

        if (getArguments() != null) {
            tvStoreName.setText(getArguments().getString("store_name"));
        }

        StorePagerAdapter adapter = new StorePagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Кнопка сохранить внизу экрана
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

        // Кнопка "Назад" в тулбаре
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
                    returnToDashboard(); // Возврат на главную без сохранения
                })
                .setNeutralButton("Отмена", null)
                .show();
    }

    private void saveOrderToDatabase() {
        String storeName = tvStoreName.getText().toString();

        // 1. Сохраняем данные
        java.util.Map<String, Integer> currentItems = new java.util.HashMap<>(
                com.sellion.mobile.entity.CartManager.getInstance().getCartItems()
        );
        com.sellion.mobile.entity.OrderModel newOrder = new com.sellion.mobile.entity.OrderModel(storeName, currentItems);
        com.sellion.mobile.managers.OrderHistoryManager.getInstance().addOrder(newOrder);

        // 2. Очищаем корзину
        com.sellion.mobile.entity.CartManager.getInstance().clearCart();

        Toast.makeText(getContext(), "Заказ успешно сохранен!", Toast.LENGTH_SHORT).show();

        if (getActivity() != null) {
            // 1. Стираем историю сбора заказа (чтобы нельзя было вернуться в пустой шаблон)
            getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            // 2. Ставим Dashboard в основу (он увидит ID из SessionManager)
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment())
                    .commit();

            // 3. Открываем список заказов поверх
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new OrdersFragment())
                    .addToBackStack(null)
                    .commit();
        }
    }

    // Метод для полной очистки стека и возврата на Dashboard
    private void returnToDashboard() {
        if (getActivity() != null) {
            // Очищаем весь BackStack, чтобы кнопка "назад" не возвращала в пустой заказ
            getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            // Заменяем текущий фрагмент на Dashboard (Главную страницу)
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment())
                    .commit();
        }
    }

    private boolean checkItemsInBasket() {
        return !CartManager.getInstance().getCartItems().isEmpty();
    }
}