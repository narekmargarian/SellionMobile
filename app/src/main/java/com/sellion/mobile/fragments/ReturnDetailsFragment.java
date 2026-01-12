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
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.CartEntity;
import com.sellion.mobile.entity.OrderEntity;
import com.sellion.mobile.entity.ReturnEntity;
import com.sellion.mobile.handler.BackPressHandler;
import com.sellion.mobile.helper.NavigationHelper;
import com.sellion.mobile.managers.CartManager;
import com.sellion.mobile.managers.ReturnManager;
import com.sellion.mobile.managers.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class ReturnDetailsFragment extends BaseFragment implements BackPressHandler {

    private TextView tvStoreName;
    private ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Используем тот же макет, что и в заказах
        View view = inflater.inflate(R.layout.fragment_order_details, container, false);

        TabLayout tabLayout = view.findViewById(R.id.storeTabLayout);
        viewPager = view.findViewById(R.id.orderDetailsViewPager);
        ImageButton btnBack = view.findViewById(R.id.btnBackToRoute);
        View btnSave = view.findViewById(R.id.btnSaveFullOrder);
        tvStoreName = view.findViewById(R.id.tvStoreName);

        if (getArguments() != null && getArguments().containsKey("store_name")) {
            tvStoreName.setText(getArguments().getString("store_name"));
        } else {
            tvStoreName.setText("Неизвестный клиент");
        }

        viewPager.setAdapter(new OrderPagerAdapter(this));
        viewPager.setOffscreenPageLimit(2);

        // Кнопка сохранить теперь вызывает сохранение ВОЗВРАТА
        btnSave.setOnClickListener(v -> saveReturnToDatabase());

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Товары"); break;
                case 1: tab.setText("Возврат"); break;
                case 2: tab.setText("Причина"); break;
            }
        }).attach();

        // Обработка кнопки назад через интерфейс
        btnBack.setOnClickListener(v -> onBackPressedHandled());

        return view;
    }

    private void saveReturnToDatabase() {
        final int returnIdToUpdate = getArguments() != null ? getArguments().getInt("return_id_to_update", -1) : -1;

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext().getApplicationContext());
            List<CartEntity> cartItems = db.cartDao().getCartItemsSync();

            if (cartItems == null || cartItems.isEmpty()) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Список возврата пуст!", Toast.LENGTH_SHORT).show());
                return;
            }

            ReturnEntity ret = new ReturnEntity();
            if (returnIdToUpdate != -1) {
                ret.id = returnIdToUpdate;
            }

            ret.shopName = tvStoreName.getText().toString();
            ret.status = "PENDING";

            // 1. Устанавливаем ID менеджера из сессии
            ret.managerId = com.sellion.mobile.managers.SessionManager.getInstance().getManagerId();

            ret.returnDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
            ret.returnReason = CartManager.getInstance().getReturnReason();

            // 2. Считаем итоговую сумму возврата
            Map<String, Integer> map = new HashMap<>();
            double total = 0;
            for (CartEntity item : cartItems) {
                map.put(item.productName, item.quantity);
                total += (item.price * item.quantity); // Суммируем
            }
            ret.items = map;
            ret.totalAmount = total; // Сохраняем сумму в Entity

            db.returnDao().insert(ret);

            requireActivity().runOnUiThread(() -> {
                CartManager.getInstance().clearCart();
                Toast.makeText(getContext(), "Возврат сохранен локально", Toast.LENGTH_SHORT).show();
                NavigationHelper.finishAndGoTo(getParentFragmentManager(), new ReturnsFragment());
            });
        }).start();
    }

    @Override
    public void onBackPressedHandled() {
        showSaveReturnDialog();
    }

    protected void showSaveReturnDialog() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext().getApplicationContext());
            boolean isEmpty = db.cartDao().getCartItemsSync().isEmpty();

            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                if (!isEmpty) {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Завершение")
                            .setMessage("Сохранить возврат перед выходом?")
                            .setPositiveButton("Да", (d, w) -> saveReturnToDatabase())
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
            });
        }).start();
    }

    private static class OrderPagerAdapter extends FragmentStateAdapter {
        public OrderPagerAdapter(@NonNull Fragment fragment) { super(fragment); }
        @Override
        public int getItemCount() { return 3; }
        @NonNull
        @Override
        public Fragment createFragment(int position) {
            // Переиспользуем те же фрагменты, но они поймут режим через аргументы
            switch (position) {
                case 0: return new CatalogFragment();
                case 1: return new CurrentOrderFragment();
                case 2: return new OrderInfoFragment(); // Здесь должна быть причина возврата
                default: return new CatalogFragment();
            }
        }
    }
}