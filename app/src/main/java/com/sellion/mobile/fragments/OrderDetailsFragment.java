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
import com.sellion.mobile.managers.CartManager;
import com.sellion.mobile.entity.OrderModel;
import com.sellion.mobile.handler.BackPressHandler;
import com.sellion.mobile.helper.NavigationHelper;
import com.sellion.mobile.managers.OrderHistoryManager;

import java.util.HashMap;
import java.util.Map;

public class OrderDetailsFragment extends BaseFragment implements BackPressHandler {
    private TextView tvStoreName;
    private ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Используем тот же XML, что и в заказе
        View view = inflater.inflate(R.layout.fragment_order_details, container, false);

        TabLayout tabLayout = view.findViewById(R.id.storeTabLayout);
        viewPager = view.findViewById(R.id.orderDetailsViewPager);
        ImageButton btnBack = view.findViewById(R.id.btnBackToRoute);
        View btnSave = view.findViewById(R.id.btnSaveFullOrder);
        tvStoreName = view.findViewById(R.id.tvStoreName);

        if (getArguments() != null) {
            tvStoreName.setText(getArguments().getString("store_name"));
        } else {
            //TODO
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
        OrderDetailsFragment.OrderPagerAdapter adapter = new OrderDetailsFragment.OrderPagerAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);


        // Кнопка ОФОРМИТЬ ВОЗВРАТ
        btnSave.setOnClickListener(v -> {
            if (CartManager.getInstance().getCartItems().isEmpty()) {
                Toast.makeText(getContext(), "Список пуст! Добавьте товары.", Toast.LENGTH_SHORT).show();
            } else {
                saveOrderToDatabase();
            }
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Товары");
                    break;
                case 1:
                    tab.setText("Заказ");
                    break;
                case 2:
                    tab.setText("О Заказе");
                    break;
            }
        }).attach();

        setupBackButton(btnBack, false);
        return view;
    }


    private void saveOrderToDatabase() {
        String storeName = tvStoreName.getText().toString();
        Map<String, Integer> currentItems = new HashMap<>(CartManager.getInstance().getCartItems());

        String dateStr = CartManager.getInstance().getDeliveryDate();
        String payMethod = CartManager.getInstance().getPaymentMethod();
        boolean isInvoice = CartManager.getInstance().isSeparateInvoice();

        OrderModel om = new OrderModel(storeName, currentItems, payMethod, dateStr, isInvoice);
        OrderHistoryManager.getInstance().addOrder(om);

        // ВАЖНО: Сначала очищаем всё
        CartManager.getInstance().clearCart();

        Toast.makeText(getContext(), "Заказ оформлен!", Toast.LENGTH_SHORT).show();

        // ИСПОЛЬЗУЕМ HELPER (как в возвратах), чтобы навигация была одинаковой
        NavigationHelper.finishAndGoTo(getParentFragmentManager(), new OrdersFragment());
    }


    @Override
    public void onBackPressedHandled() {
        showSaveReturnDialog();

    }

    // Адаптер вкладок специально для возврата
    private static class OrderPagerAdapter extends FragmentStateAdapter {
        public OrderPagerAdapter(@NonNull Fragment fragment) {
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
                    return new CatalogFragment();      // Ваш обычный каталог
                case 1:
                    return new CurrentOrderFragment(); // Ваша обычная корзина
                case 2:
                    return new OrderInfoFragment();  // НОВЫЙ фрагмент с причинами (Enum)
                default:
                    return new CatalogFragment();
            }
        }
    }


    // Логика диалога сохранения (как в заказе)
    protected void showSaveReturnDialog() {
        if (!CartManager.getInstance().getCartItems().isEmpty()) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Завершение Заказа")
                    .setMessage("Сохранить заказ перед выходом?")
                    .setPositiveButton("Да, сохранить", (dialog, which) -> saveOrderToDatabase())
                    .setNegativeButton("Нет", (dialog, which) -> {
                        CartManager.getInstance().clearCart();
                        // Возвращаемся к выбору клиента
                        getParentFragmentManager().popBackStack();
                    })
                    .setNeutralButton("Отмена", null)
                    .show();
        } else {
            CartManager.getInstance().clearCart();
            getParentFragmentManager().popBackStack();
        }
    }


}