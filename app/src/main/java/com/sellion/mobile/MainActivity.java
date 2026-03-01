package com.sellion.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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


    private int clickCount = 0;
    private static final String SECRET_CODE = "sellion&rivento&ip";


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

        setupSecretSettings();

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



    private void setupSecretSettings() {
        TextView tvCopyright = findViewById(R.id.tvCopyright);
        tvCopyright.setOnClickListener(v -> {
            // Проверка блокировки
            long lockUntil = getSharedPreferences("SyncSettings", MODE_PRIVATE).getLong("lock_time", 0);
            if (System.currentTimeMillis() < lockUntil) {
                long diffMin = (lockUntil - System.currentTimeMillis()) / 60000;
                Toast.makeText(this, "Доступ заблокирован на " + diffMin + " мин.", Toast.LENGTH_SHORT).show();
                return;
            }

            clickCount++;
            if (clickCount >= 5) {
                clickCount = 0;
                showSecretCodeDialog();
            }
        });
    }

    private void showSecretCodeDialog() {
        EditText input = new EditText(this);
        input.setHint("Введите секретный код");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Системный доступ")
                .setView(input)
                .setPositiveButton("Ввод", (d, w) -> {
                    String code = input.getText().toString();
                    if (SECRET_CODE.equals(code)) {
                        resetFailAttempts();
                        showIpChangeDialog();
                    } else {
                        handleFailAttempt();
                    }
                })
                .show();
    }

    private void handleFailAttempt() {
        SharedPreferences prefs = getSharedPreferences("SyncSettings", MODE_PRIVATE);
        int attempts = prefs.getInt("fail_attempts", 0) + 1;
        prefs.edit().putInt("fail_attempts", attempts).apply();

        if (attempts >= 3) {
            // Блокировка: 30 минут * кол-во попыток (3 попытки = 90 мин и т.д.)
            long lockTime = System.currentTimeMillis() + (30L * 60 * 1000 * (attempts - 2));
            prefs.edit().putLong("lock_time", lockTime).apply();
            Toast.makeText(this, "Слишком много попыток. Блокировка!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Неверный код! Осталось попыток: " + (3 - attempts), Toast.LENGTH_SHORT).show();
        }
    }

    private void resetFailAttempts() {
        getSharedPreferences("SyncSettings", MODE_PRIVATE).edit()
                .putInt("fail_attempts", 0)
                .putLong("lock_time", 0)
                .apply();
    }

    private void showIpChangeDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 0); // Немного увеличим отступы для красоты

        // 1. Поле Домена (раньше был IP)
        final TextInputLayout domainInputLayout = new TextInputLayout(this);
        domainInputLayout.setHint("Домен сервера (напр. sellion.vip)");
        final EditText domainInput = new EditText(this);
        domainInput.setText(getSharedPreferences("SyncSettings", MODE_PRIVATE).getString("server_ip", "sellion.vip"));
        domainInputLayout.addView(domainInput);
        layout.addView(domainInputLayout);

        // 2. Переключатель рабочей недели (Switch)
        final com.google.android.material.switchmaterial.SwitchMaterial daySwitch =
                new com.google.android.material.switchmaterial.SwitchMaterial(this);
        daySwitch.setText("6-дневная рабочая неделя");
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 50, 0, 0);
        daySwitch.setLayoutParams(params);

        // Загружаем текущий режим (false = 5 дней, true = 6 дней)
        boolean isSixDay = getSharedPreferences("SyncSettings", MODE_PRIVATE).getBoolean("is_six_day_work", false);
        daySwitch.setChecked(isSixDay);
        layout.addView(daySwitch);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Системные настройки 2026")
                .setView(layout)
                .setPositiveButton("Сохранить", (d, w) -> {
                    String newDomain = domainInput.getText().toString().trim();
                    boolean selectedSixDay = daySwitch.isChecked();

                    if (!newDomain.isEmpty()) {
                        getSharedPreferences("SyncSettings", MODE_PRIVATE).edit()
                                .putString("server_ip", newDomain) // Сохраняем как домен
                                .putBoolean("is_six_day_work", selectedSixDay)
                                .apply();

                        // Сбрасываем клиент, чтобы он пересоздался с новым URL https://...
                        ApiClient.resetClient();

                        String weekMode = selectedSixDay ? "6 дней" : "5 дней";
                        Toast.makeText(this, "Настройки обновлены: " + weekMode, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (managersCall != null) {
            managersCall.cancel(); // Отменяем сетевой запрос при закрытии
        }
    }
}

