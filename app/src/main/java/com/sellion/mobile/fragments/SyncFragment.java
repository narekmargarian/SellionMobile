package com.sellion.mobile.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.sellion.mobile.entity.ProductEntity;
import com.sellion.mobile.model.ClientModel;
import com.sellion.mobile.model.Product;
import com.sellion.mobile.helper.NavigationHelper;
import com.sellion.mobile.sync.SyncWorker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncFragment extends BaseFragment {

    private TextView tvStatus;
    private ImageView ivPreview;
    private Button btnUploadPhoto;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private AlertDialog progressDialog;

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

        setupBackButton(btnBack, true); // true — значит выход на главный экран
        btnSend.setOnClickListener(v -> sendDocuments());
        btnLoad.setOnClickListener(v -> loadDocuments());
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
        androidx.work.OneTimeWorkRequest syncRequest =
                new androidx.work.OneTimeWorkRequest.Builder(SyncWorker.class).build();

        androidx.work.WorkManager.getInstance(requireContext()).enqueue(syncRequest);

        androidx.work.WorkManager.getInstance(requireContext())
                .getWorkInfoByIdLiveData(syncRequest.getId())
                .observe(getViewLifecycleOwner(), workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        tvStatus.setText("Синхронизация завершена. Данные на сервере!");
                        tvStatus.setTextColor(android.graphics.Color.parseColor("#2E7D32"));
                    }
                });
    }

    // --- ГЛАВНАЯ ЦЕПОЧКА ЗАГРУЗКИ ---
    private void loadDocuments() {
        progressDialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Загрузка")
                .setMessage("Связь с офисом... Обновление данных.")
                .setCancelable(false)
                .create();
        progressDialog.show();

        ApiService api = ApiClient.getClient().create(ApiService.class);
        final Context appContext = requireContext().getApplicationContext();

        // ШАГ 1: Начинаем с загрузки товаров в Room
        loadProductsIntoApp(api, appContext);
    }

    private void loadProductsIntoApp(ApiService api, Context appContext) {
        api.getProducts().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new Thread(() -> {
                        try {
                            List<ProductEntity> entities = new ArrayList<>();
                            for (Product p : response.body()) {
                                entities.add(new ProductEntity(
                                        p.getName(),
                                        p.getPrice(), // Здесь теперь double
                                        p.getItemsPerBox(),
                                        p.getBarcode(),
                                        p.getCategory()
                                ));
                            }
                            // Сохраняем товары (с ценами!) в Room
                            AppDatabase.getInstance(appContext).productDao().insertAll(entities);

                            // ШАГ 2: После товаров грузим клиентов
                            loadClientsIntoApp(api, appContext);

                        } catch (Exception e) {
                            if (getActivity() != null) getActivity().runOnUiThread(() -> progressDialog.dismiss());
                        }
                    }).start();
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(appContext, "Ошибка сервера (товары)", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(appContext, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadClientsIntoApp(ApiService api, Context appContext) {
        api.getClients().enqueue(new Callback<List<ClientModel>>() {
            @Override
            public void onResponse(Call<List<ClientModel>> call, Response<List<ClientModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new Thread(() -> {
                        try {
                            List<ClientEntity> entities = new ArrayList<>();
                            for (ClientModel m : response.body()) {
                                ClientEntity e = new ClientEntity();
                                e.id = m.id;
                                e.name = m.name;
                                e.debt = m.debt;
                                e.address = m.getAddress();
                                e.inn = m.inn;
                                e.ownerName = m.ownerName;
                                e.routeDay = m.routeDay;
                                entities.add(e);
                            }
                            // Сохраняем клиентов в Room
                            AppDatabase.getInstance(appContext).clientDao().insertAll(entities);

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    progressDialog.dismiss();
                                    tvStatus.setText("Данные обновлены: " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
                                    Toast.makeText(appContext, "Все данные успешно загружены!", Toast.LENGTH_SHORT).show();
                                });
                            }
                        } catch (Exception e) {
                            if (getActivity() != null) getActivity().runOnUiThread(() -> progressDialog.dismiss());
                        }
                    }).start();
                } else {
                    if (getActivity() != null) getActivity().runOnUiThread(() -> progressDialog.dismiss());
                }
            }

            @Override
            public void onFailure(Call<List<ClientModel>> call, Throwable t) {
                if (getActivity() != null) getActivity().runOnUiThread(() -> progressDialog.dismiss());
            }
        });
    }

    private void clearData() {
        final Context appContext = requireContext().getApplicationContext();
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Очистка базы")
                .setMessage("Удалить все локальные заказы и возвраты?")
                .setPositiveButton("Да", (d, w) -> {
                    new Thread(() -> {
                        AppDatabase db = AppDatabase.getInstance(appContext);
                        db.orderDao().deleteAll();
                        db.returnDao().deleteAll();
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                tvStatus.setText("Локальная база очищена");
                                tvStatus.setTextColor(android.graphics.Color.RED);
                                Toast.makeText(appContext, "Данные удалены", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }).start();
                })
                .setNegativeButton("Нет", null)
                .show();
    }
}