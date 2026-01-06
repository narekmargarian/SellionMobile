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
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.sellion.mobile.R;
import com.sellion.mobile.adapters.CreateOrderPagerAdapter;
import com.sellion.mobile.entity.OrderModel;
import com.sellion.mobile.managers.OrderHistoryManager;


public class CreateOrderFragment extends BaseFragment {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_store_details, container, false);

        viewPager = view.findViewById(R.id.storeViewPager);
        tabLayout = view.findViewById(R.id.storeTabLayout);
        ImageButton btnBack = view.findViewById(R.id.btnBackToRoute);

        setupBackButton(btnBack, false);

        CreateOrderPagerAdapter adapter = new CreateOrderPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Маршрут" : "Клиенты");
        }).attach();

        return view;
    }

    // Центральный метод обработки выбора клиента
    public void onClientSelected(String storeName) {
        // 1. Проверка на наличие неотправленного заказа
        boolean hasPendingOrder = false;
        for (OrderModel order : OrderHistoryManager.getInstance().getSavedOrders()) {
            if (order.shopName.equals(storeName) && order.status == OrderModel.Status.PENDING) {
                hasPendingOrder = true;
                break;
            }
        }

        if (hasPendingOrder) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Внимание")
                    .setMessage("Для магазина '" + storeName + "' уже есть активный заказ. Отредактируйте его в истории или отправьте текущий.")
                    .setPositiveButton("Понятно", null)
                    .show();
        } else {
            // 2. Если заказа нет — открываем детали
            openStoreDetails(storeName);
        }
    }

    private void openStoreDetails(String storeName) {
        StoreDetailsFragment fragment = new StoreDetailsFragment();
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