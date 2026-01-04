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

        String managerId = getIntent().getStringExtra("MANAGER_ID");

        // СОХРАНЯЕМ В СЕССИЮ, чтобы данные были доступны везде
        com.sellion.mobile.managers.SessionManager.getInstance().setManagerId(managerId);

        if (savedInstanceState == null) {
            // Теперь не обязательно передавать Bundle,
            // так как Dashboard сам возьмет ID из сессии
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment())
                    .commit();
        }
    }
}