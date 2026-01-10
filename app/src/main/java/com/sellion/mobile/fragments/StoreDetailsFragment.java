package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.sellion.mobile.R;
import com.sellion.mobile.entity.CartManager;
import com.sellion.mobile.entity.OrderModel;
import com.sellion.mobile.managers.OrderHistoryManager;

import java.util.HashMap;
import java.util.Map;


public class StoreDetailsFragment extends BaseFragment {
//    private String storeName;
//    private boolean isReturnMode = false;
//    private ViewPager2 viewPager;
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_order_details, container, false);
//
//        if (getArguments() != null) {
//            storeName = getArguments().getString("store_name");
//            isReturnMode = getArguments().getBoolean("is_actually_return", false);
//        }
//
//        TextView tvTitle = view.findViewById(R.id.tvStoreName);
//        TabLayout tabLayout = view.findViewById(R.id.storeTabLayout);
//        viewPager = view.findViewById(R.id.storeViewPager);
//        TextView btnSave = view.findViewById(R.id.btnSaveFullOrder);
//        ImageButton btnBack = view.findViewById(R.id.btnBackToRoute);
//
//        tvTitle.setText(isReturnMode ? storeName + " (Возврат)" : storeName);
//        if (isReturnMode) btnSave.setText("ОФОРМИТЬ");
//
//        // 1. ПЕРЕХВАТ КНОПКИ НАЗАД
//        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
//            @Override
//            public void handleOnBackPressed() {
//                showExitConfirmation();
//            }
//        });
//
//        btnBack.setOnClickListener(v -> showExitConfirmation());
//
//        // 2. НАСТРОЙКА АДАПТЕРА (передаем фрагмент целиком для доступа к аргументам)
//        viewPager.setAdapter(new StorePagerAdapter(this, isReturnMode));
//        viewPager.setOffscreenPageLimit(3);
//
//        // 3. ОБНОВЛЕНИЕ ПРИ ПЕРЕХОДЕ НА ВКЛАДКУ ЗАКАЗА
//        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
//            @Override
//            public void onPageSelected(int position) {
//                if (position == 1) {
//                    Fragment f = getChildFragmentManager().findFragmentByTag("f" + position);
//                    if (f instanceof CurrentOrderFragment) ((CurrentOrderFragment) f).updateUI();
//                }
//            }
//        });
//
//        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
//            switch (position) {
//                case 0:
//                    tab.setText("Товары");
//                    break;
//                case 1:
//                    tab.setText(isReturnMode ? "Возврат" : "Заказ");
//                    break;
//                case 2:
//                    tab.setText(isReturnMode ? "Причина" : "О заказе");
//                    break;
//            }
//        }).attach();
//
//        btnSave.setOnClickListener(v -> saveProcess());
//
//        return view;
//    }
//
//    public ViewPager2 getViewPager() {
//        return viewPager;
//    }
//
//    private void showExitConfirmation() {
//        if (!CartManager.getInstance().getCartItems().isEmpty()) {
//            new AlertDialog.Builder(requireContext())
//                    .setTitle("Выход")
//                    .setMessage("Сохранить текущие изменения?")
//                    .setPositiveButton("Да, сохранить", (d, w) -> saveProcess())
//                    .setNegativeButton("Нет, удалить", (d, w) -> {
//                        CartManager.getInstance().clearCart();
//                        getParentFragmentManager().popBackStack();
//                    })
//                    .setNeutralButton("Отмена", null).show();
//        } else {
//            getParentFragmentManager().popBackStack();
//        }
//    }
//
//    private void saveProcess() {
//        // 1. Берем актуальные товары из CartManager
//        Map<String, Integer> items = new HashMap<>(CartManager.getInstance().getCartItems());
//        items.entrySet().removeIf(e -> e.getValue() <= 0);
//
//        if (items.isEmpty()) {
//            Toast.makeText(getContext(), "Список пуст! Добавьте товары.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        String dateStr = "";
//        String payMethod = "Наличные";
//        String retReason = "Просрочка";
//        boolean isInvoice = false;
//
//        // 2. Ищем фрагмент ИНФО (позиция 2)
//        // ВАЖНО: Мы ищем его по ID ViewPager и позиции
//        Fragment infoFrag = getChildFragmentManager().findFragmentByTag("f" + viewPager.getAdapter().getItemId(2));
//
//        // Если по тегу не нашли, ищем среди всех активных фрагментов
//        if (infoFrag == null) {
//            for (Fragment f : getChildFragmentManager().getFragments()) {
//                if (f instanceof OrderInfoFragment) {
//                    infoFrag = f;
//                    break;
//                }
//            }
//        }
//
//        if (infoFrag instanceof OrderInfoFragment) {
//            OrderInfoFragment sInfo = (OrderInfoFragment) infoFrag;
//            dateStr = sInfo.getDeliveryDate();
//            isInvoice = sInfo.isSeparateInvoiceRequired();
//            if (isReturnMode) {
//                retReason = sInfo.getSelectedReason();
//            } else {
//                payMethod = sInfo.getPaymentMethod();
//            }
//        }
//
//        // 3. Сохранение
//
//
//        OrderModel om = new OrderModel(storeName, items, payMethod, dateStr, isInvoice);
//        OrderHistoryManager.getInstance().addOrder(om);
//
//
//        CartManager.getInstance().clearCart();
//        getParentFragmentManager().popBackStack();
//    }
//
//    private static class StorePagerAdapter extends FragmentStateAdapter {
//        private final boolean isRet;
//        private final Fragment parent;
//
//        public StorePagerAdapter(Fragment f, boolean isRet) {
//            super(f);
//            this.isRet = isRet;
//            this.parent = f;
//        }
//
//        @Override
//        public int getItemCount() {
//            return 3;
//        }
//
//        @NonNull
//        @Override
//        public Fragment createFragment(int p) {
//            Bundle b = new Bundle();
//            // ВАЖНО: Пробрасываем аргументы (edit_payment, edit_date и т.д.) во вложенные фрагменты
//            if (parent.getArguments() != null) {
//                b.putAll(parent.getArguments());
//            }
//            b.putBoolean("is_actually_return", isRet);
//
//            if (p == 0) {
//                CatalogFragment f = new CatalogFragment();
//                f.setArguments(b);
//                return f;
//            }
//            if (p == 1) {
//                CurrentOrderFragment f = new CurrentOrderFragment();
//                f.setArguments(b);
//                return f;
//            }
//            OrderInfoFragment f = new OrderInfoFragment();
//            f.setArguments(b);
//            return f;
//        }
//    }
}