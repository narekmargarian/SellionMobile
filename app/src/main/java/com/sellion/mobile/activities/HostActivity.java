package com.sellion.mobile.activities;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.sellion.mobile.R;
import com.sellion.mobile.fragments.DashboardFragment;
import com.sellion.mobile.handler.BackPressHandler;
import com.sellion.mobile.managers.CartManager;
import com.sellion.mobile.managers.SessionManager;

public class HostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        // Инициализация корзины для работы с Room
        CartManager.init(getApplicationContext());

        String managerId = getIntent().getStringExtra("MANAGER_ID");
        SessionManager.getInstance().setManagerId(managerId);

        // --- НОВЫЙ СПОСОБ ОБРАБОТКИ КНОПКИ НАЗАД (AndroidX) ---
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

                // Проверяем, реализует ли фрагмент наш интерфейс BackPressHandler
                if (fragment instanceof BackPressHandler) {
                    ((BackPressHandler) fragment).onBackPressedHandled();
                } else {
                    // Если в стеке есть фрагменты — возвращаемся назад
                    if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                        getSupportFragmentManager().popBackStack();
                    } else {
                        // Если стека нет — закрываем Activity (выход из приложения)
                        finish();
                    }
                }
            }
        });
        // -------------------------------------------------------

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment())
                    .commit();
        }
    }
}