package com.sellion.mobile.fragments;

import static androidx.fragment.app.FragmentManager.TAG;

import android.content.Context;
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
import com.sellion.mobile.activities.HostActivity;
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.CartEntity;
import com.sellion.mobile.entity.OrderEntity;
import com.sellion.mobile.handler.BackPressHandler;
import com.sellion.mobile.helper.NavigationHelper;
import com.sellion.mobile.managers.CartManager;
import com.sellion.mobile.managers.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class OrderDetailsFragment extends BaseFragment implements BackPressHandler {
    private TextView tvStoreName;
    private ViewPager2 viewPager;


    String currentDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", new Locale("ru"))
            .format(new Date());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_details, container, false);

        TabLayout tabLayout = view.findViewById(R.id.storeTabLayout);
        viewPager = view.findViewById(R.id.orderDetailsViewPager);
        ImageButton btnBack = view.findViewById(R.id.btnBackToRoute);
        View btnSave = view.findViewById(R.id.btnSaveFullOrder);
        tvStoreName = view.findViewById(R.id.tvStoreName);

        if (getArguments() != null && getArguments().containsKey("store_name")) {
            String name = getArguments().getString("store_name");
            tvStoreName.setText(name);
        } else {
            tvStoreName.setText("Неизвестный клиент");
        }

        viewPager.setAdapter(new OrderPagerAdapter(this));
        viewPager.setOffscreenPageLimit(2);

        btnSave.setOnClickListener(v -> saveOrderToDatabase());

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
        btnBack.setOnClickListener(v -> onBackPressedHandled());
        setupBackButton(btnBack, false);

        return view;
    }

    private void saveOrderToDatabase() {
        // 1. Получаем ID для режима редактирования
        final int orderIdToUpdate = getArguments() != null ? getArguments().getInt("order_id_to_update", -1) : -1;

        // 2. Берем контекст до запуска потока (защита от NPE)
        final Context appContext = requireContext().getApplicationContext();

        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(appContext);
                List<CartEntity> cartItems = db.cartDao().getCartItemsSync();

                if (cartItems == null || cartItems.isEmpty()) {
                    if (getActivity() != null) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(appContext, "Корзина пуста!", Toast.LENGTH_SHORT).show());
                    }
                    return;
                }

                // 3. ПРОВЕРКА ОСТАТКОВ ПО ID
                StringBuilder outOfStockItems = new StringBuilder();
                for (CartEntity item : cartItems) {
                    int currentStock = db.productDao().getStockById(item.productId);
                    if (item.quantity > currentStock) {
                        outOfStockItems.append("• ").append(item.productName)
                                .append(": в заказе ").append(item.quantity)
                                .append(", на складе ").append(currentStock).append("\n");
                    }
                }

                if (outOfStockItems.length() > 0) {
                    if (getActivity() != null) {
                        requireActivity().runOnUiThread(() -> {
                            new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireActivity())
                                    .setTitle("Недостаточно товара")
                                    .setMessage("Следующие позиции превышают остаток на складе:\n\n" + outOfStockItems.toString()
                                            + "\nПожалуйста, исправьте количество во вкладке 'Заказ'.")
                                    .setPositiveButton("Понятно", null)
                                    .show();
                        });
                    }
                    return;
                }

                // 4. ФОРМИРОВАНИЕ ЗАКАЗА
                OrderEntity order = new OrderEntity();

                // Если редактируем — сохраняем старый ID для замены в БД
                if (orderIdToUpdate != -1) {
                    order.id = orderIdToUpdate;
                }

                order.shopName = tvStoreName.getText().toString();
                order.status = "PENDING";
                order.managerId = com.sellion.mobile.managers.SessionManager.getInstance().getManagerId();
                order.deliveryDate = CartManager.getInstance().getDeliveryDate();
                order.paymentMethod = CartManager.getInstance().getPaymentMethod();
                order.needsSeparateInvoice = CartManager.getInstance().isSeparateInvoice();
                order.createdAt = currentDateTime;

                // ИДЕАЛЬНО: Привязываем уникальный ID устройства к заказу
                String deviceId = android.provider.Settings.Secure.getString(appContext.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
                order.androidId = deviceId + "_" + System.currentTimeMillis();

                double total = 0;
                Map<Long, Integer> map = new HashMap<>();
                for (CartEntity item : cartItems) {
                    map.put(item.productId, item.quantity); // Используем Long ID
                    total += (item.price * item.quantity);
                }
                order.items = map;
                order.totalAmount = total;

                // 5. ЗАПИСЬ В БД (Room автоматически сделает UPDATE, если id совпадет)
                db.orderDao().insert(order);
                HostActivity.logToFile(appContext, TAG, "Заказ сохранен для: " + order.shopName);

                // 6. ЗАВЕРШЕНИЕ
                if (getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        CartManager.getInstance().clearCart();
                        Toast.makeText(appContext, "Заказ сохранен локально", Toast.LENGTH_SHORT).show();
                        NavigationHelper.finishAndGoTo(getParentFragmentManager(), new OrdersFragment());
                    });
                }
            } catch (Exception e) {
                HostActivity.logToFile(appContext, TAG + "_ERR", e.getMessage());
            }
        }).start();
    }


    @Override
    public void onBackPressedHandled() {
        showSaveReturnDialog();
    }

    protected void showSaveReturnDialog() {
        final Context appContext = requireContext().getApplicationContext();
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(appContext);
                boolean isEmpty = db.cartDao().getCartItemsSync().isEmpty();

                requireActivity().runOnUiThread(() -> {
                    if (!isEmpty) {
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Завершение")
                                .setMessage("Сохранить изменения перед выходом?")
                                .setPositiveButton("Да", (d, w) -> saveOrderToDatabase())
                                .setNegativeButton("Нет", (dialog, which) -> {
                                    CartManager.getInstance().clearCart();
                                    if (isAdded()) {
                                        getParentFragmentManager().popBackStack();
                                    }
                                })
                                .setNeutralButton("Отмена", null)
                                .show();
                    } else {
                        CartManager.getInstance().clearCart();
                        if (isAdded()) {
                            getParentFragmentManager().popBackStack();
                        }
                    }
                });
            } catch (Exception e) {
                HostActivity.logToFile(appContext, TAG + "_BACK_ERR", e.getMessage());
            }
        }).start();
    }

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
                    return new CatalogFragment();
                case 1:
                    return new CurrentOrderFragment();
                case 2:
                    return new OrderInfoFragment();
                default:
                    return new CatalogFragment();
            }
        }
    }
}