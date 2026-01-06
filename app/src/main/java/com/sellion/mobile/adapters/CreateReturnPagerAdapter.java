package com.sellion.mobile.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.sellion.mobile.fragments.OrderClientFragment;
import com.sellion.mobile.fragments.OrderRouteFragment;

public class CreateReturnPagerAdapter extends FragmentStateAdapter {
    public CreateReturnPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Здесь можно использовать те же фрагменты списка,
        // но в их адаптерах нужно будет вызвать onClientSelected именно для Возврата
        return position == 0 ? new OrderRouteFragment() : new OrderClientFragment();
    }

    @Override
    public int getItemCount() { return 2; }
}