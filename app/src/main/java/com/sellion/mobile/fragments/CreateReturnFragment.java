package com.sellion.mobile.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.sellion.mobile.R;
import com.sellion.mobile.adapters.CreateOrderPagerAdapter;
import com.sellion.mobile.adapters.CreateReturnPagerAdapter;


public class CreateReturnFragment  extends BaseFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_return_details, container, false); // переиспользуем XML

        ViewPager2 viewPager = view.findViewById(R.id.returnViewPager);
        TabLayout tabLayout = view.findViewById(R.id.returnTabLayout);
        ImageButton btnBack = view.findViewById(R.id.btnBackFromReturn);

        // Меняем заголовок
        TextView title = view.findViewById(R.id.tvReturnStoreName);
        if (title != null) title.setText("Выбор клиента (Возврат)");

        // Используем ваш CreateOrderPagerAdapter
        viewPager.setAdapter(new CreateReturnPagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Маршрут" : "Клиенты");
        }).attach();

        setupBackButton(btnBack, false);
        return view;
    }

    // ВАЖНО: Этот метод вызывается при клике на клиента
    public void onClientSelected(String storeName) {
        ReturnDetailsFragment fragment = new ReturnDetailsFragment();
        Bundle args = new Bundle();
        args.putString("store_name", storeName);
        fragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}