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
import com.sellion.mobile.managers.OrderHistoryManager;

import java.util.HashMap;
import java.util.Map;


public class StoreDetailsFragment extends Fragment {
    private TextView tvStoreName;
    private ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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

        // Гарантирует актуальность корзины при переключении вкладок в 2026 году
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

        btnBack.setOnClickListener(v -> showSaveOrderDialog());

        return view;
    }

    private void saveOrderToDatabase() {
        String storeName = tvStoreName.getText().toString();
        Map<String, Integer> currentItems = new HashMap<>(CartManager.getInstance().getCartItems());

        String payment = "Наличные";
        boolean needsInvoice = false;

        // Поиск фрагмента инфо для получения данных об оплате
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

        // ИСПРАВЛЕНИЕ: Переход в список заказов вместо Dashboard
        if (getActivity() != null) {
            // Закрываем текущий экран (сбор заказа)
            getParentFragmentManager().popBackStack();

            // Открываем историю заказов
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new OrdersFragment())
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void showSaveOrderDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Завершение заказа")
                .setMessage("Сохранить заказ перед выходом?")
                .setPositiveButton("Да, сохранить", (dialog, which) -> saveOrderToDatabase())
                .setNegativeButton("Нет", (dialog, which) -> {
                    CartManager.getInstance().clearCart();
                    // ИСПРАВЛЕНИЕ: Возвращаемся на экран маршрута/клиентов
                    getParentFragmentManager().popBackStack();
                })
                .setNeutralButton("Отмена", null)
                .show();
    }

    private boolean checkItemsInBasket() {
        return !CartManager.getInstance().getCartItems().isEmpty();
    }
}