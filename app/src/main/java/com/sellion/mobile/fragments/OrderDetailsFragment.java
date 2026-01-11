package com.sellion.mobile.fragments;

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
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.OrderEntity;
import com.sellion.mobile.handler.BackPressHandler;
import com.sellion.mobile.helper.NavigationHelper;
import com.sellion.mobile.managers.CartManager;

public class OrderDetailsFragment extends BaseFragment implements BackPressHandler {
    private TextView tvStoreName;
    private ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Используем тот же XML, что и в заказе
        View view = inflater.inflate(R.layout.fragment_order_details, container, false);

        TabLayout tabLayout = view.findViewById(R.id.storeTabLayout);
        viewPager = view.findViewById(R.id.orderDetailsViewPager);
        ImageButton btnBack = view.findViewById(R.id.btnBackToRoute);
        View btnSave = view.findViewById(R.id.btnSaveFullOrder);
        tvStoreName = view.findViewById(R.id.tvStoreName);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // В 2026 году здесь ПУСТО.
                // LiveData внутри CurrentOrderFragment сама обновит экран.
            }
        });
        // Устанавливаем специальный адаптер для возврата
        OrderDetailsFragment.OrderPagerAdapter adapter = new OrderDetailsFragment.OrderPagerAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);


        // Кнопка ОФОРМИТЬ ВОЗВРАТ
        btnSave.setOnClickListener(v -> {
            if (CartManager.getInstance().getCartItems().isEmpty()) {
                Toast.makeText(getContext(), "Список пуст! Добавьте товары.", Toast.LENGTH_SHORT).show();
            } else {
                saveOrderToDatabase();
            }
        });

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

        setupBackButton(btnBack, false);
        return view;
    }


    private void saveOrderToDatabase() {
        String storeName = tvStoreName.getText().toString();

        // 1. Собираем данные из интерфейса и CartManager
        OrderEntity newOrder = new OrderEntity();
        newOrder.shopName = storeName;
        newOrder.items = new java.util.HashMap<>(CartManager.getInstance().getCartItems());
        newOrder.deliveryDate = CartManager.getInstance().getDeliveryDate();
        newOrder.paymentMethod = CartManager.getInstance().getPaymentMethod();
        newOrder.needsSeparateInvoice = CartManager.getInstance().isSeparateInvoice();
        newOrder.status = "PENDING"; // Статус "Ожидает отправки"

        // 2. Получаем экземпляр базы данных
        AppDatabase db = AppDatabase.getInstance(requireContext());

        // 3. Room запрещает операции в главном потоке (чтобы приложение не зависало)
        // Используем новый поток для записи в БД
        new Thread(() -> {
            db.orderDao().insert(newOrder);

            // 4. После записи возвращаемся в главный поток для обновления UI
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    CartManager.getInstance().clearCart();
                    Toast.makeText(getContext(), "Заказ сохранен в базу!", Toast.LENGTH_SHORT).show();

                    // Используем ваш NavigationHelper для перехода
                    NavigationHelper.finishAndGoTo(getParentFragmentManager(), new OrdersFragment());
                });
            }
        }).start();
    }


    @Override
    public void onBackPressedHandled() {
        showSaveReturnDialog();

    }

    // Адаптер вкладок специально для возврата
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
                    return new CatalogFragment();      // Ваш обычный каталог
                case 1:
                    return new CurrentOrderFragment(); // Ваша обычная корзина
                case 2:
                    return new OrderInfoFragment();  // НОВЫЙ фрагмент с причинами (Enum)
                default:
                    return new CatalogFragment();
            }
        }
    }


    // Логика диалога сохранения (как в заказе)
    protected void showSaveReturnDialog() {
        if (!CartManager.getInstance().getCartItems().isEmpty()) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Завершение Заказа")
                    .setMessage("Сохранить заказ перед выходом?")
                    .setPositiveButton("Да, сохранить", (dialog, which) -> saveOrderToDatabase())
                    .setNegativeButton("Нет", (dialog, which) -> {
                        CartManager.getInstance().clearCart();
                        // Возвращаемся к выбору клиента
                        getParentFragmentManager().popBackStack();
                    })
                    .setNeutralButton("Отмена", null)
                    .show();
        } else {
            CartManager.getInstance().clearCart();
            getParentFragmentManager().popBackStack();
        }
    }


}