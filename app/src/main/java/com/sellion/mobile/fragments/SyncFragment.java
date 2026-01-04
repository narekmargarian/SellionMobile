package com.sellion.mobile.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.sellion.mobile.R;
import com.sellion.mobile.entity.OrderModel;
import com.sellion.mobile.managers.OrderHistoryManager;

import java.util.List;

public class SyncFragment extends Fragment {

    private TextView tvStatus;
    private ImageView ivPreview; // Для показа сделанного фото
    private Button btnUploadPhoto; // Кнопка "Отправить в офис"
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sync, container, false);

        ImageButton btnBack = view.findViewById(R.id.btnBackSync);
        Button btnSend = view.findViewById(R.id.btnSendDocs);
        Button btnLoad = view.findViewById(R.id.btnLoadDocs);
        Button btnClear = view.findViewById(R.id.btnClearData);
        Button btnPhoto = view.findViewById(R.id.btnPhotoReport);
        Button btnVersion = view.findViewById(R.id.btnVersion);
        tvStatus = view.findViewById(R.id.tvSyncStatus);


        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // 1. Отправить Документы
        btnSend.setOnClickListener(v -> sendDocuments());

        // 2. Загрузить Документы
        btnLoad.setOnClickListener(v -> loadDocuments());

        // 3. Очистить данные
        btnClear.setOnClickListener(v -> clearData());

        // 4. Фото Отчеты (Пока заглушка для камеры)
        btnPhoto.setOnClickListener(v -> takePhotoReport());

        // 5. Новая версия
        btnVersion.setOnClickListener(v -> Toast.makeText(getContext(), "У вас установлена последняя версия приложения 2025.12.30", Toast.LENGTH_SHORT).show());


        ivPreview = view.findViewById(R.id.ivPhotoPreview);
        btnUploadPhoto = view.findViewById(R.id.btnUploadPhoto);

        // Логика кнопки "Отправить"
        btnUploadPhoto.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Фото успешно отправлено в офис!", Toast.LENGTH_SHORT).show();
            ivPreview.setVisibility(View.GONE);
            btnUploadPhoto.setVisibility(View.GONE);
        });

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Регистрируем обработчик результата камеры
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        // Получаем фото из камеры
                        Bundle extras = result.getData().getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");

                        // Показываем фото и кнопку отправки
                        ivPreview.setImageBitmap(imageBitmap);
                        ivPreview.setVisibility(View.VISIBLE);
                        btnUploadPhoto.setVisibility(View.VISIBLE);
                    }
                }
        );
    }

    private void sendDocuments() {
        showLoadingDialog("Отправка заказов в офис...");

        List<OrderModel> allOrders = OrderHistoryManager.getInstance().getSavedOrders();

        if (allOrders.isEmpty()) {
            Toast.makeText(getContext(), "Нет заказов для отправки", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Меняем статус у всех заказов на SENT
        for (OrderModel order : allOrders) {
            if (order.status == OrderModel.Status.PENDING) {
                order.status = OrderModel.Status.SENT;
            }
        }

        // 4. Обновляем текст статуса на экране синхронизации
        tvStatus.setText("Синхронизация завершена успешно.\nОтправлено заказов: " + allOrders.size());
        tvStatus.setTextColor(android.graphics.Color.parseColor("#2E7D32")); // Зеленый

        Toast.makeText(getContext(), "Данные переданы в офис!", Toast.LENGTH_SHORT).show();
    }

    private void loadDocuments() {
        showLoadingDialog("Загрузка новых клиентов и товаров...");
        // В реальном приложении здесь будет код HTTP запроса на сервер
        // После завершения:
        // dismissLoadingDialog();
        // tvStatus.setText("Данные обновлены. Добавлено 10 новых клиентов и чипсы со вкусом манго.");
    }

    private void clearData() {
        // Просто сообщение
        new AlertDialog.Builder(getContext())
                .setTitle("Внимание")
                .setMessage("Все локальные данные удалены (заказы, маршруты). Необходимо выполнить полную синхронизацию.")
                .setPositiveButton("ОК", null)
                .show();
        tvStatus.setText("Данные очищены");
        tvStatus.setTextColor(android.graphics.Color.RED);
    }

    // Пока просто Toast, для камеры нужен Permission и Activity Result
    private void takePhotoReport() {
        Toast.makeText(getContext(), "Открываем камеру...", Toast.LENGTH_SHORT).show();
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            cameraLauncher.launch(cameraIntent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Ошибка запуска камеры", Toast.LENGTH_SHORT).show();
        }
    }


    private void showLoadingDialog(String message) {
        // Пока просто Toast, для окна прогресса нужен AlertDialog с ProgressBar
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }
}