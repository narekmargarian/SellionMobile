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

        // Получаем данные из Room через LiveData
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
                final ReturnEntity finalReturn = currentReturn; // Для использования в listener

                tvReason.setText("Причина: " + finalReturn.returnReason);
                tvDate.setText("Дата возврата: " + finalReturn.returnDate);

                // Исправленный расчет суммы через справочник цен
                calculateTotal(finalReturn, tvTotalSum);

                rv.setAdapter(new OrderHistoryItemsAdapter(finalReturn.items));

                // Логика кнопки "Изменить"
                if ("SENT".equals(finalReturn.status)) {
                    btnEdit.setVisibility(View.GONE);
                } else {
                    btnEdit.setVisibility(View.VISIBLE);
                    btnEdit.setOnClickListener(v -> {
                        // 1. Загружаем данные в менеджеры для редактирования
                        CartManager.getInstance().clearCart();
                        if (finalReturn.items != null) {
                            CartManager.getInstance().getCartItems().putAll(finalReturn.items);
                        }
                        ReturnManager.getInstance().setReturnReason(finalReturn.returnReason);
                        ReturnManager.getInstance().setReturnDate(finalReturn.returnDate);

                        // 2. Открываем экран оформления возврата
                        ReturnDetailsFragment fragment = new ReturnDetailsFragment();
                        Bundle args = new Bundle();
                        args.putString("store_name", finalReturn.shopName);
                        fragment.setArguments(args);

                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null)
                                .commit();
                    });
                }
            }
        });

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        setupBackButton(btnBack, false);
        return view;
    }

    private void calculateTotal(ReturnEntity ret, TextView tvTotalSum) {
        double total = 0;
        int qty = 0;
        if (ret.items != null) {
            for (Map.Entry<String, Integer> entry : ret.items.entrySet()) {
                int itemQty = entry.getValue();
                total += (itemQty * getPriceForProduct(entry.getKey()));
                qty += itemQty;
            }
        }
        if (tvTotalSum != null) {
            tvTotalSum.setText(String.format("Товаров: %d шт. | Итого: %,.0f ֏", qty, total));
        }
    }

    // Идентичный справочник цен
    private int getPriceForProduct(String name) {
        if (name == null) return 0;
        switch (name) {
            case "Чипсы кокосовые ВМ Оригинальные":
                return 730;
            case "Чипсы кокосовые ВМ Соленая карамель":
                return 730;
            case "Чипсы кокосовые Costa Cocosta":
                return 430;
            case "Чипсы кокосовые Costa Cocosta Васаби":
                return 430;
            case "Шарики Манго в какао-глазури ВМ":
                return 930;
            case "Шарики Манго в белой глазури ВМ":
                return 930;
            case "Шарики Банано в глазури ВМ":
                return 730;
            case "Шарики Имбирь сладкий в глазури ВМ":
                return 930;
            case "Чай ВМ Лемонграсс и ананас":
                return 1690;
            case "Чай ВМ зеленый с фруктами":
                return 1690;
            case "Чай ВМ черный Мята и апельсин":
                return 1690;
            case "Чай ВМ черный Черника и манго":
                return 1990;
            case "Чай ВМ черный Шишки и саган-дайля":
                return 1990;
            case "Чай ВМ зеленый Жасмин и манго":
                return 1990;
            case "Чай ВМ черный Цветочное манго":
                return 590;
            case "Чай ВМ черный Шишки и клюква":
                return 790;
            case "Чай ВМ черный Нежная черника":
                return 790;
            case "Чай ВМ черный Ассам Цейлон":
                return 1190;
            case "Чай ВМ черный \"Хвойный\"":
                return 790;
            case "Чай ВМ черный \"Русский березовый\"":
                return 790;
            case "Чай ВМ черный Шишки и малина":
                return 790;
            case "Сух. Манго сушеное Вкусы мира":
                return 1490;
            case "Сух. Манго сушеное ВМ Чили":
                return 1490;
            case "Сух. Папайя сушеная Вкусы мира":
                return 1190;
            case "Сух. Манго шарики из сушеного манго":
                return 1190;
            case "Сух. Манго Сушеное LikeDay (250г)":
                return 2490;
            case "Сух. Манго Сушеное LikeDay (100г)":
                return 1190;
            case "Сух.Бананы вяленые Вкусы мира":
                return 1190;
            case "Сух.Джекфрут сушеный Вкусы мира":
                return 1190;
            case "Сух.Ананас сушеный Вкусы мира":
            default:
                return 0;
        }
    }
}