package com.sellion.mobile.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import com.sellion.mobile.entity.OrderModel;
import com.sellion.mobile.entity.ReturnModel;
import com.sellion.mobile.managers.OrderHistoryManager;
import com.sellion.mobile.managers.ReturnHistoryManager;

import java.util.List;

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
        showLoadingDialog("Синхронизация данных...");

        // 1. Обработка Заказов - меняем статус на SENT
        List<OrderModel> allOrders = OrderHistoryManager.getInstance().getOrders();
        for (OrderModel order : allOrders) {
            if (order.status == OrderModel.Status.PENDING) {
                order.status = OrderModel.Status.SENT;
            }
        }

        // 2. Обработка Возвратов - меняем статус на SENT
        List<ReturnModel> allReturns = ReturnHistoryManager.getInstance().getReturns();
        for (ReturnModel returnItem : allReturns) {
            if (returnItem.status == ReturnModel.Status.PENDING) {
                returnItem.status = ReturnModel.Status.SENT;
            }
        }

        tvStatus.setText("Синхронизация завершена успешно.");
        tvStatus.setTextColor(Color.parseColor("#2E7D32"));
        Toast.makeText(getContext(), "Все документы переданы в офис!", Toast.LENGTH_SHORT).show();
    }

    private void loadDocuments() {
        showLoadingDialog("Загрузка данных из офиса...");
        // Имитация загрузки
    }

    private void clearData() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Очистка")
                .setMessage("Удалить все локальные заказы и возвраты?")
                .setPositiveButton("Да", (d, w) -> {
                    OrderHistoryManager.getInstance().getOrders().clear();
                    ReturnHistoryManager.getInstance().getReturns().clear();
                    tvStatus.setText("Все данные удалены");
                    tvStatus.setTextColor(Color.RED);
                    Toast.makeText(getContext(), "База очищена", Toast.LENGTH_SHORT).show();
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