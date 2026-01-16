package com.sellion.mobile.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sellion.mobile.R;
import com.sellion.mobile.api.ApiClient;
import com.sellion.mobile.api.ApiService;
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.ClientEntity;
import com.sellion.mobile.entity.OrderEntity;
import com.sellion.mobile.entity.ProductEntity;
import com.sellion.mobile.entity.ReturnEntity;
import com.sellion.mobile.managers.SessionManager;
import com.sellion.mobile.model.ClientModel;
import com.sellion.mobile.model.Product;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class SyncFragment extends BaseFragment {

    private TextView tvStatus;
    private ImageView ivPreview;
    private Button btnUploadPhoto;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private AlertDialog progressDialog;

    // Константы для хранения состояния загрузки
    private static final String PREFS_NAME = "SyncSettings";
    private static final String KEY_IS_LOADED = "is_data_loaded";

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

        // Проверяем статус при входе: загружены ли данные
        updateStatusText();

        setupBackButton(btnBack, true);
        btnSend.setOnClickListener(v -> sendDocuments());

        // Кнопка загрузки теперь имеет проверку
        btnLoad.setOnClickListener(v -> checkBeforeLoading());

        btnClear.setOnClickListener(v -> clearData());
        btnPhoto.setOnClickListener(v -> takePhotoReport());
        btnVersion.setOnClickListener(v -> Toast.makeText(getContext(), "Версия 2026.01.12", Toast.LENGTH_SHORT).show());

        btnUploadPhoto.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Фото отправлено!", Toast.LENGTH_SHORT).show();
            ivPreview.setVisibility(View.GONE);
            btnUploadPhoto.setVisibility(View.GONE);
        });

        return view;
    }

    private void updateStatusText() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(KEY_IS_LOADED, false)) {
            tvStatus.setText("Данные загружены. Работа в локальном режиме.");
            tvStatus.setTextColor(android.graphics.Color.parseColor("#2E7D32")); // Зеленый
        } else {
            tvStatus.setText("Требуется первичная загрузка документов!");
            tvStatus.setTextColor(android.graphics.Color.RED);
        }
    }

    // Проверка: нужно ли скачивать данные или они уже есть
    private void checkBeforeLoading() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean alreadyLoaded = prefs.getBoolean(KEY_IS_LOADED, false);

        if (alreadyLoaded) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Внимание")
                    .setMessage("Данные уже загружены. Вы хотите ОЧИСТИТЬ текущую базу и загрузить новые данные из офиса?")
                    .setPositiveButton("Обновить всё", (d, w) -> loadDocuments())
                    .setNegativeButton("Отмена", null)
                    .show();
        } else {
            loadDocuments();
        }
    }

    private void loadDocuments() {
        // 1. Показываем диалог загрузки
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_progress, null);
        TextView tvMessage = dialogView.findViewById(R.id.tvProgressMessage);
        tvMessage.setText("Загрузка данных из офиса...");

        AlertDialog progressDialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();
        progressDialog.show();

        // 2. Начинаем загрузку в фоновом потоке
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(requireContext().getApplicationContext());
                ApiService api = ApiClient.getClient().create(ApiService.class);

                // Получаем ID текущего менеджера (например, 1011)
                String currentManagerId = SessionManager.getInstance().getManagerId();
                if (currentManagerId == null || currentManagerId.isEmpty()) {
                    throw new Exception("ID менеджера не найден. Перезайдите в приложение.");
                }

                // Очищаем старые данные перед загрузкой новых (справочники и документы)
                db.clearAllTables();

                // ШАГ 1: Загружаем товары
                Response<List<Product>> productResponse = api.getProducts().execute();
                if (productResponse.isSuccessful() && productResponse.body() != null) {
                    List<ProductEntity> productEntities = new ArrayList<>();
                    for (Product p : productResponse.body()) {
                        productEntities.add(new ProductEntity(
                                p.getName(), p.getPrice(), p.getItemsPerBox(),
                                p.getBarcode(), p.getCategory(), p.getStockQuantity()));
                    }
                    db.productDao().insertAll(productEntities);
                }

                // ШАГ 2: Загружаем клиентов (магазины)
                Response<List<ClientModel>> clientResponse = api.getClients().execute();
                if (clientResponse.isSuccessful() && clientResponse.body() != null) {
                    List<ClientEntity> clientEntities = new ArrayList<>();
                    for (ClientModel m : clientResponse.body()) {
                        ClientEntity e = new ClientEntity();
                        e.id = m.id;
                        e.name = m.name;
                        e.debt = m.debt;
                        e.address = m.getAddress();
                        e.inn = m.inn;
                        e.ownerName = m.ownerName;
                        e.routeDay = m.routeDay;
                        clientEntities.add(e);
                    }
                    db.clientDao().insertAll(clientEntities);
                }

                // ШАГ 3: Загружаем ЗАКАЗЫ этого менеджера (НОВОЕ)
                Response<List<OrderEntity>> ordersResponse = api.getOrdersByManager(currentManagerId).execute();
                if (ordersResponse.isSuccessful() && ordersResponse.body() != null) {
                    for (OrderEntity order : ordersResponse.body()) {
                        // Важно: ставим статус SENT, чтобы телефон не считал их новыми
                        order.status = "SENT";
                        db.orderDao().insert(order);
                    }
                }

                // ШАГ 4: Загружаем ВОЗВРАТЫ этого менеджера (НОВОЕ)
                Response<List<ReturnEntity>> returnsResponse = api.getReturnsByManager(currentManagerId).execute();
                if (returnsResponse.isSuccessful() && returnsResponse.body() != null) {
                    for (ReturnEntity ret : returnsResponse.body()) {
                        // Ставим статус SENT
                        ret.status = "SENT";
                        db.returnDao().insert(ret);
                    }
                }

                // ФИНАЛ: Сохраняем состояние успеха
                SharedPreferences.Editor editor = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
                editor.putBoolean(KEY_IS_LOADED, true);
                editor.apply();

                requireActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    updateStatusText();
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Синхронизация завершена")
                            .setMessage("Все данные, включая ваши заказы и возвраты, загружены.")
                            .setPositiveButton("Понятно", null)
                            .show();
                });

            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    showSyncError("Ошибка: " + e.getMessage());
                    Log.e("SYNC_ERROR", "Детали: ", e);
                });
            }
        }).start();
    }


    private void showSyncError(String message) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Ошибка загрузки")
                .setMessage(message)
                .setPositiveButton("ОК", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    private void dismissProgress() {
        if (getActivity() != null) getActivity().runOnUiThread(() -> progressDialog.dismiss());
    }

    private void clearData() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Очистка")
                .setMessage("Это удалит ВСЕ данные из телефона. Вы уверены?")
                .setPositiveButton("Да, удалить", (d, w) -> {
                    new Thread(() -> {
                        AppDatabase.getInstance(requireContext()).clearAllTables();
                        requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply();
                        requireActivity().runOnUiThread(() -> {
                            updateStatusText();
                            Toast.makeText(getContext(), "Все данные удалены", Toast.LENGTH_SHORT).show();
                        });
                    }).start();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    // ... методы takePhotoReport и sendDocuments остаются без изменений ...

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


    private void takePhotoReport() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            cameraLauncher.launch(takePictureIntent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Камера недоступна", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendDocuments() {
        // Создаем современный диалог прогресса
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_progress, null);
        AlertDialog progressDialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        progressDialog.show();

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext().getApplicationContext());
            ApiService api = ApiClient.getClient().create(ApiService.class);

            // Получаем данные, которые еще не отправлены
            List<OrderEntity> pendingOrders = db.orderDao().getPendingOrdersSync();
            List<ReturnEntity> pendingReturns = db.returnDao().getPendingReturnsSync();

            if (pendingOrders.isEmpty() && pendingReturns.isEmpty()) {
                requireActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Нет новых данных для отправки", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            try {
                boolean allOk = true;

                // 1. Отправка Заказов
                if (!pendingOrders.isEmpty()) {
                    Response<okhttp3.ResponseBody> response = api.sendOrders(pendingOrders).execute();
                    if (response.isSuccessful()) {
                        db.orderDao().markAllAsSent();
                    } else {
                        allOk = false;
                    }
                }

                // 2. Отправка Возвратов
                if (!pendingReturns.isEmpty()) {
                    Response<okhttp3.ResponseBody> response = api.sendReturns(pendingReturns).execute();
                    if (response.isSuccessful()) {
                        db.returnDao().markAllAsSent();
                    } else {
                        allOk = false;
                    }
                }

                boolean finalStatus = allOk;
                requireActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    if (finalStatus) {
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Успешно")
                                .setMessage("Данные успешно синхронизированы с офисом.")
                                .setPositiveButton("ОК", null)
                                .show();
                    } else {
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Ошибка сервера")
                                .setMessage("Сервер ответил ошибкой. Попробуйте позже.")
                                .setPositiveButton("Понятно", null)
                                .show();
                    }
                });

            } catch (java.io.IOException e) {
                // ОШИБКА: Сервер выключен или нет интернета
                requireActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Связь отсутствует")
                            .setMessage("Не удалось подключиться к серверу.\n\nПроверьте:\n1. Включен ли сервер в офисе\n2. Работает ли интернет на телефоне")
                            .setPositiveButton("Попробовать позже", null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                });
            }
        }).start();
    }
}