package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;
import com.sellion.mobile.adapters.OrderHistoryItemsAdapter;
import com.sellion.mobile.entity.CartManager;
import com.sellion.mobile.entity.OrderModel;
import com.sellion.mobile.managers.OrderHistoryManager;

import java.util.Map;

public class OrderDetailsViewFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_details_view, container, false);

        String shopName = getArguments() != null ? getArguments().getString("order_shop_name") : "";

        TextView tvTitle = view.findViewById(R.id.tvViewOrderTitle);
        TextView tvPaymentMethod = view.findViewById(R.id.tvViewOrderPaymentMethod);
        TextView tvInvoiceStatus = view.findViewById(R.id.tvViewOrderInvoiceStatus);
        TextView tvTotalSum = view.findViewById(R.id.tvViewOrderTotalSum);
        RecyclerView rv = view.findViewById(R.id.rvViewOrderItems);

        tvTitle.setText(shopName);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        OrderModel currentOrder = null;
        for (OrderModel o : OrderHistoryManager.getInstance().getSavedOrders()) {
            if (o.shopName.equals(shopName)) {
                currentOrder = o;
                break;
            }
        }

        if (currentOrder != null) {
            // Создаем финальную переменную для использования внутри лямбда-выражения
            final OrderModel finalOrder = currentOrder;

            // 1. Детали заказа
            tvPaymentMethod.setText("Оплата: " + finalOrder.paymentMethod);
            tvInvoiceStatus.setText("Раздельная фактура: " + (finalOrder.needsSeparateInvoice ? "Да" : "Нет"));

            // 2. РАСЧЕТ ИТОГОВОЙ СУММЫ ЗАКАЗА
            double totalOrderSum = 0;
            for (Map.Entry<String, Integer> entry : finalOrder.items.entrySet()) {
                totalOrderSum += (getPriceForProduct(entry.getKey()) * entry.getValue());
            }
            if (tvTotalSum != null) {
                tvTotalSum.setText(String.format("Итоговая сумма: %,.0f ֏", totalOrderSum));
            }

            // 3. Адаптер товаров
            OrderHistoryItemsAdapter adapter = new OrderHistoryItemsAdapter(finalOrder.items);
            rv.setAdapter(adapter);

            // Логика кнопки "Изменить"
            Button btnEdit = view.findViewById(R.id.btnEditThisOrder);
            if (finalOrder.status == OrderModel.Status.SENT) {
                btnEdit.setVisibility(View.GONE);
            } else {
                btnEdit.setOnClickListener(v -> {
                    // 1. Загружаем данные обратно в корзину
                    CartManager.getInstance().clearCart();
                    CartManager.getInstance().getCartItems().putAll(finalOrder.items);

                    // 2. Переходим в экран сбора заказа
                    StoreDetailsFragment storeFrag = new StoreDetailsFragment();
                    Bundle b = new Bundle();
                    b.putString("store_name", finalOrder.shopName);
                    storeFrag.setArguments(b);

                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, storeFrag)
                            .addToBackStack(null)
                            .commit();
                });
            }
        }

        view.findViewById(R.id.btnBackFromView).setOnClickListener(v -> getParentFragmentManager().popBackStack());
        return view;
    }

    // Справочник цен
    private int getPriceForProduct(String name) {
        if (name == null) return 0;
        switch (name) {
            case "Шоколад Аленка":
                return 500;
            case "Конфеты Мишка":
                return 2500;
            case "Вафли Артек":
                return 3500;
            case "Lays Сметана/Зелень":
                return 785;
            case "Pringles Оригинал":
                return 789;
            case "Чай Гринфилд":
                return 900;
            case "Чай Ахмад":
                return 1100;
            default:
                return 0;
        }
    }
}