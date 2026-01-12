package com.sellion.mobile.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.sellion.mobile.R;
import com.sellion.mobile.fragments.DashboardFragment;
import com.sellion.mobile.handler.BackPressHandler;
import com.sellion.mobile.managers.CartManager;

public class HostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        // 1. Инициализируем CartManager (обязательно до работы с фрагментами)
        CartManager.init(getApplicationContext());

        // 2. Настраиваем сессию
        String managerId = getIntent().getStringExtra("MANAGER_ID");
        com.sellion.mobile.managers.SessionManager.getInstance().setManagerId(managerId);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment())
                    .commit();
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment instanceof BackPressHandler) {
            ((BackPressHandler) fragment).onBackPressedHandled();
        } else {
            super.onBackPressed();
        }
    }

}