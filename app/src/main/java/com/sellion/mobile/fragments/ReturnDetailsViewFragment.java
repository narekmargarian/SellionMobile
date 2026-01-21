package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;
import com.sellion.mobile.adapters.OrderHistoryItemsAdapter;
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.ProductEntity;
import com.sellion.mobile.entity.ReturnEntity;
import com.sellion.mobile.managers.CartManager;
import com.sellion.mobile.managers.ReturnManager;

import java.util.Map;


public class ReturnDetailsViewFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_return_details_view, container, false);

        int returnId = getArguments() != null ? getArguments().getInt("return_id") : -1;
        String shopNameFromArgs = getArguments() != null ? getArguments().getString("order_shop_name") : "";

        TextView tvTitle = view.findViewById(R.id.RtvViewReturnTitle);
        TextView tvReason = view.findViewById(R.id.tvViewReturnMethod);
        TextView tvDate = view.findViewById(R.id.tvViewReturnDate);
        TextView tvTotalSum = view.findViewById(R.id.RtvViewReturnTotalSum);
        RecyclerView rv = view.findViewById(R.id.RrvViewOrderItems);
        Button btnEdit = view.findViewById(R.id.RbtnEditThisReturn);
        ImageButton btnBack = view.findViewById(R.id.RbtnBackFromView);

        tvTitle.setText(shopNameFromArgs);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        AppDatabase db = AppDatabase.getInstance(requireContext().getApplicationContext());
        db.returnDao().getAllReturnsLive().observe(getViewLifecycleOwner(), returns -> {
            ReturnEntity currentReturn = null;
            if (returns != null) {
                for (ReturnEntity r : returns) {
                    if (r.id == returnId) {
                        currentReturn = r;
                        break;
                    }
                }
            }

            if (currentReturn != null) {
                final ReturnEntity finalReturn = currentReturn;

                // Отображение причины (используем getTitle() из Enum для красоты)
                String reasonStr = (finalReturn.returnReason != null) ? finalReturn.returnReason.getTitle() : "Не указана";
                tvReason.setText("Причина: " + reasonStr);
                tvDate.setText("Дата возврата: " + (finalReturn.returnDate != null ? finalReturn.returnDate : "Не указана"));

                calculateTotal(finalReturn, tvTotalSum);

                if (finalReturn.items != null) {
                    // Используем обновленный адаптер, принимающий Map<Long, Integer>
                    rv.setAdapter(new OrderHistoryItemsAdapter(finalReturn.items));
                }

                if ("SENT".equals(finalReturn.status)) {
                    btnEdit.setVisibility(View.GONE);
                } else {
                    btnEdit.setVisibility(View.VISIBLE);
                    btnEdit.setOnClickListener(v -> {
                        CartManager.getInstance().clearCart();

                        new Thread(() -> {
                            if (finalReturn.items != null) {
                                // ИСПРАВЛЕНО: Ключ теперь Long (ID товара)
                                for (Map.Entry<Long, Integer> entry : finalReturn.items.entrySet()) {
                                    // Ищем актуальные данные товара по ID
                                    ProductEntity p = db.productDao().getProductById(entry.getKey());
                                    if (p != null) {
                                        CartManager.getInstance().addProduct(p.id, p.name, entry.getValue(), p.price);
                                    }
                                }
                            }

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    CartManager.getInstance().setReturnReason(finalReturn.returnReason);
                                    CartManager.getInstance().setReturnDate(finalReturn.returnDate);

                                    ReturnDetailsFragment fragment = new ReturnDetailsFragment();
                                    Bundle args = new Bundle();
                                    args.putString("store_name", finalReturn.shopName);
                                    args.putInt("return_id_to_update", finalReturn.id);
                                    fragment.setArguments(args);

                                    getParentFragmentManager().beginTransaction()
                                            .replace(R.id.fragment_container, fragment)
                                            .addToBackStack(null)
                                            .commit();
                                });
                            }
                        }).start();
                    });
                }
            }
        });

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        setupBackButton(btnBack, false);

        return view;
    }

    private void calculateTotal(ReturnEntity ret, TextView tvTotalSum) {
        if (ret == null || tvTotalSum == null) return;

        // Если сумма уже есть в Entity, выводим сразу
        if (ret.totalAmount > 0) {
            tvTotalSum.setText(String.format("Итого: %,.0f ֏", ret.totalAmount));
            return;
        }

        new Thread(() -> {
            double total = 0;
            int qty = 0;
            AppDatabase db = AppDatabase.getInstance(requireContext().getApplicationContext());

            if (ret.items != null) {
                for (Map.Entry<Long, Integer> entry : ret.items.entrySet()) {
                    int itemQty = entry.getValue();
                    // ИСПРАВЛЕНО: Поиск цены по ID
                    double price = db.productDao().getPriceById(entry.getKey());
                    total += (itemQty * price);
                    qty += itemQty;
                }
            }

            final double finalTotal = total;
            final int finalQty = qty;

            if (isAdded()) {
                tvTotalSum.post(() ->
                        tvTotalSum.setText(String.format("Товаров: %d шт. | Итого: %,.0f ֏", finalQty, finalTotal))
                );
            }
        }).start();
    }
}
