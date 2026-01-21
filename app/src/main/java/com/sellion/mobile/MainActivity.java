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
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.ManagerEntity;
import com.sellion.mobile.managers.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private String[] managers;
    private TextInputLayout textInputLayout;
    private AutoCompleteTextView etManager;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(MainActivity.this);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(this);

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

        // ШАГ 1: Получение данных из локальной базы Room
        db.managerDao().getAllManagersLive().observe(this, managerIds -> {
            if (managerIds != null && !managerIds.isEmpty()) {
                managers = managerIds.toArray(new String[0]);
                setupListeners();
            } else {
                // Дефолтный список для первого запуска
                managers = new String[]{"1011", "1012", "1013", "1014", "1015"};
                setupListeners();
            }
        });

        // ШАГ 2: Обновление списка с сервера
        syncManagersFromServer();
    }

    private void syncManagersFromServer() {
        // ИСПРАВЛЕНО: Передаем 'this' в getClient, так как методу нужен контекст для получения Android ID
        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);

        apiService.getManagersList().enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> serverList = response.body();

                    new Thread(() -> {
                        List<ManagerEntity> entities = new ArrayList<>();
                        for (String id : serverList) {
                            entities.add(new ManagerEntity(id));
                        }
                        db.managerDao().deleteAll();
                        db.managerDao().insertAll(entities);
                    }).start();
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                // Беззвучный режим при отсутствии сети
            }
        });
    }

    private void setupListeners() {
        View.OnClickListener showDialog = v -> {
            if (managers == null || managers.length == 0) {
                Toast.makeText(MainActivity.this, "Загрузка списка...", Toast.LENGTH_SHORT).show();
                return;
            }

            new MaterialAlertDialogBuilder(MainActivity.this)
                    .setTitle("Выберите менеджера")
                    .setItems(managers, (dialog, which) -> {

                        String selectedManager = managers[which];
                        etManager.setText(selectedManager);

                        // 1. Сохраняем ID менеджера
                        SessionManager.getInstance().setManagerId(selectedManager);

                        /// 2. ГЕНЕРИРУЕМ СКРЫТЫЙ КЛЮЧ
                        // Формат: sellion.rivento.mg.ID
                        String generatedKey = "sellion.rivento.mg." + selectedManager;
                        SessionManager.getInstance().setApiKey(generatedKey);

                        ApiClient.resetClient();
                        // Переход
                        Intent intent = new Intent(MainActivity.this, HostActivity.class);
                        intent.putExtra("MANAGER_ID", selectedManager);
                        startActivity(intent);
                        finish();
                    })
                    .show();
        };

        etManager.setOnClickListener(null);
        etManager.setOnClickListener(showDialog);

        textInputLayout.setOnClickListener(null);
        textInputLayout.setOnClickListener(showDialog);

        textInputLayout.setEndIconOnClickListener(null);
        textInputLayout.setEndIconOnClickListener(showDialog);
    }
}
