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

        AppDatabase db = AppDatabase.getInstance(requireContext());
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

                // Исправлено: защита от null при отображении
                tvReason.setText("Причина: " + (finalReturn.returnReason != null ? finalReturn.returnReason : "Не указана"));
                tvDate.setText("Дата возврата: " + (finalReturn.returnDate != null ? finalReturn.returnDate : "Не указана"));

                calculateTotal(finalReturn, tvTotalSum);

                if (finalReturn.items != null) {
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
                                for (Map.Entry<String, Integer> entry : finalReturn.items.entrySet()) {
                                    double price = db.productDao().getPriceByName(entry.getKey());
                                    CartManager.getInstance().addProduct(entry.getKey(), entry.getValue(), price);
                                }
                            }

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    ReturnManager.getInstance().setReturnReason(finalReturn.returnReason);
                                    ReturnManager.getInstance().setReturnDate(finalReturn.returnDate);

                                    ReturnDetailsFragment fragment = new ReturnDetailsFragment();
                                    Bundle args = new Bundle();
                                    args.putString("store_name", finalReturn.shopName);

                                    // ВАЖНО: передаем ID для обновления, чтобы не было дубликата при сохранении
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
        new Thread(() -> {
            double total = 0;
            int qty = 0;
            AppDatabase db = AppDatabase.getInstance(requireContext().getApplicationContext());

            if (ret.items != null) {
                for (Map.Entry<String, Integer> entry : ret.items.entrySet()) {
                    int itemQty = entry.getValue();
                    double price = db.productDao().getPriceByName(entry.getKey());
                    total += (itemQty * price);
                    qty += itemQty;
                }
            }

            final double finalTotal = total;
            final int finalQty = qty;

            if (tvTotalSum != null) {
                tvTotalSum.post(() ->
                        tvTotalSum.setText(String.format("Товаров: %d шт. | Итого: %,.0f ֏", finalQty, finalTotal))
                );
            }
        }).start();
    }
}