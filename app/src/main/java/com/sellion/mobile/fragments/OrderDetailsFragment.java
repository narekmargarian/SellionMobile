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
        setupBackButton(btnBack, false);

        return view;
    }

    private void saveOrderToDatabase() {
        // 1. Получаем данные
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

                // 2. Проверка остатков
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

                // 3. Проверка акций на сервере
                ApiService api = ApiClient.getClient(appContext).create(ApiService.class);
                // Используем полный путь к модели в списке, если есть конфликт
                Response<List<com.sellion.mobile.model.PromoAction>> promoResponse = api.checkActiveForItems(productIds).execute();

                if (promoResponse.isSuccessful() && promoResponse.body() != null && !promoResponse.body().isEmpty()) {
                    final List<com.sellion.mobile.model.PromoAction> activePromos = promoResponse.body();

                    requireActivity().runOnUiThread(() -> {
                        if (isAdded()) {
                            PromoSelectionDialog dialog = PromoSelectionDialog.newInstance(activePromos, new PromoSelectionDialog.OnPromoSelectedListener() {

                                @Override
                                public void onConfirmed(com.sellion.mobile.model.PromoAction selectedPromo) {
                                    // ИСПОЛЬЗУЕМ ПОЛНЫЙ ПУТЬ К КЛАССУ В АРГУМЕНТЕ
                                    CartManager.getInstance().setPromo(selectedPromo.getId(), selectedPromo.getItems());
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
                    // Если акций нет
                    CartManager.getInstance().setPromo(null, null);
                    proceedToFinalSave(appContext, cartItems, storeName, orderIdToUpdate);
                }

            } catch (Exception e) {
                HostActivity.logToFile(appContext, "OrderDetails_ERR", e.getMessage());
                // В случае ошибки сети — сохраняем с дефолтным процентом магазина (извлекаем cartItems повторно)
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


    private void proceedToFinalSave(Context appContext, List<CartEntity> cartItems, String storeName, int orderIdToUpdate) {
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(appContext);

                // 1. Извлекаем настройки из CartManager
                BigDecimal shopPercent = CartManager.getInstance().getClientDefaultPercent();
                Map<Long, BigDecimal> promoItems = CartManager.getInstance().getAppliedPromoItems();

                double totalAmount = 0;
                Map<Long, Integer> itemsMap = new HashMap<>();
                Map<Long, BigDecimal> appliedPromos = new HashMap<>();

                // 2. Расчет каждой позиции с учетом иерархии скидок
                for (CartEntity item : cartItems) {
                    itemsMap.put(item.productId, item.quantity);

                    // Определяем процент: Акция имеет приоритет над процентом магазина
                    BigDecimal finalPercent = shopPercent; // По умолчанию
                    if (promoItems.containsKey(item.productId)) {
                        finalPercent = promoItems.get(item.productId);
                    }

                    // Сохраняем, какой процент применился к конкретному товару
                    appliedPromos.put(item.productId, finalPercent);

                    // Вычисляем цену со скидкой
                    double discountFactor = 1.0 - (finalPercent.doubleValue() / 100.0);
                    double discountedPrice = item.price * discountFactor;

                    totalAmount += (discountedPrice * item.quantity);
                }

                // 3. Формируем сущность заказа
                OrderEntity order = new OrderEntity();

                // Используем переданный ID, если мы в режиме редактирования
                if (orderIdToUpdate != -1) {
                    order.id = orderIdToUpdate;
                }

                order.shopName = storeName;
                order.items = itemsMap;
                order.appliedPromoItems = appliedPromos; // Сохраняем детализацию скидок в БД Room
                order.totalAmount = totalAmount;
                order.status = "PENDING";
                order.managerId = com.sellion.mobile.managers.SessionManager.getInstance().getManagerId();
                order.deliveryDate = CartManager.getInstance().getDeliveryDate();
                order.paymentMethod = CartManager.getInstance().getPaymentMethod();
                order.needsSeparateInvoice = CartManager.getInstance().isSeparateInvoice();
                order.createdAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(new Date());

                // Генерация уникального ID для защиты от дубликатов на сервере
                String deviceId = android.provider.Settings.Secure.getString(appContext.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
                order.androidId = deviceId + "_" + System.currentTimeMillis();

                // 4. Запись в локальную базу данных
                db.orderDao().insert(order);
                HostActivity.logToFile(appContext, "SAVE_ORDER", "Заказ сохранен: " + storeName + " Сумма: " + totalAmount);

                // 5. Обновление UI
                if (getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        CartManager.getInstance().clearCart();
                        Toast.makeText(appContext, "Заказ сохранен со скидками!", Toast.LENGTH_SHORT).show();

                        // Переход в список заказов
                        if (isAdded()) {
                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, new OrdersFragment())
                                    .commit();
                        }
                    });
                }

            } catch (Exception e) {
                HostActivity.logToFile(appContext, "SAVE_ORDER_ERR", e.getMessage());
                if (getActivity() != null) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(appContext, "Ошибка сохранения: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
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