package com.sellion.mobile;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

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
    // Сохраняем ссылку на запрос, чтобы отменить его при уничтожении Activity
    private Call<List<String>> managersCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        SessionManager.init(this);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(getApplicationContext());



        // 1. ПРИВЯЗКА ГРАДИЕНТА К ЛОГОТИПУ (Как в вебе)
        TextView tvLogo = findViewById(R.id.logoText);
        setupLogoGradient(tvLogo);

        // 2. АНИМАЦИЯ ПОЯВЛЕНИЯ КАРТОЧКИ (Аналог твоего JS wrapper)
        View cardAuth = findViewById(R.id.cardAuth); // Используем ID из твоего XML
        cardAuth.setAlpha(0f);
        cardAuth.setTranslationY(100f);
        cardAuth.animate().alpha(1f).translationY(0f).setDuration(1000).start();




        textInputLayout = findViewById(R.id.textInputLayoutManager);
        etManager = findViewById(R.id.editTextManager);




        // ШАГ 1: Единый источник данных. UI всегда слушает БД.
        db.managerDao().getAllManagersLive().observe(this, managerIds -> {
            if (managerIds != null && !managerIds.isEmpty()) {
                managers = managerIds.toArray(new String[0]);
            } else {
                // ИСПРАВЛЕНО: Берем список из XML, а не из кода
                String defaultData = getString(R.string.default_managers_list);
                managers = defaultData.split(",");
            }
            setupListeners();
        });

        syncManagersFromServer();



    }

    private void syncManagersFromServer() {
        ApiService apiService = ApiClient.getClient(getApplicationContext()).create(ApiService.class);
        managersCall = apiService.getManagersList();

        managersCall.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (isFinishing() || isDestroyed()) return; // Защита от Memory Leak

                if (response.isSuccessful() && response.body() != null) {
                    List<String> serverList = response.body();
                    new Thread(() -> {
                        List<ManagerEntity> entities = new ArrayList<>();
                        for (String id : serverList) {
                            entities.add(new ManagerEntity(id));
                        }
                        // Просто обновляем БД. LiveData в onCreate сама обновит UI.
                        db.managerDao().deleteAll();
                        db.managerDao().insertAll(entities);
                    }).start();
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                if (!call.isCanceled()) {
                    Log.e("API_DEBUG", "Ошибка загрузки менеджеров: " + t.getMessage());
                }
            }
        });
    }

    private void setupLogoGradient(TextView textView) {
        textView.post(() -> {
            // Градиент от белого к твоему --accent (#6366F1)
            Shader shader = new LinearGradient(0, 0, 0, textView.getLineHeight(),
                    new int[]{Color.WHITE, Color.parseColor("#6366F1")},
                    new float[]{0, 1}, Shader.TileMode.CLAMP);
            textView.getPaint().setShader(shader);
            textView.invalidate();
        });
    }

    private void setupListeners() {
        View.OnClickListener showDialog = v -> {
            if (managers == null || managers.length == 0) return;

            new MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.dialog_select_manager_title)) // Из XML
                    .setItems(managers, (dialog, which) -> {
                        String selectedManager = managers[which];
                        etManager.setText(selectedManager);

                        SessionManager.getInstance().setManagerId(selectedManager);

                        // ИСПРАВЛЕНО: Формируем ключ через формат в XML
                        String generatedKey = getString(R.string.api_key_prefix, selectedManager);
                        SessionManager.getInstance().setApiKey(generatedKey);

                        ApiClient.resetClient();

                        // Логируем вход в файл
                        HostActivity.logToFile(getApplicationContext(), "LOGIN", "Manager " + selectedManager + " logged in");

                        Intent intent = new Intent(MainActivity.this, HostActivity.class);
                        intent.putExtra("MANAGER_ID", selectedManager);
                        startActivity(intent);
                        finish();
                    })
                    .show();
        };

        etManager.setOnClickListener(showDialog);
        textInputLayout.setEndIconOnClickListener(showDialog);
    }

    private void showErrorState() {
        textInputLayout.setError("Неверный ID менеджера"); // Красная обводка как в вебе
        textInputLayout.setErrorTextColor(ColorStateList.valueOf(Color.parseColor("#EF4444")));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (managersCall != null) {
            managersCall.cancel(); // Отменяем сетевой запрос при закрытии
        }
    }
}

