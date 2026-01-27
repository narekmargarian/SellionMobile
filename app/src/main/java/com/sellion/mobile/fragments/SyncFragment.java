package com.sellion.mobile.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sellion.mobile.R;
import com.sellion.mobile.api.ApiClient;
import com.sellion.mobile.api.ApiResponse;
import com.sellion.mobile.api.ApiService;
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.ClientEntity;
import com.sellion.mobile.entity.OrderEntity;
import com.sellion.mobile.entity.ProductEntity;
import com.sellion.mobile.entity.ReturnEntity;
import com.sellion.mobile.managers.SessionManager;
import com.sellion.mobile.model.CategoryGroupDto;
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
    private static final String TAG = "SYNC_LOG";


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
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() == null || !cm.getActiveNetworkInfo().isConnected()) {
            Toast.makeText(getContext(), "Нет интернета!", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_progress, null);
        AlertDialog progressDialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView).setCancelable(false).create();
        progressDialog.show();

        Context appContext = requireContext().getApplicationContext();

        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(appContext);
                ApiService api = ApiClient.getClient(appContext).create(ApiService.class);
                String currentManagerId = SessionManager.getInstance().getManagerId();

                if (currentManagerId == null || currentManagerId.isEmpty()) {
                    throw new Exception("ID менеджера не найден. Перезайдите в систему.");
                }

                // ШАГ 1: Полная очистка
                db.runInTransaction(() -> {
                    db.productDao().deleteAll();
                    db.clientDao().deleteAll();
                    db.orderDao().deleteAll();
                    db.returnDao().deleteAll();
                });

                // ШАГ 2: КАТАЛОГ
                Response<ApiResponse<List<CategoryGroupDto>>> catalogResp = api.getCatalog().execute();


                if (catalogResp.isSuccessful() && catalogResp.body() != null) {
                    List<CategoryGroupDto> groups = catalogResp.body().getData();
                    if (groups != null) {
                        List<ProductEntity> productEntities = new ArrayList<>();
                        for (CategoryGroupDto group : groups) {
                            for (Product p : group.getProducts()) {
                                productEntities.add(new ProductEntity(p.getId(), p.getName(), p.getPrice(),
                                        p.getItemsPerBox(), p.getBarcode(), group.getCategoryName(), p.getStockQuantity()));
                            }
                        }
                        db.productDao().insertAll(productEntities);
                    }
                } else if (catalogResp.code() == 403) {
                    throw new Exception("Доступ запрещен! Проверьте API-ключ устройства.");
                }

                // ШАГ 3: КЛИЕНТЫ
                Response<List<ClientModel>> clientResp = api.getClients().execute();
                if (clientResp.isSuccessful() && clientResp.body() != null) {
                    List<ClientEntity> entities = new ArrayList<>();
                    for (ClientModel c : clientResp.body()) {
                        ClientEntity ce = new ClientEntity();
                        ce.id = c.id; ce.name = c.name; ce.address = c.address;
                        ce.debt = c.debt; ce.inn = c.inn; ce.ownerName = c.ownerName;
                        ce.routeDay = c.routeDay;
                        entities.add(ce);
                    }
                    db.clientDao().insertAll(entities);
                }

                // ШАГ 4: ЗАКАЗЫ (Оптимизировано)
                Response<List<OrderEntity>> orderResp = api.getOrdersByManager(currentManagerId).execute();
                if (orderResp.isSuccessful() && orderResp.body() != null) {
                    List<OrderEntity> orders = orderResp.body();
                    for (OrderEntity o : orders) { o.status = "SENT"; } // Массово меняем статус в памяти
                    db.orderDao().insertAll(orders); // ОДИН запрос к базе вместо сотни
                }

                // ШАГ 5: ВОЗВРАТЫ (Оптимизировано)
                Response<List<ReturnEntity>> returnResp = api.getReturnsByManager(currentManagerId).execute();
                if (returnResp.isSuccessful() && returnResp.body() != null) {
                    List<ReturnEntity> returns = returnResp.body();
                    for (ReturnEntity r : returns) { r.status = "SENT"; }
                    db.returnDao().insertAll(returns); // ОДИН запрос к базе
                }

                // ФИНАЛ
                requireActivity().runOnUiThread(() -> {
                    if (isAdded()) {
                        appContext.getSharedPreferences("SyncSettings", Context.MODE_PRIVATE)
                                .edit().putBoolean("is_data_loaded", true).apply();
                        updateStatusText();
                        progressDialog.dismiss();
                        Toast.makeText(appContext, "Синхронизация завершена успешно!", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                android.util.Log.d("SYNC_ERROR", "Ошибка: ", e);
                showSyncError("Нет связи с сервером или нет интернета в офисе.");
                requireActivity().runOnUiThread(progressDialog::dismiss);
            }
        }).start();
    }



    // Вспомогательный метод для чистого вывода ошибок
    private void showSyncError(String message) {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Ошибка синхронизации")
                    .setMessage(message)
                    .setPositiveButton("ОК", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        });
    }





    private void dismissProgress() {
        if (getActivity() != null) getActivity().runOnUiThread(() -> progressDialog.dismiss());
    }

    private void clearData() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Очистка")
                .setMessage("Это удалит ВСЕ данные. Вы уверены?")
                .setPositiveButton("Да, удалить", (d, w) -> {
                    new Thread(() -> {
                        AppDatabase db = AppDatabase.getInstance(requireContext());
                        // Правильный порядок очистки для Room 2026
                        db.runInTransaction(() -> {
                            db.cartDao().clearCart();
                            db.productDao().deleteAll();
                            db.clientDao().deleteAll(); // Добавьте этот метод в ClientDao
                            db.orderDao().deleteAll();
                            db.returnDao().deleteAll();
                        });

                        requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply();

                        requireActivity().runOnUiThread(() -> {
                            updateStatusText();
                            Toast.makeText(getContext(), "База полностью очищена", Toast.LENGTH_SHORT).show();
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
            // ИСПРАВЛЕНО: Получаем контекст приложения для безопасности
            Context context = requireContext().getApplicationContext();
            AppDatabase db = AppDatabase.getInstance(context);

            // ИСПРАВЛЕНО: Передаем контекст в ApiClient
            ApiService api = ApiClient.getClient(context).create(ApiService.class);

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
                                .setMessage("Сервер ответил ошибкой (Код: " + (finalStatus ? "OK" : "Error") + "). Попробуйте позже.")
                                .setPositiveButton("Понятно", null)
                                .show();
                    }
                });

            } catch (java.io.IOException e) {
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