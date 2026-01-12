package com.sellion.mobile.helper;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.sellion.mobile.R;
import com.sellion.mobile.fragments.DashboardFragment;

public class NavigationHelper {

    public static void openSection(FragmentManager fragmentManager, Fragment targetFragment) {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, targetFragment)
                .addToBackStack(null)
                .commit();
    }

    public static void finishAndGoTo(FragmentManager fragmentManager, Fragment targetFragment) {
        // Очищаем стек ПЕРЕД транзакцией
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, targetFragment)
                .commitAllowingStateLoss();
    }

    public static void backToDashboard(FragmentManager fragmentManager) {
        if (fragmentManager.getBackStackEntryCount() > 0) {
            // Очищаем стек до самого первого фрагмента (Dashboard)
            // Система САМА покажет Dashboard, который уже там лежит
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else {
            // Если стек уже пуст, используем безопасную транзакцию
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment())
                    .commitAllowingStateLoss();
        }
    }
}