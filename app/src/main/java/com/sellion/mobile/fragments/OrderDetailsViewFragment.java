package com.sellion.mobile.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sellion.mobile.R;
import com.sellion.mobile.adapters.CartAdapter;
import com.sellion.mobile.entity.CartManager;
import com.sellion.mobile.entity.OrderModel;
import com.sellion.mobile.entity.Product;
import com.sellion.mobile.managers.OrderHistoryManager;

import java.util.ArrayList;
import java.util.List;

public class OrderDetailsViewFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_details_view, container, false);

        String shopName = getArguments() != null ? getArguments().getString("order_shop_name") : "";

        TextView tvTitle = view.findViewById(R.id.tvViewOrderTitle);
        tvTitle.setText(shopName);

        RecyclerView rv = view.findViewById(R.id.rvViewOrderItems);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        // Поиск заказа в истории
        OrderModel currentOrder = null;
        for (OrderModel o : OrderHistoryManager.getInstance().getSavedOrders()) {
            if (o.shopName.equals(shopName)) {
                currentOrder = o;
                break;
            }
        }

        if (currentOrder != null) {
            // Список товаров для отображения
            List<Product> products = new ArrayList<>();
            for (String name : currentOrder.items.keySet()) {
                products.add(new Product(name, ""));
            }

            // Используем CartAdapter, чтобы видеть названия и количество
            CartAdapter adapter = new CartAdapter(products, product -> {
                // Здесь при клике можно тоже открывать BottomSheet для правки, если нужно
            });
            rv.setAdapter(adapter);

            // Кнопка ИЗМЕНИТЬ
            OrderModel finalOrder = currentOrder;
            Button btnEdit = view.findViewById(R.id.btnEditThisOrder);
            btnEdit.setOnClickListener(v -> {
                if (finalOrder.status == OrderModel.Status.PENDING) {
                    // Загружаем данные обратно в корзину
                    CartManager.getInstance().clearCart();
                    CartManager.getInstance().getCartItems().putAll(finalOrder.items);

                    // Переход в режим сбора заказа
                    StoreDetailsFragment storeFrag = new StoreDetailsFragment();
                    Bundle b = new Bundle();
                    b.putString("store_name", shopName);
                    storeFrag.setArguments(b);

                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, storeFrag)
                            .addToBackStack(null)
                            .commit();
                } else {
                    Toast.makeText(getContext(), "Заказ уже отправлен в офис!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        view.findViewById(R.id.btnBackFromView).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        return view;
    }
}