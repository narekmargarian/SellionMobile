package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.sellion.mobile.handler.BackPressHandler;
import com.sellion.mobile.helper.NavigationHelper;


public abstract class BaseFragment extends Fragment {
    protected void setupBackButton(View btnBack, boolean returnToDashboard) {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> handleBack(returnToDashboard));
        }
    }

    // Единая точка входа для навигации назад
    protected void handleBack(boolean returnToDashboard) {
        // Защита: если фрагмент не активен, ничего не делаем (избегаем краша контейнера)
        if (!isAdded() || !isResumed() || isRemoving()) return;

        FragmentManager fm = getParentFragmentManager();

        // 1. Проверяем, должен ли фрагмент показать диалог (OrderDetails/ReturnDetails)
        if (this instanceof BackPressHandler) {
            ((BackPressHandler) this).onBackPressedHandled();
            return;
        }

        // 2. Если нужно жестко выйти на Dashboard (например, из списка клиентов)
        if (returnToDashboard) {
            NavigationHelper.backToDashboard(fm);
        } else {
            // 3. Стандартный шаг назад по стеку
            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();
            } else {
                // Если мы уже в корне (на Dashboard) — закрываем Activity (выход из приложения)
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Связываем физическую кнопку телефона с нашей логикой handleBack
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        // Эта строка вызывает handleBack(false)
                        BaseFragment.this.handleBack(false);
                    }
                });
    }
}
