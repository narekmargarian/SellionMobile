package com.sellion.mobile.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.sellion.mobile.R;
import com.sellion.mobile.fragments.DashboardFragment;

public class HostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        // 1. Получаем ID менеджера из Intent
        String managerId = getIntent().getStringExtra("MANAGER_ID");

        // 2. Если это первый запуск, загружаем DashboardFragment
        if (savedInstanceState == null) {
            DashboardFragment fragment = new DashboardFragment();

            // Передаем ID менеджера во фрагмент через Bundle
            Bundle args = new Bundle();
            args.putString("MANAGER_ID", managerId);
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}