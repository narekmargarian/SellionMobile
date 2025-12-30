package com.sellion.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.sellion.mobile.activities.HostActivity;
import com.sellion.mobile.fragments.DashboardFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        final TextInputEditText etManager = findViewById(R.id.editTextManager);
        // Список номеров менеджеров
        final String[] managers = {"1011", "1012", "1013", "1014", "1015"};

        // Слушатель нажатия на поле
        etManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Создаем всплывающее окно (AlertDialog)
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Выберите менеджера");

                // Добавляем список в окно

                builder.setItems(managers, (dialog, which) -> {
                    String selectedManager = managers[which];

                    // Запускаем HostActivity (это законно, это Activity)
                    Intent intent = new Intent(MainActivity.this, HostActivity.class);

                    // Передаем номер менеджера
                    intent.putExtra("MANAGER_ID", selectedManager);

                    startActivity(intent);
                });
                builder.show();


            }

        });


    }
}