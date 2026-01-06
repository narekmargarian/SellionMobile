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
import com.sellion.mobile.entity.OrderModel;
import com.sellion.mobile.handler.BackPressHandler;
import com.sellion.mobile.managers.OrderHistoryManager;

import java.util.HashMap;
import java.util.Map;


public class StoreDetailsFragment extends BaseFragment implements BackPressHandler {
    private TextView tvStoreName;
    private ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {



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



        View view = inflater.inflate(R.layout.fragment_store_details, container, false);

        TabLayout tabLayout = view.findViewById(R.id.storeTabLayout);
        viewPager = view.findViewById(R.id.storeViewPager);
        ImageButton btnBack = view.findViewById(R.id.btnBackToRoute);
        View btnSave = view.findViewById(R.id.btnSaveFullOrder);
        tvStoreName = view.findViewById(R.id.tvStoreName);

        if (getArguments() != null) {
            tvStoreName.setText(getArguments().getString("store_name"));
        }

        StorePagerAdapter adapter = new StorePagerAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);

        btnSave.setOnClickListener(v -> {
            if (!checkItemsInBasket()) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Пустой заказ")
                        .setMessage("Вы не выбрали ни одного товара.")
                        .setPositiveButton("ОК", null)
                        .show();
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
                    tab.setText("О заказе");
                    break;
            }
        }).attach();

        setupBackButton(btnBack, false); // UI кнопка «назад»





        return view;
    }

    private boolean checkItemsInBasket() {
        CartManager.getInstance().getCartItems()
                .entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue() <= 0);
        return !CartManager.getInstance().getCartItems().isEmpty();
    }

    @Override
    public void onBackPressedHandled() {
        // При нажатии кнопки назад телефона или UI-кнопки
        showSaveOrderDialog();
    }

    protected void showSaveOrderDialog() {
        if (checkItemsInBasket()) {
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
        } else {
            CartManager.getInstance().clearCart();
            getParentFragmentManager().popBackStack();
        }
    }

    private void saveOrderToDatabase() {
        String storeName = tvStoreName.getText().toString();
        Map<String, Integer> currentItems = new HashMap<>(CartManager.getInstance().getCartItems());

        if (currentItems.isEmpty()) {
            Toast.makeText(getContext(), "Невозможно сохранить пустой заказ!", Toast.LENGTH_SHORT).show();
            return;
        }

        String payment = "Наличные";
        boolean needsInvoice = false;

        Fragment infoFrag = getChildFragmentManager().findFragmentByTag("f" + viewPager.getId() + ":" + 2);
        if (infoFrag instanceof StoreInfoFragment) {
            StoreInfoFragment details = (StoreInfoFragment) infoFrag;
            payment = details.getSelectedPaymentMethod();
            needsInvoice = details.isSeparateInvoiceRequired();
        }

        OrderModel newOrder = new OrderModel(storeName, currentItems, payment, needsInvoice);
        OrderHistoryManager.getInstance().addOrder(newOrder);

        CartManager.getInstance().clearCart();
        Toast.makeText(getContext(), "Заказ сохранен!", Toast.LENGTH_SHORT).show();

        if (getActivity() != null) {
            getParentFragmentManager().popBackStack();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new OrdersFragment())
                    .addToBackStack(null)
                    .commit();
        }
    }

    public ViewPager2 getViewPager() {
        return viewPager;
    }


}

