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
import com.sellion.mobile.entity.CartManager;
import com.sellion.mobile.entity.OrderModel;
import com.sellion.mobile.managers.OrderHistoryManager;

import java.util.List;
import java.util.Map;

public class OrderDetailsViewFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_details_view, container, false);
        String shopName = getArguments() != null ? getArguments().getString("order_shop_name") : "";

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

        // --- ИСПРАВЛЕННЫЙ ПОИСК (АКТУАЛЬНО НА 10 ЯНВАРЯ 2026) ---
        // Идем с конца списка, чтобы найти САМЫЙ НОВЫЙ заказ (черновик),
        // даже если есть старые отправленные заказы для этого магазина.
        OrderModel currentOrder = null;
        List<OrderModel> allOrders = OrderHistoryManager.getInstance().getOrders();
        for (int i = allOrders.size() - 1; i >= 0; i--) {
            OrderModel o = allOrders.get(i);
            if (o.shopName.equals(shopName)) {
                currentOrder = o;
                break;
            }
        }

        if (currentOrder != null) {
            final OrderModel finalOrder = currentOrder;

            // 1. Детали заказа
            tvPaymentMethod.setText("Оплата: " + finalOrder.paymentMethod);
            tvInvoiceStatus.setText("Раздельная фактура: " + (finalOrder.needsSeparateInvoice ? "Да" : "Нет"));
            tvDate.setText("Дата доставки: " + finalOrder.deliveryDate);

            // 2. РАСЧЕТ ИТОГОВОЙ СУММЫ И КОЛИЧЕСТВА (Синхронизировано с возвратами)
            calculateTotal(finalOrder, tvTotalSum);

            // 3. Адаптер товаров
            if (finalOrder.items != null) {
                OrderHistoryItemsAdapter adapter = new OrderHistoryItemsAdapter(finalOrder.items);
                rv.setAdapter(adapter);
            }

            // --- ЛОГИКА БЛОКИРОВКИ РЕДАКТИРОВАНИЯ ---
            if (finalOrder.status == OrderModel.Status.SENT) {
                // Если в SyncFragment нажали "Отправить", кнопка исчезает
                btnEdit.setVisibility(View.GONE);
            } else {
                // Если статус PENDING, редактирование доступно
                btnEdit.setVisibility(View.VISIBLE);
                btnEdit.setOnClickListener(v -> {
                    // Загружаем данные обратно в CartManager
                    CartManager.getInstance().clearCart();
                    if (finalOrder.items != null) {
                        CartManager.getInstance().getCartItems().putAll(finalOrder.items);
                    }

                    CartManager.getInstance().setDeliveryDate(finalOrder.deliveryDate);
                    CartManager.getInstance().setPaymentMethod(finalOrder.paymentMethod);
                    CartManager.getInstance().setSeparateInvoice(finalOrder.needsSeparateInvoice);

                    // Переходим в экран оформления
                    OrderDetailsFragment storeFrag = new OrderDetailsFragment();
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

        // Кнопка назад на экране
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Привязка системной кнопки назад на корпусе телефона
        setupBackButton(btnBack, false);

        return view;
    }

    private void calculateTotal(OrderModel finalOrder, TextView tvTotalSum) {
        double totalOrderSum = 0;
        int totalQty = 0;
        if (finalOrder.items != null) {
            for (Map.Entry<String, Integer> entry : finalOrder.items.entrySet()) {
                int qty = entry.getValue();
                totalOrderSum += (getPriceForProduct(entry.getKey()) * qty);
                totalQty += qty;
            }
        }
        if (tvTotalSum != null) {
            // Добавили вывод общего кол-ва штук для контроля
            tvTotalSum.setText(String.format("Товаров: %d шт. | Итого: %,.0f ֏", totalQty, totalOrderSum));
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