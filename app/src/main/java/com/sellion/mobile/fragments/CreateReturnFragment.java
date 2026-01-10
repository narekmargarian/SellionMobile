package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.sellion.mobile.R;
import com.sellion.mobile.adapters.CreateReturnPagerAdapter;
import com.sellion.mobile.entity.ReturnModel;
import com.sellion.mobile.managers.ReturnHistoryManager;


public class CreateReturnFragment extends BaseFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_return_details, container, false); // переиспользуем XML

        ViewPager2 viewPager = view.findViewById(R.id.returnViewPager);
        TabLayout tabLayout = view.findViewById(R.id.returnTabLayout);
        ImageButton btnBack = view.findViewById(R.id.btnBackFromReturn);

        // Меняем заголовок
        TextView title = view.findViewById(R.id.tvReturnStoreName);
        if (title != null) title.setText("Выбор клиента (Возврат)");

        // Используем ваш CreateOrderPagerAdapter
        viewPager.setAdapter(new CreateReturnPagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Маршрут" : "Клиенты");
        }).attach();

        setupBackButton(btnBack, false);
        return view;
    }

    // ВАЖНО: Этот метод вызывается при клике на клиента
    public void onClientSelected(String storeName) {

        // 1. Проверка на наличие неотправленного заказа
        boolean hasPendingReturn = false;
        for (ReturnModel returnModel : ReturnHistoryManager.getInstance().getReturns()) {
            if (returnModel.shopName.equals(storeName) && returnModel.status == ReturnModel.Status.PENDING) {
                hasPendingReturn = true;
                break;
            }

        }

        if (hasPendingReturn) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Внимание")
                    .setMessage("Для магазина '" + storeName + "' уже есть активный Возврат. Отредактируйте его в истории или отправьте текущий.")
                    .setPositiveButton("Понятно", null)
                    .show();
        } else {
            // 2. Если заказа нет — открываем детали
            openStoreDetails(storeName);
        }

    }


    private void openStoreDetails(String storeName) {
        ReturnDetailsFragment fragment = new ReturnDetailsFragment();
        Bundle args = new Bundle();
        args.putString("store_name", storeName);
        fragment.setArguments(args);

        // Используем стандартную анимацию и ПРАВИЛЬНУЮ транзакцию
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, fragment)
                .addToBackStack("return_process") // Даем имя шагу, чтобы легче управлять
                .commit();
    }
}