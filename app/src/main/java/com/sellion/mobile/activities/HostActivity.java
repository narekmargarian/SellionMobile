package com.sellion.mobile.activities;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.sellion.mobile.R;
import com.sellion.mobile.fragments.DashboardFragment;
import com.sellion.mobile.handler.BackPressHandler;
import com.sellion.mobile.managers.CartManager;
import com.sellion.mobile.managers.SessionManager;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
        logToFile(this, "SYSTEM", "App Started for manager: " + SessionManager.getInstance().getManagerId());


        // 1. Инициализируем корзину (используем appContext для защиты от утечек)
        CartManager.init(getApplicationContext());

        // 2. Настраиваем сессию с проверкой на null (защита при восстановлении процесса)
        String managerId = getIntent().getStringExtra("MANAGER_ID");

        if (managerId != null) {
            SessionManager.getInstance().setManagerId(managerId);
        } else {
            // Если Activity пересоздана, пробуем достать ID из SessionManager (он там в SharedPreferences)
            managerId = SessionManager.getInstance().getManagerId();
        }

        if (savedInstanceState == null) {
            // Если это первый запуск, а не восстановление после поворота
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment())
                    .commit();
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        // Если фрагмент хочет сам обработать нажатие "Назад" (например, закрыть меню или диалог)
        if (fragment instanceof BackPressHandler) {
            ((BackPressHandler) fragment).onBackPressedHandled();
        } else {
            // Иначе стандартное поведение
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                super.onBackPressed();
            }
        }

    }

    public static void logToFile(Context context, String tag, String message) {
        try {
            // Файл будет лежать в: /Android/data/имя.пакета/files/logs.txt
            File logFile = new File(context.getExternalFilesDir(null), "logs.txt");
            FileWriter writer = new FileWriter(logFile, true);
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            writer.append(timeStamp).append(" [").append(tag).append("] : ").append(message).append("\n");
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
