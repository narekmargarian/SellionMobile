package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.sellion.mobile.R;
import com.sellion.mobile.adapters.CreateOrderPagerAdapter;
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.OrderEntity;
import com.sellion.mobile.managers.CartManager;
import com.sellion.mobile.model.ClientModel;

public class CreateOrderFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_details, container, false);

        ViewPager2 viewPager = view.findViewById(R.id.orderDetailsViewPager);
        TabLayout tabLayout = view.findViewById(R.id.storeTabLayout);
        ImageButton btnBack = view.findViewById(R.id.btnBackToRoute);

        TextView title = view.findViewById(R.id.tvStoreName);
        if (title != null) title.setText("Выбор клиента (Заказ)");

        viewPager.setAdapter(new CreateOrderPagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Маршрут" : "Клиенты");
        }).attach();

        setupBackButton(btnBack, false);

        return view;
    }

    // ИСПРАВЛЕНО: Теперь принимаем объект ClientModel целиком
    public void onClientSelected(ClientModel client) {
        if (client == null) return;

        final String storeName = client.getName();

        // 1. ОЧИСТКА: Принудительно очищаем корзину, чтобы не было "синих товаров" от старых заказов
        CartManager.getInstance().clearCart();

        // 2. ПРОЦЕНТ: Устанавливаем процент магазина (например, 5%) в менеджер корзины
        if (client.defaultPercent > 0) {
            CartManager.getInstance().setClientDefaultPercent(java.math.BigDecimal.valueOf(client.defaultPercent));
        } else {
            CartManager.getInstance().setClientDefaultPercent(java.math.BigDecimal.ZERO);
        }

        // 3. ПРОВЕРКА ДУБЛИКАТОВ (Твой код в фоновом потоке)
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(requireContext().getApplicationContext());

                // Проверяем все заказы в статусе PENDING
                java.util.List<OrderEntity> pendingOrders = db.orderDao().getOrdersByStatusSync("PENDING");

                boolean hasPendingOrder = false;
                for (OrderEntity order : pendingOrders) {
                    if (order.shopName != null && order.shopName.equals(storeName)) {
                        hasPendingOrder = true;
                        break;
                    }
                }

                final boolean finalHasPending = hasPendingOrder;

                requireActivity().runOnUiThread(() -> {
                    if (isAdded()) {
                        if (finalHasPending) {
                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Внимание")
                                    .setMessage("Для магазина '" + storeName + "' уже есть активный заказ. Отредактируйте его в истории.")
                                    .setPositiveButton("Понятно", null)
                                    .show();
                        } else {
                            // Если всё чисто — открываем сборку заказа
                            openStoreDetails(storeName);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void openStoreDetails(String storeName) {
        OrderDetailsFragment fragment = new OrderDetailsFragment();
        Bundle args = new Bundle();
        args.putString("store_name", storeName);
        fragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    // ... (CreateOrderPagerAdapter остается без изменений)
}
