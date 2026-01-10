package com.sellion.mobile.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.sellion.mobile.fragments.CatalogFragment;
import com.sellion.mobile.fragments.CurrentOrderFragment;
import com.sellion.mobile.fragments.OrderInfoFragment;

public class StorePagerAdapter extends FragmentStateAdapter {

    public StorePagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // 2026 стандарт: переключение между вкладками
        switch (position) {
            case 0:
                return new CatalogFragment();      // Твой готовый каталог
            case 1:
                return new CurrentOrderFragment(); // Список заказа
            case 2:
                return new OrderInfoFragment();    // Инфо о магазине
            default:
                return new CatalogFragment();
        }
    }


    @Override
    public int getItemCount() {
        return 3; // Всего 3 вкладки
    }
}
