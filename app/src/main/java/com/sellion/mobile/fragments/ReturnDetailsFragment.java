package com.sellion.mobile.fragments;

import android.content.Context;
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
import com.sellion.mobile.managers.SessionManager;

import java.util.HashMap;


public class ReturnDetailsFragment extends BaseFragment implements BackPressHandler {

    private TextView tvStoreName;
    private ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_return_details, container, false);

        TabLayout tabLayout = view.findViewById(R.id.returnTabLayout);
        viewPager = view.findViewById(R.id.returnViewPager);
        ImageButton btnBack = view.findViewById(R.id.btnBackFromReturn);
        View btnSave = view.findViewById(R.id.btnSaveReturn);
        tvStoreName = view.findViewById(R.id.tvReturnStoreName);

        // --- ИСПРАВЛЕНИЕ: Получаем имя магазина ---
        if (getArguments() != null && getArguments().containsKey("store_name")) {
            tvStoreName.setText(getArguments().getString("store_name"));
        }

        viewPager.setAdapter(new ReturnPagerAdapter(this));
        viewPager.setOffscreenPageLimit(2);

        btnSave.setOnClickListener(v -> {
            if (CartManager.getInstance().getCartItems().isEmpty()) {
                Toast.makeText(getContext(), "Список пуст!", Toast.LENGTH_SHORT).show();
            } else {
                saveReturnToDatabase();
            }
        });

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

        // На кнопке "Назад" в тулбаре
        btnBack.setOnClickListener(v -> NavigationHelper.backToDashboard(getParentFragmentManager()));

        return view;
    }

    private void saveReturnToDatabase() {
        String storeName = tvStoreName.getText().toString();

        ReturnEntity newReturn = new ReturnEntity();
        newReturn.shopName = storeName;
        newReturn.items = new HashMap<>(CartManager.getInstance().getCartItems());
        newReturn.returnReason = ReturnManager.getInstance().getReturnReason();
        newReturn.returnDate = ReturnManager.getInstance().getReturnDate();
        newReturn.status = "PENDING";
        newReturn.managerId = SessionManager.getInstance().getManagerId();

        new Thread(() -> {
            try {
                // Используем ApplicationContext, чтобы избежать утечек памяти и I/O ошибок
                Context context = requireContext().getApplicationContext();
                AppDatabase.getInstance(requireContext().getApplicationContext()).returnDao().insert(newReturn);

                // Даем системе время "отпустить" файл базы
                Thread.sleep(250);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Проверка, что фрагмент еще существует (isAdded() — это правильно)
                        if (!isAdded()) return;

                        // Очищаем корзину и менеджер возвратов
                        CartManager.getInstance().clearCart();
                        ReturnManager.getInstance().clear();

                        Toast.makeText(requireContext().getApplicationContext(), "Возврат сохранен!", Toast.LENGTH_SHORT).show();

                        // ИСПОЛЬЗУЕМ HELPER ДЛЯ ПРАВИЛЬНОЙ НАВИГАЦИИ
                        // Это очистит стек и откроет список возвратов
                        NavigationHelper.finishAndGoTo(getParentFragmentManager(), new ReturnsFragment());
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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