package com.sellion.mobile.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.sellion.mobile.fragments.OrderClientFragment;
import com.sellion.mobile.fragments.OrderRouteFragment;

public class CreateOrderPagerAdapter extends FragmentStateAdapter {

    public CreateOrderPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) return new OrderRouteFragment();
        return new OrderClientFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}