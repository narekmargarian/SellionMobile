package com.sellion.mobile.fragments;

import android.content.Context;
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
import com.sellion.mobile.activities.HostActivity;
import com.sellion.mobile.adapters.OrderHistoryItemsAdapter;
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.OrderEntity;
import com.sellion.mobile.entity.OrderItemInfo;
import com.sellion.mobile.entity.ProductEntity;
import com.sellion.mobile.managers.CartManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class OrderDetailsViewFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_details_view, container, false);

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

        final Context appContext = requireContext().getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(appContext);

        db.orderDao().getAllOrdersLive().observe(getViewLifecycleOwner(), orders -> {
            OrderEntity currentOrder = null;
            if (orders != null) {
                for (OrderEntity o : orders) {
                    if (orderId != -1) {
                        if (o.id == orderId) { currentOrder = o; break; }
                    } else if (o.shopName.equals(shopName)) {
                        currentOrder = o; break;
                    }
                }
            }

            if (currentOrder != null) {
                final OrderEntity finalOrder = currentOrder;

                String paymentText = (finalOrder.paymentMethod != null) ? finalOrder.paymentMethod.getTitle() : "Не указано";
                tvPaymentMethod.setText("Оплата: " + paymentText);

                tvInvoiceStatus.setText("Раздельная фактура: " + (finalOrder.needsSeparateInvoice ? "Да" : "Нет"));
                tvDate.setText("Дата доставки: " + (finalOrder.deliveryDate != null ? finalOrder.deliveryDate : "Не указано"));

                calculateTotal(finalOrder, tvTotalSum);

                // --- ПОДГОТОВКА ДАННЫХ ДЛЯ АДАПТЕРА ---
                if (finalOrder.items != null) {
                    new Thread(() -> {
                        try {
                            List<OrderItemInfo> preparedList = new ArrayList<>();
                            for (Map.Entry<Long, Integer> entry : finalOrder.items.entrySet()) {
                                ProductEntity p = db.productDao().getProductById(entry.getKey());
                                if (p != null) {
                                    preparedList.add(new OrderItemInfo(p.name, entry.getValue(), p.price, p.stockQuantity));
                                } else {
                                    preparedList.add(new OrderItemInfo("Удаленный товар ID:" + entry.getKey(), entry.getValue(), 0, 0));
                                }
                            }

                            requireActivity().runOnUiThread(() -> {
                                if (isAdded()) {
                                    // Устанавливаем новый быстрый адаптер
                                    rv.setAdapter(new OrderHistoryItemsAdapter(preparedList));
                                }
                            });
                        } catch (Exception e) {
                            HostActivity.logToFile(appContext, "PREPARE_DATA_ERR", e.getMessage());
                        }
                    }).start();
                }

                if ("SENT".equals(finalOrder.status)) {
                    btnEdit.setVisibility(View.GONE);
                } else {
                    btnEdit.setVisibility(View.VISIBLE);
                    btnEdit.setOnClickListener(v -> {
                        CartManager.getInstance().clearCart();
                        if (finalOrder.items != null) {
                            new Thread(() -> {
                                for (Map.Entry<Long, Integer> entry : finalOrder.items.entrySet()) {
                                    ProductEntity p = db.productDao().getProductById(entry.getKey());
                                    if (p != null) {
                                        CartManager.getInstance().addProduct(p.id, p.name, entry.getValue(), p.price);
                                    }
                                }

                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        CartManager.getInstance().setDeliveryDate(finalOrder.deliveryDate);
                                        CartManager.getInstance().setPaymentMethod(finalOrder.paymentMethod);
                                        CartManager.getInstance().setSeparateInvoice(finalOrder.needsSeparateInvoice);

                                        OrderDetailsFragment editFrag = new OrderDetailsFragment();
                                        Bundle b = new Bundle();
                                        b.putString("store_name", finalOrder.shopName);
                                        b.putInt("order_id_to_update", finalOrder.id);
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
        return view;
    }

    private void calculateTotal(OrderEntity order, TextView tvTotalSum) {
        if (order == null || tvTotalSum == null) return;
        final Context appContext = requireContext().getApplicationContext();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                double totalSum = 0;
                int totalQty = 0;
                AppDatabase db = AppDatabase.getInstance(appContext);

                if (order.items != null) {
                    for (Map.Entry<Long, Integer> entry : order.items.entrySet()) {
                        double price = db.productDao().getPriceById(entry.getKey());
                        totalSum += (price * entry.getValue());
                        totalQty += entry.getValue();
                    }
                }

                final String result = String.format("Товаров: %d шт. | Итого: %,.0f ֏", totalQty, totalSum);

                if (isAdded() && getActivity() != null) {
                    tvTotalSum.post(() -> tvTotalSum.setText(result));
                }
            } catch (Exception e) {
                HostActivity.logToFile(appContext, "CALC_TOTAL_ERR", e.getMessage());
            }
        });
    }
}
