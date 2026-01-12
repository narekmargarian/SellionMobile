package com.sellion.mobile.helper;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.sellion.mobile.R;

public class NavigationHelper {
    public static void openSection(FragmentManager fragmentManager, Fragment targetFragment) {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, targetFragment)
                .addToBackStack(null) // Чтобы вернуться на Dashboard
                .commit();
    }

    /**
     * Финальный переход после СОХРАНЕНИЯ.
     * Очищает пошаговую историю, чтобы кнопка "Назад" вела на главный экран.
     */
    public static void finishAndGoTo(FragmentManager fragmentManager, Fragment targetFragment) {
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, targetFragment)
                .addToBackStack(null)
                .commitAllowingStateLoss(); // Добавь это для безопасности в 2026 году
    }

    /**
     * Просто выход назад с очисткой стека (например, при нажатии "Отмена")
     */
    public static void backToDashboard(FragmentManager fragmentManager) {
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

}