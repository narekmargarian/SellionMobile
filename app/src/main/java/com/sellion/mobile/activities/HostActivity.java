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

        CartManager.init(getApplicationContext());
        String managerId = getIntent().getStringExtra("MANAGER_ID");
        SessionManager.getInstance().setManagerId(managerId);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment())
                    .commit();
        }
    }
    // Системная кнопка "Назад" теперь управляется фрагментами автоматически!
}