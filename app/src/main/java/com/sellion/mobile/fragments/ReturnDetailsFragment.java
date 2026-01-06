package com.sellion.mobile.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.sellion.mobile.R;
import com.sellion.mobile.entity.CartManager;
import com.sellion.mobile.handler.BackPressHandler;


public class ReturnDetailsFragment extends BaseFragment implements BackPressHandler {

    private TextView tvStoreName;
    private ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Используем тот же XML, что и в заказе
        View view = inflater.inflate(R.layout.fragment_return_details, container, false);

        TabLayout tabLayout = view.findViewById(R.id.returnTabLayout);
        viewPager = view.findViewById(R.id.returnViewPager);
        ImageButton btnBack = view.findViewById(R.id.btnBackFromReturn);
        View btnSave = view.findViewById(R.id.btnSaveReturn); // Кнопка СОХРАНИТЬ
        tvStoreName = view.findViewById(R.id.tvReturnStoreName);

        if (getArguments() != null) {
            tvStoreName.setText(getArguments().getString("store_name"));
        }
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Если перешли на вкладку корзины (позиция 1)
                if (position == 1) {
                    Fragment currentFragment = getChildFragmentManager().findFragmentByTag("f" + viewPager.getId() + ":" + position);
                    if (currentFragment instanceof CurrentOrderFragment) {
                        ((CurrentOrderFragment) currentFragment).updateUI();
                    }
                }
            }
        });

        // Устанавливаем специальный адаптер для возврата
        ReturnPagerAdapter adapter = new ReturnPagerAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);


        // Кнопка ОФОРМИТЬ ВОЗВРАТ
        btnSave.setOnClickListener(v -> {
            if (CartManager.getInstance().getCartItems().isEmpty()) {
                Toast.makeText(getContext(), "Выберите товары для возврата", Toast.LENGTH_SHORT).show();
            } else {
                saveReturnToDatabase();
            }
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Товары"); break;
                case 1: tab.setText("Возврат"); break; // Список выбранного
                case 2: tab.setText("Причина"); break; // О возврате
            }
        }).attach();

        setupBackButton(btnBack, false);
        return view;
    }

    private void saveReturnToDatabase() {
        // Здесь ваша логика сохранения возврата
        Toast.makeText(getContext(), "Возврат для " + tvStoreName.getText() + " сохранен!", Toast.LENGTH_LONG).show();
        CartManager.getInstance().clearCart();
        getParentFragmentManager().popBackStack();
    }

    @Override
    public void onBackPressedHandled() {
        // Логика как в StoreDetailsFragment
        CartManager.getInstance().clearCart();
        getParentFragmentManager().popBackStack();
    }

    // Адаптер вкладок специально для возврата
    private static class ReturnPagerAdapter extends FragmentStateAdapter {
        public ReturnPagerAdapter(@NonNull Fragment fragment) { super(fragment); }
        @Override public int getItemCount() { return 3; }
        @NonNull @Override public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new CatalogFragment();      // Ваш обычный каталог
                case 1: return new CurrentOrderFragment(); // Ваша обычная корзина
                case 2: return new ReturnInfoFragment();  // НОВЫЙ фрагмент с причинами (Enum)
                default: return new CatalogFragment();
            }
        }
    }



}