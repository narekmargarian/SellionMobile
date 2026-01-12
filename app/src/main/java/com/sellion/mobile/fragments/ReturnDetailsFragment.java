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
import com.sellion.mobile.entity.CartEntity;
import com.sellion.mobile.entity.ReturnEntity;
import com.sellion.mobile.handler.BackPressHandler;
import com.sellion.mobile.helper.NavigationHelper;
import com.sellion.mobile.managers.CartManager;
import com.sellion.mobile.managers.ReturnManager;
import com.sellion.mobile.managers.SessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ReturnDetailsFragment extends BaseFragment implements BackPressHandler {

    private TextView tvStoreName;
    private ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_return_details, container, false);

        TabLayout tabLayout = view.findViewById(R.id.returnTabLayout);
        viewPager = view.findViewById(R.id.returnViewPager);
        ImageButton btnBack = view.findViewById(R.id.btnBackFromReturn);
        View btnSave = view.findViewById(R.id.btnSaveReturn);
        tvStoreName = view.findViewById(R.id.tvReturnStoreName);

        if (getArguments() != null && getArguments().containsKey("store_name")) {
            tvStoreName.setText(getArguments().getString("store_name"));
        }

        viewPager.setAdapter(new ReturnPagerAdapter(this));
        viewPager.setOffscreenPageLimit(2);

        // Кнопка Сохранить
        btnSave.setOnClickListener(v -> saveReturnToDatabase());

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Товары"); break;
                case 1: tab.setText("Возврат"); break;
                case 2: tab.setText("Причина"); break;
            }
        }).attach();

        // Кнопка Назад вызывает логику BackPressHandler
        btnBack.setOnClickListener(v -> handleBack(false));

        return view;
    }

    private void saveReturnToDatabase() {
        // Получаем ID возврата, если мы в режиме редактирования
        final int returnIdToUpdate = getArguments() != null ? getArguments().getInt("return_id_to_update", -1) : -1;

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext().getApplicationContext());
            List<CartEntity> cartItems = db.cartDao().getCartItemsSync();

            if (cartItems == null || cartItems.isEmpty()) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Список возврата пуст!", Toast.LENGTH_SHORT).show());
                }
                return;
            }

            ReturnEntity newReturn = new ReturnEntity();
            // Если редактируем — сохраняем старый ID для замены записи в БД
            if (returnIdToUpdate != -1) {
                newReturn.id = returnIdToUpdate;
            }

            newReturn.shopName = tvStoreName.getText().toString();
            newReturn.managerId = SessionManager.getInstance().getManagerId();
            newReturn.status = "PENDING";

            // СОХРАНЯЕМ ПАРАМЕТРЫ (чтобы не было null в деталях)
            newReturn.returnReason = ReturnManager.getInstance().getReturnReason();
            newReturn.returnDate = ReturnManager.getInstance().getReturnDate();

            Map<String, Integer> itemsMap = new HashMap<>();
            for (CartEntity ci : cartItems) {
                itemsMap.put(ci.productName, ci.quantity);
            }
            newReturn.items = itemsMap;

            // Room обновит запись, если ID совпадет
            db.returnDao().insert(newReturn);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    CartManager.getInstance().clearCart();
                    ReturnManager.getInstance().clear();
                    Toast.makeText(getContext(), "Возврат сохранен!", Toast.LENGTH_SHORT).show();
                    // Переход к списку возвратов с очисткой стека
                    NavigationHelper.finishAndGoTo(getParentFragmentManager(), new ReturnsFragment());
                });
            }
        }).start();
    }

    @Override
    public void onBackPressedHandled() {
        showSaveReturnDialog();
    }

    protected void showSaveReturnDialog() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext().getApplicationContext());
            boolean isEmpty = db.cartDao().getCartItemsSync().isEmpty();

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (!isEmpty) {
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Завершение возврата")
                                .setMessage("Сохранить изменения перед выходом?")
                                .setPositiveButton("Да", (d, w) -> saveReturnToDatabase())
                                .setNegativeButton("Нет", (d, w) -> {
                                    CartManager.getInstance().clearCart();
                                    ReturnManager.getInstance().clear();
                                    // Шаг назад к выбору клиента
                                    if (isAdded()) getParentFragmentManager().popBackStack();
                                })
                                .setNeutralButton("Отмена", null)
                                .show();
                    } else {
                        CartManager.getInstance().clearCart();
                        ReturnManager.getInstance().clear();
                        if (isAdded()) getParentFragmentManager().popBackStack();
                    }
                });
            }
        }).start();
    }

    private static class ReturnPagerAdapter extends FragmentStateAdapter {
        public ReturnPagerAdapter(@NonNull Fragment fragment) { super(fragment); }
        @Override
        public int getItemCount() { return 3; }
        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new CatalogFragment();
                case 1: return new CurrentReturnFragment();
                case 2: return new ReturnInfoFragment();
                default: return new CatalogFragment();
            }
        }
    }
}