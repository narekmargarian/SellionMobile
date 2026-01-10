package com.sellion.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sellion.mobile.activities.HostActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Принудительно отключаем темную тему, чтобы фон всегда был светлым
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Настройка отступов для EdgeToEdge
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        final TextInputLayout textInputLayout = findViewById(R.id.textInputLayoutManager);
        final TextInputEditText etManager = findViewById(R.id.editTextManager);
        final String[] managers = {"1011", "1012", "1013", "1014", "1015"};

        View.OnClickListener showDialog = v -> {
            // Используем Material-диалог со светлой темой
            new MaterialAlertDialogBuilder(MainActivity.this)
                    .setTitle("Выберите менеджера")
                    .setItems(managers, (dialog, which) -> {
                        String selectedManager = managers[which];
                        etManager.setText(selectedManager);

                        // Переход в HostActivity
                        Intent intent = new Intent(MainActivity.this, HostActivity.class);
                        intent.putExtra("MANAGER_ID", selectedManager);
                        startActivity(intent);
                    })
                    .show();
        };

        // Клик работает и по самому полю, и по иконкам рядом
        etManager.setOnClickListener(showDialog);
        textInputLayout.setOnClickListener(showDialog);
    }
}