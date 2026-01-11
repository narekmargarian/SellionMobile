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
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.sellion.mobile.R;
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.ReturnEntity;
import com.sellion.mobile.handler.BackPressHandler;
import com.sellion.mobile.helper.NavigationHelper;
import com.sellion.mobile.managers.CartManager;
import com.sellion.mobile.managers.ReturnManager;

import java.util.HashMap;
import java.util.Map;


public class ReturnDetailsFragment extends BaseFragment implements BackPressHandler {

    private TextView tvStoreName;
    private ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Используем XML для оформления деталей возврата
        View view = inflater.inflate(R.layout.fragment_return_details, container, false);

        TabLayout tabLayout = view.findViewById(R.id.returnTabLayout);
        viewPager = view.findViewById(R.id.returnViewPager);
        ImageButton btnBack = view.findViewById(R.id.btnBackFromReturn);
        View btnSave = view.findViewById(R.id.btnSaveReturn);
        tvStoreName = view.findViewById(R.id.tvReturnStoreName);

        // Получаем имя магазина из аргументов
        if (getArguments() != null) {
            tvStoreName.setText(getArguments().getString("store_name"));

            // Если мы пришли из режима редактирования, заполняем ReturnManager данными
            if (getArguments().containsKey("edit_reason")) {
                ReturnManager.getInstance().setReturnReason(getArguments().getString("edit_reason"));
                ReturnManager.getInstance().setReturnDate(getArguments().getString("edit_date"));
            }
        }

        // Обновление списка товаров при переключении на вкладку "Возврат" (позиция 1)
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // В 2026 году здесь ПУСТО.
                // LiveData внутри CurrentOrderFragment сама обновит экран.
            }
        });

        // Настройка адаптера ViewPager2
        ReturnPagerAdapter adapter = new ReturnPagerAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);

        // Логика кнопки СОХРАНИТЬ
        btnSave.setOnClickListener(v -> {
            if (CartManager.getInstance().getCartItems().isEmpty()) {
                Toast.makeText(getContext(), "Список пуст! Добавьте товары.", Toast.LENGTH_SHORT).show();
            } else {
                saveReturnToDatabase();
            }
        });

        // Настройка вкладок
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Товары");
                    break;
                case 1:
                    tab.setText("Возврат");
                    break;
                case 2:
                    tab.setText("Причина");
                    break;
            }
        }).attach();

        // Обработка кнопки назад
        btnBack.setOnClickListener(v -> onBackPressedHandled());
        setupBackButton(btnBack, false);

        return view;
    }

    private void saveReturnToDatabase() {
        String storeName = tvStoreName.getText().toString();
        Map<String, Integer> currentItems = new HashMap<>(CartManager.getInstance().getCartItems());

        // Создаем Entity для Room
        ReturnEntity newReturn = new ReturnEntity();
        newReturn.shopName = storeName;
        newReturn.items = currentItems;
        newReturn.returnReason = ReturnManager.getInstance().getReturnReason();
        newReturn.returnDate = ReturnManager.getInstance().getReturnDate();
        newReturn.status = "PENDING"; // Устанавливаем статус для WorkManager

        // Сохраняем в фоновом потоке
        new Thread(() -> {
            AppDatabase.getInstance(requireContext())
                    .returnDao().insert(newReturn);

            requireActivity().runOnUiThread(() -> {
                CartManager.getInstance().clearCart();
                ReturnManager.getInstance().clear();
                Toast.makeText(getContext(), "Возврат сохранен в базу!", Toast.LENGTH_SHORT).show();
                // Навигация через Helper
                NavigationHelper.finishAndGoTo(getParentFragmentManager(), new ReturnsFragment());
            });
        }).start();
    }

    @Override
    public void onBackPressedHandled() {
        showSaveReturnDialog();
    }

    protected void showSaveReturnDialog() {
        if (!CartManager.getInstance().getCartItems().isEmpty()) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Завершение возврата")
                    .setMessage("Сохранить изменения перед выходом?")
                    .setPositiveButton("Да, сохранить", (dialog, which) -> saveReturnToDatabase())
                    .setNegativeButton("Нет", (dialog, which) -> {
                        CartManager.getInstance().clearCart();
                        ReturnManager.getInstance().clear();
                        // КЛЮЧЕВОЙ МОМЕНТ: возвращает на один экран назад (к выбору магазина)
                        getParentFragmentManager().popBackStack();
                    })
                    .setNeutralButton("Отмена", null)
                    .show();
        } else {
            CartManager.getInstance().clearCart();
            ReturnManager.getInstance().clear();
            getParentFragmentManager().popBackStack();
        }
    }

    // Адаптер вкладок
    private static class ReturnPagerAdapter extends FragmentStateAdapter {
        public ReturnPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @Override
        public int getItemCount() {
            return 3;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new CatalogFragment();      // Вкладка с каталогом товаров
                case 1:
                    return new CurrentReturnFragment(); // Вкладка со списком выбранного
                case 2:
                    return new ReturnInfoFragment();   // Вкладка с выбором причины и даты
                default:
                    return new CatalogFragment();
            }
        }
    }
}