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

    // Центральный метод обработки выбора клиента
    public void onClientSelected(String storeName) {
        // Проверка в фоновом потоке через Room
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());

            // Получаем все заказы для этого магазина из базы
            java.util.List<OrderEntity> orders = db.orderDao().getPendingOrdersSync();

            boolean hasPendingOrder = false;
            for (OrderEntity order : orders) {
                if (order.shopName.equals(storeName) && "PENDING".equals(order.status)) {
                    hasPendingOrder = true;
                    break;
                }
            }

            final boolean finalHasPending = hasPendingOrder;

            // Возвращаемся в главный поток для UI
            requireActivity().runOnUiThread(() -> {
                if (finalHasPending) {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Внимание")
                            .setMessage("Для магазина '" + storeName + "' уже есть активный заказ. Отредактируйте его в истории или отправьте текущий.")
                            .setPositiveButton("Понятно", null)
                            .show();
                } else {
                    openStoreDetails(storeName);
                }
            });
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
}