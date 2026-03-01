package com.sellion.mobile.fragments;

import static androidx.fragment.app.FragmentManager.TAG;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.sellion.mobile.R;
import com.sellion.mobile.activities.HostActivity;
import com.sellion.mobile.api.ApiClient;
import com.sellion.mobile.api.ApiService;
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.CartEntity;
import com.sellion.mobile.entity.OrderEntity;
import com.sellion.mobile.entity.ProductEntity;
import com.sellion.mobile.handler.BackPressHandler;
import com.sellion.mobile.helper.NavigationHelper;
import com.sellion.mobile.managers.CartManager;
import com.sellion.mobile.managers.SessionManager;
import com.sellion.mobile.model.PromoAction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import retrofit2.Response;

public class OrderDetailsFragment extends BaseFragment implements BackPressHandler {
    private TextView tvStoreName;
    private ViewPager2 viewPager;
    private static final String TAG = "OrderDetailsFragment";



    String currentDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", new Locale("ru"))
            .format(new Date());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_details, container, false);

        TabLayout tabLayout = view.findViewById(R.id.storeTabLayout);
        viewPager = view.findViewById(R.id.orderDetailsViewPager);
        ImageButton btnBack = view.findViewById(R.id.btnBackToRoute);
        View btnSave = view.findViewById(R.id.btnSaveFullOrder);
        tvStoreName = view.findViewById(R.id.tvStoreName);

        if (getArguments() != null && getArguments().containsKey("store_name")) {
            String name = getArguments().getString("store_name");
            tvStoreName.setText(name);
        } else {
            tvStoreName.setText("Неизвестный клиент");
        }

        viewPager.setAdapter(new OrderPagerAdapter(this));
        viewPager.setOffscreenPageLimit(2);

        btnSave.setOnClickListener(v -> saveOrderToDatabase());

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Товары");
                    break;
                case 1:
                    tab.setText("Заказ");
                    break;
                case 2:
                    tab.setText("О Заказе");
                    break;
            }
        }).attach();
        btnBack.setOnClickListener(v -> onBackPressedHandled());
//        setupBackButton(btnBack, false);

        return view;
    }

    private void saveOrderToDatabase() {
        final int orderIdToUpdate = getArguments() != null ? getArguments().getInt("order_id_to_update", -1) : -1;
        final Context appContext = requireContext().getApplicationContext();
        final String storeName = tvStoreName.getText().toString();

        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(appContext);
                final List<CartEntity> cartItems = db.cartDao().getCartItemsSync();

                if (cartItems == null || cartItems.isEmpty()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(appContext, "Корзина пуста!", Toast.LENGTH_SHORT).show());
                    return;
                }

                // Проверка остатков на складе (без изменений)
                StringBuilder outOfStockItems = new StringBuilder();
                List<Long> productIds = new ArrayList<>();
                for (CartEntity item : cartItems) {
                    productIds.add(item.productId);
                    int currentStock = db.productDao().getStockById(item.productId);
                    if (item.quantity > currentStock) {
                        outOfStockItems.append("• ").append(item.productName)
                                .append(": ").append(item.quantity).append(" шт. (в наличии ").append(currentStock).append(")\n");
                    }
                }

                if (outOfStockItems.length() > 0) {
                    requireActivity().runOnUiThread(() -> {
                        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireActivity())
                                .setTitle("Недостаточно товара")
                                .setMessage("Исправьте количество:\n\n" + outOfStockItems.toString())
                                .setPositiveButton("ОК", null).show();
                    });
                    return;
                }

                // Запрос активных акций с сервера
                ApiService api = ApiClient.getClient(appContext).create(ApiService.class);
                Response<List<com.sellion.mobile.model.PromoAction>> promoResponse = api.checkActiveForItems(productIds).execute();

                if (promoResponse.isSuccessful() && promoResponse.body() != null && !promoResponse.body().isEmpty()) {
                    final List<com.sellion.mobile.model.PromoAction> activePromos = promoResponse.body();

                    requireActivity().runOnUiThread(() -> {
                        if (isAdded()) {
                            // ИНИЦИАЛИЗАЦИЯ ДИАЛОГА С ОБНОВЛЕННЫМ ИНТЕРФЕЙСОМ (List вместо PromoAction)
                            PromoSelectionDialog dialog = PromoSelectionDialog.newInstance(activePromos, new PromoSelectionDialog.OnPromoSelectedListener() {

                                @Override
                                public void onConfirmed(List<com.sellion.mobile.model.PromoAction> selectedPromos) {
                                    // Защита от вылета при пустом выборе
                                    if (selectedPromos == null || selectedPromos.isEmpty()) {
                                        onSkip();
                                        return;
                                    }

                                    // Объединяем скидки из всех выбранных акций
                                    Map<Long, BigDecimal> combinedPromoItems = new HashMap<>();
                                    for (com.sellion.mobile.model.PromoAction promo : selectedPromos) {
                                        if (promo.getItems() != null) {
                                            combinedPromoItems.putAll(promo.getItems());
                                        }
                                    }

                                    // Фиксируем ID первой акции для отчетности и объединенную карту цен
                                    CartManager.getInstance().setPromo(selectedPromos.get(0).getId(), combinedPromoItems);
                                    proceedToFinalSave(appContext, cartItems, storeName, orderIdToUpdate);
                                }

                                @Override
                                public void onSkip() {
                                    CartManager.getInstance().setPromo(null, null);
                                    proceedToFinalSave(appContext, cartItems, storeName, orderIdToUpdate);
                                }
                            });
                            dialog.show(getChildFragmentManager(), "PROMO_DIALOG");
                        }
                    });
                } else {
                    // Если акций нет, сразу переходим к сохранению
                    CartManager.getInstance().setPromo(null, null);
                    proceedToFinalSave(appContext, cartItems, storeName, orderIdToUpdate);
                }

            } catch (Exception e) {
                HostActivity.logToFile(appContext, "OrderDetails_ERR", e.getMessage());
                try {
                    final List<CartEntity> retryItems = AppDatabase.getInstance(appContext).cartDao().getCartItemsSync();
                    proceedToFinalSave(appContext, retryItems, storeName, orderIdToUpdate);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onBackPressedHandled() {
        showSaveReturnDialog();
    }

    protected void showSaveReturnDialog() {
        final Context appContext = requireContext().getApplicationContext();
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(appContext);
                boolean isEmpty = db.cartDao().getCartItemsSync().isEmpty();

                requireActivity().runOnUiThread(() -> {
                    if (!isEmpty) {
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Завершение")
                                .setMessage("Сохранить изменения перед выходом?")
                                .setPositiveButton("Да", (d, w) -> saveOrderToDatabase())
                                .setNegativeButton("Нет", (dialog, which) -> {
                                    CartManager.getInstance().clearCart();
                                    if (isAdded()) {
                                        getParentFragmentManager().popBackStack();
                                    }
                                })
                                .setNeutralButton("Отмена", null)
                                .show();
                    } else {
                        CartManager.getInstance().clearCart();
                        if (isAdded()) {
                            getParentFragmentManager().popBackStack();
                        }
                    }
                });
            } catch (Exception e) {
                HostActivity.logToFile(appContext, TAG + "_BACK_ERR", e.getMessage());
            }
        }).start();
    }


    private void proceedToFinalSave(Context appContext, List<CartEntity> cartItems,
                                    String storeName, int orderIdToUpdate) {
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(appContext);

                // Получаем базовый процент магазина, сохраненный в CartManager
                BigDecimal shopPercent = CartManager.getInstance().getClientDefaultPercent();
                Map<Long, BigDecimal> promoItems = CartManager.getInstance().getAppliedPromoItems();

                BigDecimal totalAcc = BigDecimal.ZERO;
                Map<Long, Integer> itemsMap = new HashMap<>();
                Map<Long, BigDecimal> appliedPromos = new HashMap<>();

                for (CartEntity item : cartItems) {
                    itemsMap.put(item.productId, item.quantity);

                    // ПРИОРИТЕТ: Если есть акция на товар — берем её, иначе процент магазина
                    BigDecimal finalPercent = promoItems.containsKey(item.productId)
                            ? promoItems.get(item.productId)
                            : shopPercent;

                    appliedPromos.put(item.productId, finalPercent);

                    BigDecimal pricePerUnit = BigDecimal.valueOf(item.price);

                    // Расчет цены со скидкой (аналогично логике бэкенда)
                    BigDecimal discountMultiplier = BigDecimal.valueOf(100)
                            .subtract(finalPercent)
                            .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

                    BigDecimal discountedPrice = pricePerUnit.multiply(discountMultiplier)
                            .setScale(2, RoundingMode.HALF_UP);

                    BigDecimal lineTotal = discountedPrice.multiply(BigDecimal.valueOf(item.quantity));
                    totalAcc = totalAcc.add(lineTotal);
                }

                OrderEntity order = new OrderEntity();
                if (orderIdToUpdate != -1) order.id = orderIdToUpdate;

                order.shopName = storeName;
                order.items = itemsMap;
                order.appliedPromoItems = appliedPromos;

                // КЛЮЧЕВОЕ ИСПРАВЛЕНИЕ: Сохраняем процент магазина в объект заказа,
                // чтобы офис (бэкенд) видел его при открытии или редактировании.
                order.discountPercent = shopPercent.doubleValue();

                order.totalAmount = totalAcc.setScale(2, RoundingMode.HALF_UP).doubleValue();

                order.status = "PENDING";
                order.managerId = com.sellion.mobile.managers.SessionManager.getInstance().getManagerId();
                order.deliveryDate = CartManager.getInstance().getDeliveryDate();
                order.paymentMethod = CartManager.getInstance().getPaymentMethod();
                order.needsSeparateInvoice = CartManager.getInstance().isSeparateInvoice();

                order.createdAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(new Date());

                String deviceId = android.provider.Settings.Secure.getString(appContext.getContentResolver(),
                        android.provider.Settings.Secure.ANDROID_ID);
                order.androidId = deviceId + "_" + System.currentTimeMillis();

                // Сохранение в локальную БД Room
                db.orderDao().insert(order);

                if (getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        if (isAdded()) {
                            CartManager.getInstance().clearCart();
                            Toast.makeText(appContext, "Заказ сохранен!", Toast.LENGTH_SHORT).show();
                            NavigationHelper.finishAndGoTo(getParentFragmentManager(), new OrdersFragment());
                        }
                    });
                }
            } catch (Exception e) {
                HostActivity.logToFile(appContext, "SAVE_ORDER_ERR", e.getMessage());
            }
        }).start();
    }










    private static class OrderPagerAdapter extends FragmentStateAdapter {
        public OrderPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @Override
        public int getItemCount() {
            return 3;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new CatalogFragment();
                case 1:
                    return new CurrentOrderFragment();
                case 2:
                    return new OrderInfoFragment();
                default:
                    return new CatalogFragment();
            }
        }
    }
}