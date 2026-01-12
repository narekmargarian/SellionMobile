package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;
import com.sellion.mobile.adapters.OrderHistoryItemsAdapter;
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.OrderEntity;
import com.sellion.mobile.managers.CartManager;

import java.util.Map;

public class OrderDetailsViewFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_details_view, container, false);

        // Получаем данные из аргументов
        String shopName = getArguments() != null ? getArguments().getString("order_shop_name") : "";
        int orderId = getArguments() != null ? getArguments().getInt("order_id", -1) : -1;

        TextView tvTitle = view.findViewById(R.id.tvViewOrderTitle);
        TextView tvPaymentMethod = view.findViewById(R.id.tvViewOrderPaymentMethod);
        TextView tvInvoiceStatus = view.findViewById(R.id.tvViewOrderInvoiceStatus);
        TextView tvDate = view.findViewById(R.id.tvViewOrderDate);
        TextView tvTotalSum = view.findViewById(R.id.tvViewOrderTotalSum);
        RecyclerView rv = view.findViewById(R.id.rvViewOrderItems);
        Button btnEdit = view.findViewById(R.id.btnEditThisOrder);
        View btnBack = view.findViewById(R.id.btnBackFromView);

        tvTitle.setText(shopName);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        // --- РАБОТА С ROOM И LIVEDATA ---
        AppDatabase db = AppDatabase.getInstance(requireContext());

        db.orderDao().getAllOrdersLive().observe(getViewLifecycleOwner(), orders -> {
            OrderEntity currentOrder = null;

            for (OrderEntity o : orders) {
                if (orderId != -1) {
                    if (o.id == orderId) {
                        currentOrder = o;
                        break;
                    }
                } else if (o.shopName.equals(shopName)) {
                    currentOrder = o;
                    break;
                }
            }

            if (currentOrder != null) {
                final OrderEntity finalOrder = currentOrder;

                tvPaymentMethod.setText("Оплата: " + (finalOrder.paymentMethod != null ? finalOrder.paymentMethod : "Не указано"));
                tvInvoiceStatus.setText("Раздельная фактура: " + (finalOrder.needsSeparateInvoice ? "Да" : "Нет"));
                tvDate.setText("Дата доставки: " + (finalOrder.deliveryDate != null ? finalOrder.deliveryDate : "Не указано"));

                // --- ИСПРАВЛЕННЫЙ ВЫЗОВ РАСЧЕТА ---
                calculateTotal(finalOrder, tvTotalSum);

                if (finalOrder.items != null) {
                    rv.setAdapter(new OrderHistoryItemsAdapter(finalOrder.items));
                }

                if ("SENT".equals(finalOrder.status)) {
                    btnEdit.setVisibility(View.GONE);
                } else {
                    btnEdit.setVisibility(View.VISIBLE);
                    btnEdit.setOnClickListener(v -> {
                        CartManager.getInstance().clearCart();
                        if (finalOrder.items != null) {
                            // Для редактирования нам тоже нужны цены из БД
                            new Thread(() -> {
                                for (Map.Entry<String, Integer> entry : finalOrder.items.entrySet()) {
                                    double price = db.productDao().getPriceByName(entry.getKey());
                                    CartManager.getInstance().addProduct(entry.getKey(), entry.getValue(), price);
                                }

                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        CartManager.getInstance().setDeliveryDate(finalOrder.deliveryDate);
                                        CartManager.getInstance().setPaymentMethod(finalOrder.paymentMethod);
                                        CartManager.getInstance().setSeparateInvoice(finalOrder.needsSeparateInvoice);

                                        OrderDetailsFragment editFrag = new OrderDetailsFragment();
                                        Bundle b = new Bundle();
                                        b.putString("store_name", finalOrder.shopName);
                                        b.putInt("order_id_to_update", finalOrder.id); // ПЕРЕДАЕМ ID
                                        editFrag.setArguments(b);

                                        getParentFragmentManager().beginTransaction()
                                                .replace(R.id.fragment_container, editFrag)
                                                .addToBackStack(null)
                                                .commit();
                                    });
                                }
                            }).start();
                        }
                    });
                }
            }
        });

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        setupBackButton(btnBack, false);

        return view;
    }

    // --- ПОЛНОСТЬЮ ИСПРАВЛЕННЫЙ МЕТОД БЕЗ ХАРДКОДА ЦЕН ---
    private void calculateTotal(OrderEntity order, TextView tvTotalSum) {
        new Thread(() -> {
            double totalSum = 0;
            int totalQty = 0;
            AppDatabase db = AppDatabase.getInstance(requireContext().getApplicationContext());

            if (order.items != null) {
                for (Map.Entry<String, Integer> entry : order.items.entrySet()) {
                    int qty = entry.getValue();
                    // Получаем цену из БД по имени товара
                    double price = db.productDao().getPriceByName(entry.getKey());
                    totalSum += (price * qty);
                    totalQty += qty;
                }
            }

            final double finalSum = totalSum;
            final int finalQty = totalQty;

            if (tvTotalSum != null && getActivity() != null) {
                tvTotalSum.post(() ->
                        tvTotalSum.setText(String.format("Товаров: %d шт. | Итого: %,.0f ֏", finalQty, finalSum))
                );
            }
        }).start();
    }
}
