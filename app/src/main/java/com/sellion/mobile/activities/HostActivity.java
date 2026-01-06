package com.sellion.mobile.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.sellion.mobile.R;
import com.sellion.mobile.fragments.DashboardFragment;

public class HostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        String managerId = getIntent().getStringExtra("MANAGER_ID");
        com.sellion.mobile.managers.SessionManager.getInstance().setManagerId(managerId);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment())
                    .commit();
        }
    }
}