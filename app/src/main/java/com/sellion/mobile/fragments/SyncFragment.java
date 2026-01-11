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

import com.sellion.mobile.R;

public class SyncFragment extends BaseFragment {

    private TextView tvStatus;
    private ImageView ivPreview;
    private Button btnUploadPhoto;
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
        ivPreview = view.findViewById(R.id.ivPhotoPreview);
        btnUploadPhoto = view.findViewById(R.id.btnUploadPhoto);

        setupBackButton(btnBack, false);

        btnSend.setOnClickListener(v -> sendDocuments());
        btnLoad.setOnClickListener(v -> loadDocuments());
        btnClear.setOnClickListener(v -> clearData());
        btnPhoto.setOnClickListener(v -> takePhotoReport());
        btnVersion.setOnClickListener(v -> Toast.makeText(getContext(), "Версия 2026.01.10", Toast.LENGTH_SHORT).show());

        btnUploadPhoto.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Фото отправлено!", Toast.LENGTH_SHORT).show();
            ivPreview.setVisibility(View.GONE);
            btnUploadPhoto.setVisibility(View.GONE);
        });

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        ivPreview.setImageBitmap(imageBitmap);
                        ivPreview.setVisibility(View.VISIBLE);
                        btnUploadPhoto.setVisibility(View.VISIBLE);
                    }
                }
        );
    }

    private void sendDocuments() {
        // Создаем запрос на синхронизацию (используем наш SyncWorker)
        androidx.work.OneTimeWorkRequest syncRequest =
                new androidx.work.OneTimeWorkRequest.Builder(com.sellion.mobile.sync.SyncWorker.class).build();

        // Ставим задачу в очередь
        androidx.work.WorkManager.getInstance(requireContext()).enqueue(syncRequest);

        // Слушаем статус выполнения через LiveData
        androidx.work.WorkManager.getInstance(requireContext())
                .getWorkInfoByIdLiveData(syncRequest.getId())
                .observe(getViewLifecycleOwner(), workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        tvStatus.setText("Синхронизация завершена. Данные на сервере!");
                        tvStatus.setTextColor(android.graphics.Color.parseColor("#2E7D32"));
                    }
                });
    }

    private void loadDocuments() {
        showLoadingDialog("Загрузка данных из офиса...");
        // Имитация загрузки
    }

    private void clearData() {
        // 1. Сначала показываем диалог подтверждения
        new AlertDialog.Builder(requireContext())
                .setTitle("Очистка")
                .setMessage("Удалить все локальные заказы и возвраты из базы данных?")
                .setPositiveButton("Да", (d, w) -> {

                    // 2. Запускаем очистку Room в отдельном потоке
                    new Thread(() -> {
                        com.sellion.mobile.database.AppDatabase db =
                                com.sellion.mobile.database.AppDatabase.getInstance(requireContext());

                        // Очищаем таблицы заказов и возвратов
                        db.orderDao().deleteAll();
                        db.returnDao().deleteAll(); // Убедитесь, что метод deleteAll есть в ReturnDao

                        // 3. Возвращаемся в главный поток, чтобы обновить UI
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                tvStatus.setText("База данных Room очищена");
                                tvStatus.setTextColor(android.graphics.Color.RED);
                                android.widget.Toast.makeText(getContext(), "Все данные удалены", android.widget.Toast.LENGTH_SHORT).show();
                            });
                        }
                    }).start();

                })
                .setNegativeButton("Нет", null)
                .show();
    }

    private void takePhotoReport() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(cameraIntent);
    }

    private void showLoadingDialog(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}