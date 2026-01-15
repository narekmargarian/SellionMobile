package com.sellion.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.sellion.mobile.activities.HostActivity;
import com.sellion.mobile.api.ApiClient;
import com.sellion.mobile.api.ApiService;
import com.sellion.mobile.managers.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private String[] managers;

    private TextInputLayout textInputLayout;
    private AutoCompleteTextView etManager;

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

        textInputLayout = findViewById(R.id.textInputLayoutManager);
        etManager = findViewById(R.id.editTextManager);
        loadManagersList();

    }


    private void loadManagersList() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<String>> call = apiService.getManagersList();

        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> managerList = response.body();
                    // Преобразуем List в массив String[]
                    managers = managerList.toArray(new String[0]);
                    Toast.makeText(MainActivity.this, "Список менеджеров загружен.", Toast.LENGTH_SHORT).show();
                    // Активируем UI-слушатели после успешной загрузки
                    setupListeners();
                } else {
                    Toast.makeText(MainActivity.this, "Ошибка загрузки списка менеджеров.", Toast.LENGTH_LONG).show();
                    // Можно использовать статический список на случай ошибки сети как запасной вариант
                    managers = new String[]{"1011", "1012", "1013", "1014", "1015","1016", "1017", "1018"};
                    setupListeners(); // Активируем с запасным списком
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_LONG).show();
                // Используем статический список на случай ошибки сети как запасной вариант
                managers = new String[]{"1011", "1012", "1013", "1014", "1015","1016", "1017", "1018"};
                setupListeners(); // Активируем с запасным списком
            }
        });
    }

    private void setupListeners() {
        // Логика onClickListener, которую вы перенесли из onCreate
        View.OnClickListener showDialog = v -> {
            if (managers == null || managers.length == 0) {
                Toast.makeText(MainActivity.this, "Список менеджеров пуст.", Toast.LENGTH_SHORT).show();
                return;
            }

            new MaterialAlertDialogBuilder(MainActivity.this)
                    .setTitle("Выберите менеджера")
                    .setItems(managers, (dialog, which) -> {
                        String selectedManager = managers[which];
                        etManager.setText(selectedManager);
                        SessionManager.getInstance().setManagerId(selectedManager);
                        Intent intent = new Intent(MainActivity.this, HostActivity.class);
                        intent.putExtra("MANAGER_ID", selectedManager);
                        startActivity(intent);
                        finish();
                    })
                    .show();
        };

        etManager.setOnClickListener(showDialog);
        textInputLayout.setOnClickListener(showDialog);
        textInputLayout.setEndIconOnClickListener(showDialog);
    }
}