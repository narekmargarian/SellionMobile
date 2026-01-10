package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.sellion.mobile.R;
import com.sellion.mobile.handler.BackPressHandler;

public abstract class BaseFragment extends Fragment {

    protected void setupBackButton(View btnBack, boolean returnToRoot) {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> handleBack(returnToRoot));
        }
    }

    protected void handleBack(boolean returnToRoot) {
        FragmentManager fm = getParentFragmentManager();

        if (this instanceof BackPressHandler) {
            ((BackPressHandler) this).onBackPressedHandled();
            return;
        }

        if (returnToRoot) {
            fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fm.beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment())
                    .commit();
        } else {
            fm.popBackStack();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Регистрация системной кнопки "Назад" через жизненный цикл View
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        handleBack(false);
                    }
                });
    }
}
