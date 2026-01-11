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

        // Наблюдаем за списком заказов. LiveData сама найдет нужный при изменении в БД.
        db.orderDao().getAllOrdersLive().observe(getViewLifecycleOwner(), orders -> {
            OrderEntity currentOrder = null;

            // Ищем конкретный заказ по ID или по имени (если ID не передан)
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

                // 1. Установка деталей
                tvPaymentMethod.setText("Оплата: " + finalOrder.paymentMethod);
                tvInvoiceStatus.setText("Раздельная фактура: " + (finalOrder.needsSeparateInvoice ? "Да" : "Нет"));
                tvDate.setText("Дата доставки: " + finalOrder.deliveryDate);

                // 2. Расчет итога
                calculateTotal(finalOrder, tvTotalSum);

                // 3. Адаптер товаров
                if (finalOrder.items != null) {
                    rv.setAdapter(new OrderHistoryItemsAdapter(finalOrder.items));
                }

                // 4. Логика блокировки редактирования
                if ("SENT".equals(finalOrder.status)) {
                    btnEdit.setVisibility(View.GONE); // Скрываем кнопку, если отправлено на сервер
                } else {
                    btnEdit.setVisibility(View.VISIBLE);
                    btnEdit.setOnClickListener(v -> {
                        // Загружаем данные обратно в CartManager для редактирования
                        CartManager.getInstance().clearCart();
                        if (finalOrder.items != null) {
                            CartManager.getInstance().getCartItems().putAll(finalOrder.items);
                        }
                        CartManager.getInstance().setDeliveryDate(finalOrder.deliveryDate);
                        CartManager.getInstance().setPaymentMethod(finalOrder.paymentMethod);
                        CartManager.getInstance().setSeparateInvoice(finalOrder.needsSeparateInvoice);

                        // Переходим в экран оформления
                        OrderDetailsFragment editFrag = new OrderDetailsFragment();
                        Bundle b = new Bundle();
                        b.putString("store_name", finalOrder.shopName);
                        editFrag.setArguments(b);

                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, editFrag)
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

    private void calculateTotal(OrderEntity order, TextView tvTotalSum) {
        double totalSum = 0;
        int totalQty = 0;
        if (order.items != null) {
            for (Map.Entry<String, Integer> entry : order.items.entrySet()) {
                int qty = entry.getValue();
                totalSum += (getPriceForProduct(entry.getKey()) * qty);
                totalQty += qty;
            }
        }
        if (tvTotalSum != null) {
            tvTotalSum.setText(String.format("Товаров: %d шт. | Итого: %,.0f ֏", totalQty, totalSum));
        }
    }

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