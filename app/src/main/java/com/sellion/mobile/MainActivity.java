package com.sellion.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.sellion.mobile.activities.HostActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        final TextInputEditText etManager = findViewById(R.id.editTextManager);

        final String[] managers = {"1011", "1012", "1013", "1014", "1015"};


        etManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Выберите менеджера");

                builder.setItems(managers, (dialog, which) -> {
                    String selectedManager = managers[which];

                    Intent intent = new Intent(MainActivity.this, HostActivity.class);
                    intent.putExtra("MANAGER_ID", selectedManager);

                    startActivity(intent);
                });
                builder.show();


            }

        });


    }
}