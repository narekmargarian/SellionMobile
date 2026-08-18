package com.sellion.mobile.fragments;

import android.annotation.SuppressLint;
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
import com.sellion.mobile.activities.HostActivity;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
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
        btnVersion.setOnClickListener(v -> checkForUpdate());
        btnUploadPhoto.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Фото отправлено!", Toast.LENGTH_SHORT).show();
            ivPreview.setVisibility(View.GONE);
            btnUploadPhoto.setVisibility(View.GONE);
        });

        return view;
    }


    private void checkForUpdate() {
        Toast.makeText(getContext(), "Проверка обновлений...", Toast.LENGTH_SHORT).show();

        // Получаем текущий versionCode из package manager
        int currentVersionCode;
        try {
            currentVersionCode = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0).versionCode;
        } catch (Exception e) {
            currentVersionCode = 1;
        }

        final int finalCurrentVersionCode = currentVersionCode;
        ApiService api = ApiClient.getClient(requireContext()).create(ApiService.class);

        api.getLatestVersion().enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonString = response.body().string();
                        org.json.JSONObject jsonObject = new org.json.JSONObject(jsonString);
                        int serverVersionCode = jsonObject.getInt("versionCode");

                        if (serverVersionCode > finalCurrentVersionCode) {
                            showUpdateDialog("https://sellion.vip/sellion/updates/app-release.apk");
                        } else {
                            new MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Обновление")
                                    .setMessage("У вас установлена последняя версия программы.")
                                    .setPositiveButton("ОК", null)
                                    .show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Ошибка обработки ответа", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Ошибка сервера: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "Не удалось проверить обновление: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showUpdateDialog(String apkUrl) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Доступно обновление")
                .setMessage("Появилась новая версия приложения. Скачать и установить?")
                .setPositiveButton("Обновить", (dialog, which) -> downloadAndInstallApk(apkUrl))
                .setNegativeButton("Позже", null)
                .show();
    }

    @SuppressLint({"UnspecifiedRegisterReceiverFlag", "WrongConstant"})
    private void downloadAndInstallApk(String url) {
        Toast.makeText(getContext(), "Загрузка обновления...", Toast.LENGTH_SHORT).show();

        android.app.DownloadManager.Request request = new android.app.DownloadManager.Request(android.net.Uri.parse(url));
        request.setTitle("Обновление Sellion Mobile");
        request.setDescription("Загрузка новой версии...");
        request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, "sellion_update.apk");

        android.app.DownloadManager manager = (android.app.DownloadManager) requireContext().getSystemService(Context.DOWNLOAD_SERVICE);
        long downloadId = manager.enqueue(request);

        android.content.BroadcastReceiver onComplete = new android.content.BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(android.app.DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (downloadId == id) {
                    try {
                        android.content.Intent installIntent = new Intent(Intent.ACTION_VIEW);
                        android.net.Uri apkUri;

                        java.io.File file = new java.io.File(
                                android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS),
                                "sellion_update.apk"
                        );

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            apkUri = androidx.core.content.FileProvider.getUriForFile(
                                    context,
                                    context.getPackageName() + ".fileprovider",
                                    file
                            );
                            installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } else {
                            apkUri = android.net.Uri.fromFile(file);
                            installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                        }

                        installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(installIntent);
                        context.unregisterReceiver(this);
                    } catch (Exception e) {
                        Toast.makeText(context, "Ошибка установки: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        };

        android.content.IntentFilter filter = new android.content.IntentFilter(android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE);

        if (android.os.Build.VERSION.SDK_INT >= 33) {
            requireContext().registerReceiver(onComplete, filter, 4); // Context.RECEIVER_EXPORTED
        } else {
            requireContext().registerReceiver(onComplete, filter);
        }
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
                HostActivity.logToFile(appContext, TAG, "Начало загрузки данных с сервера");
                AppDatabase db = AppDatabase.getInstance(appContext);
                ApiService api = ApiClient.getClient(appContext).create(ApiService.class);
                String currentManagerId = SessionManager.getInstance().getManagerId();

                if (currentManagerId == null || currentManagerId.isEmpty()) {
                    throw new Exception("ID менеджера не найден. Перезайдите в систему.");
                }

                // 1. ЗАГРУЖАЕМ КАТАЛОГ С СЕРВЕРА ВО ВРЕМЕННЫЙ СПИСОК (базу пока не очищаем!)
                Response<ApiResponse<List<CategoryGroupDto>>> catalogResp = api.getCatalog().execute();
                List<ProductEntity> newProducts = new ArrayList<>();
                if (catalogResp.isSuccessful() && catalogResp.body() != null) {
                    List<CategoryGroupDto> groups = catalogResp.body().getData();
                    if (groups != null) {
                        for (CategoryGroupDto group : groups) {
                            for (Product p : group.getProducts()) {
                                newProducts.add(new ProductEntity(p.getId(), p.getName(), p.getPrice(),
                                        p.getItemsPerBox(), p.getBarcode(), group.getCategoryName(), p.getStockQuantity()));
                            }
                        }
                    }
                } else if (catalogResp.code() == 403) {
                    throw new Exception("Доступ запрещен! Проверьте API-ключ устройства.");
                } else {
                    throw new Exception("Ошибка загрузки каталога: " + catalogResp.code());
                }

                // 2. ЗАГРУЖАЕМ КЛИЕНТОВ С СЕРВЕРА
                Response<List<ClientModel>> clientResp = api.getClients(currentManagerId).execute();
                List<ClientEntity> newClients = new ArrayList<>();
                if (clientResp.isSuccessful() && clientResp.body() != null) {
                    for (ClientModel c : clientResp.body()) {
                        ClientEntity ce = new ClientEntity();
                        ce.id = c.id;
                        ce.name = c.name;
                        ce.address = c.address;
                        ce.debt = c.debt;
                        ce.inn = c.inn;
                        ce.ownerName = c.ownerName;
                        ce.routeDay = c.routeDay;
                        ce.defaultPercent = c.defaultPercent;
                        newClients.add(ce);
                    }
                } else {
                    throw new Exception("Ошибка загрузки клиентов: " + clientResp.code());
                }

                // 3. БЕЗОПАСНОЕ ОБНОВЛЕНИЕ БАЗЫ ДАННЫХ
                // Если сеть пропала на этапе скачивания, этот блок не выполнится и старые данные не сотрутся.
                db.runInTransaction(() -> {
                    // Обновляем справочники (товары и клиенты)
                    db.productDao().deleteAll();
                    db.productDao().insertAll(newProducts);

                    db.clientDao().deleteAll();
                    db.clientDao().insertAll(newClients);
                });

                // 4. ЗАГРУЖАЕМ ИСТОРИЮ ЗАКАЗОВ (без удаления локальных неотправленных!)
                try {
                    Response<List<OrderEntity>> orderResp = api.getOrdersByManager(currentManagerId).execute();
                    if (orderResp.isSuccessful() && orderResp.body() != null) {
                        List<OrderEntity> orders = orderResp.body();
                        for (OrderEntity o : orders) {
                            o.status = "SENT";
                            if (o.appliedPromoItems == null) o.appliedPromoItems = new HashMap<>();
                        }
                        db.orderDao().insertAll(orders);
                    }
                } catch (Exception ignored) {
                    // Если история заказов с сервера не загрузилась, не обрываем синхронизацию
                }

                // 5. ЗАГРУЖАЕМ ИСТОРИЮ ВОЗВРАТОВ
                try {
                    Response<List<ReturnEntity>> returnResp = api.getReturnsByManager(currentManagerId).execute();
                    if (returnResp.isSuccessful() && returnResp.body() != null) {
                        List<ReturnEntity> returns = returnResp.body();
                        for (ReturnEntity r : returns) { r.status = "SENT"; }
                        db.returnDao().insertAll(returns);
                    }
                } catch (Exception ignored) {
                    // Игнорируем ошибку загрузки истории возвратов
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
                HostActivity.logToFile(appContext, "SYNC_ERROR", e.getMessage());
                requireActivity().runOnUiThread(() -> {
                    if (isAdded()) {
                        progressDialog.dismiss();
                        showSyncError(e.getMessage() != null ? e.getMessage() : "Ошибка соединения с сервером. Обратитесь в офис");
                    }
                });
            }
        }).start();
    }
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
                    final Context appContext = requireContext().getApplicationContext();
                    new Thread(() -> {
                        AppDatabase db = AppDatabase.getInstance(appContext);
                        db.runInTransaction(() -> {
                            db.cartDao().clearCart();
                            db.productDao().deleteAll();
                            db.clientDao().deleteAll();
                            db.orderDao().deleteAll();
                            db.returnDao().deleteAll();
                        });

                        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply();
                        HostActivity.logToFile(appContext, TAG, "База данных полностью очищена пользователем");

                        requireActivity().runOnUiThread(() -> {
                            if (isAdded()) {
                                updateStatusText();
                                Toast.makeText(appContext, "База полностью очищена", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).start();
                })
                .setNegativeButton("Отмена", null)
                .show();
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

    private void takePhotoReport() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            cameraLauncher.launch(takePictureIntent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Камера недоступна", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendDocuments() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_progress, null);
        AlertDialog progressDialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        progressDialog.show();

        final Context appContext = requireContext().getApplicationContext();

        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(appContext);
                ApiService api = ApiClient.getClient(appContext).create(ApiService.class);

                List<OrderEntity> pendingOrders = db.orderDao().getPendingOrdersSync();
                List<ReturnEntity> pendingReturns = db.returnDao().getPendingReturnsSync();

                if (pendingOrders.isEmpty() && pendingReturns.isEmpty()) {
                    requireActivity().runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(appContext, "Нет новых данных для отправки", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                HostActivity.logToFile(appContext, TAG, "Начало отправки документов. Заказов: " + pendingOrders.size() + ", Возвратов: " + pendingReturns.size());
                boolean allOrdersOk = true;
                boolean allReturnsOk = true;

                if (!pendingOrders.isEmpty()) {
                    Response<ApiResponse<Map<String, Object>>> response = api.sendOrders(pendingOrders).execute();
                    if (response.isSuccessful() && response.body() != null) {
                        Map<String, Object> data = response.body().getData();
                        int errors = 0;
                        if (data != null && data.get("errors") instanceof Number) {
                            errors = ((Number) data.get("errors")).intValue();
                        }

                        if (errors == 0) {
                            db.orderDao().markAllAsSent();
                        } else {
                            allOrdersOk = false;
                            HostActivity.logToFile(appContext, "API_ERR", "Сервер отклонил заказы. Ошибок: " + errors);
                        }
                    } else {
                        allOrdersOk = false;
                        HostActivity.logToFile(appContext, "API_ERR", "Заказы не приняты, код: " + response.code());
                    }
                }

                if (!pendingReturns.isEmpty()) {
                    Response<ApiResponse<Map<String, Object>>> response = api.sendReturns(pendingReturns).execute();
                    if (response.isSuccessful() && response.body() != null) {
                        Map<String, Object> data = response.body().getData();
                        int errors = 0;
                        if (data != null && data.get("errors") instanceof Number) {
                            errors = ((Number) data.get("errors")).intValue();
                        }

                        if (errors == 0) {
                            db.returnDao().markAllAsSent();
                        } else {
                            allReturnsOk = false;
                            HostActivity.logToFile(appContext, "API_ERR", "Сервер отклонил возвраты. Ошибок: " + errors);
                        }
                    } else {
                        allReturnsOk = false;
                        HostActivity.logToFile(appContext, "API_ERR", "Возвраты не приняты, код: " + response.code());
                    }
                }

                final boolean finalStatus = allOrdersOk && allReturnsOk;

                requireActivity().runOnUiThread(() -> {
                    if (isAdded()) {
                        progressDialog.dismiss();
                        if (finalStatus) {
                            new MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Успешно")
                                    .setMessage("Все данные переданы в офис.")
                                    .setPositiveButton("ОК", null)
                                    .show();
                        } else {
                            new MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Частичная ошибка")
                                    .setMessage("Некоторые документы не были доставлены из-за ошибок. Попробуйте синхронизацию еще раз.")
                                    .setPositiveButton("Понятно", null)
                                    .show();
                        }
                    }
                });

            } catch (Exception e) {
                HostActivity.logToFile(appContext, "SEND_DOCS_ERROR", e.getMessage());
                requireActivity().runOnUiThread(() -> {
                    if (isAdded()) {
                        progressDialog.dismiss();
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Ошибка связи")
                                .setMessage("Проверьте интернет или статус сервера в офисе.")
                                .setPositiveButton("ОК", null)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                });
            }
        }).start();
    }
}