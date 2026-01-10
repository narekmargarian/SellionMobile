package com.sellion.mobile.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sellion.mobile.R;
import com.sellion.mobile.adapters.OrderHistoryItemsAdapter;
import com.sellion.mobile.entity.CartManager;
import com.sellion.mobile.entity.ReturnModel;
import com.sellion.mobile.helper.NavigationHelper;
import com.sellion.mobile.managers.ReturnHistoryManager;
import com.sellion.mobile.managers.ReturnManager;

import java.util.List;
import java.util.Map;


public class ReturnDetailsViewFragment extends BaseFragment {


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_return_details_view, container, false);
        String shopName = getArguments() != null ? getArguments().getString("order_shop_name") : "";

        TextView tvTitle = view.findViewById(R.id.RtvViewReturnTitle);
        TextView tvReason = view.findViewById(R.id.tvViewReturnMethod);
        TextView tvDate = view.findViewById(R.id.tvViewReturnDate);
        TextView tvTotalSum = view.findViewById(R.id.RtvViewReturnTotalSum);
        RecyclerView rv = view.findViewById(R.id.RrvViewOrderItems);
        Button btnEdit = view.findViewById(R.id.RbtnEditThisReturn);
        ImageButton btnBack = view.findViewById(R.id.RbtnBackFromView);

        tvTitle.setText(shopName);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        // ИСПРАВЛЕНО: Ищем самый свежий возврат (с конца списка)
        ReturnModel currentReturn = null;
        List<ReturnModel> history = ReturnHistoryManager.getInstance().getReturns();
        for (int i = history.size() - 1; i >= 0; i--) {
            if (history.get(i).shopName.equals(shopName)) {
                currentReturn = history.get(i);
                break;
            }
        }

        if (currentReturn != null) {
            final ReturnModel finalReturn = currentReturn;

            tvReason.setText("Причина: " + finalReturn.returnReason);
            tvDate.setText("Дата возврата: " + finalReturn.returnDate);

            // Расчет итога
            calculateTotal(finalReturn, tvTotalSum);
            rv.setAdapter(new OrderHistoryItemsAdapter(finalReturn.items));

            // ЛОГИКА БЛОКИРОВКИ: Если статус SENT (Отправлен), кнопка "Изменить" исчезает
            if (finalReturn.status == ReturnModel.Status.SENT) {
                btnEdit.setVisibility(View.GONE);
            } else {
                btnEdit.setVisibility(View.VISIBLE);
                btnEdit.setOnClickListener(v -> {
                    // Загружаем данные для редактирования
                    CartManager.getInstance().clearCart();
                    CartManager.getInstance().getCartItems().putAll(finalReturn.items);
                    ReturnManager.getInstance().setReturnReason(finalReturn.returnReason);
                    ReturnManager.getInstance().setReturnDate(finalReturn.returnDate);

                    ReturnDetailsFragment storeFrag = new ReturnDetailsFragment();
                    Bundle b = new Bundle();
                    b.putString("store_name", finalReturn.shopName);
                    storeFrag.setArguments(b);

                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, storeFrag)
                            .addToBackStack(null)
                            .commit();
                });
            }
        }

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        setupBackButton(btnBack, false);
        return view;
    }

    private void calculateTotal(ReturnModel model, TextView tvTotal) {
        double total = 0;
        if (model.items != null) {
            for (Map.Entry<String, Integer> entry : model.items.entrySet()) {
                total += (getPriceForProduct(entry.getKey()) * entry.getValue());
            }
        }
        if (tvTotal != null) tvTotal.setText(String.format("Итоговая сумма: %,.0f ֏", total));
    }


    // Идентичный справочник цен
    private int getPriceForProduct(String name) {
        if (name == null) return 0;
        switch (name) {
            case "Шоколад Аленка": return 500;
            case "Шоколад 1": return 5574;
            case "Шоколад 2": return 45452;
            case "Шоколад 3": return 1212;
            case "Конфеты Мишка": return 2500;
            case "Вафли Артек": return 3500;
            case "Вафли 1": return 12560;
            case "Вафли 2": return 12121;
            case "Вафли 3": return 12;
            case "Lays 1": return 785;
            case "Lays 2": return 125;
            case "Lays Сметана/Зелень": return 10001;
            case "Pringles Оригинал": return 789;
            case "Pringles 1": return 123;
            case "Pringles 2": return 566;
            case "Чай 1": return 120;
            case "Чай 2": return 698;
            case "Чай 3": return 900;
            case "Чай Ахмад": return 1100;
            default: return 0;
        }
    }
}