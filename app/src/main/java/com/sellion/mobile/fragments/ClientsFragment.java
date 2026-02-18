package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sellion.mobile.R;
import com.sellion.mobile.adapters.ClientAdapter;
import com.sellion.mobile.api.ApiClient;
import com.sellion.mobile.api.ApiService;
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.ClientEntity;
import com.sellion.mobile.model.ClientModel;
import com.sellion.mobile.helper.NavigationHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ClientsFragment extends BaseFragment {

    private RecyclerView recyclerClients;
    private ClientAdapter adapter;
    private List<ClientModel> clientList = new ArrayList<>();

    // Элементы поиска
    private LinearLayout layoutSearchFields;
    private EditText etSearchInn, etSearchName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clients, container, false);

        // Инициализация стандартных UI элементов
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        recyclerClients = view.findViewById(R.id.recyclerClients);
        recyclerClients.setLayoutManager(new LinearLayoutManager(getContext()));

        // Инициализация новых элементов поиска
        layoutSearchFields = view.findViewById(R.id.layoutSearchFields);
        etSearchInn = view.findViewById(R.id.etSearchInn);
        etSearchName = view.findViewById(R.id.etSearchName);
        ImageButton btnOpenSearch = view.findViewById(R.id.btnOpenSearch);
        ImageButton btnCloseSearch = view.findViewById(R.id.btnCloseSearch);
        Button btnExecuteSearch = view.findViewById(R.id.btnExecuteSearch);

        // Логика управления панелью поиска
        btnOpenSearch.setOnClickListener(v -> layoutSearchFields.setVisibility(View.VISIBLE));

        btnCloseSearch.setOnClickListener(v -> {
            layoutSearchFields.setVisibility(View.GONE);
            etSearchInn.setText("");
            etSearchName.setText("");
            loadClientsFromLocalDB(); // Возвращаем полный список при закрытии
        });

        btnExecuteSearch.setOnClickListener(v -> performSearch());

        // Загрузка данных
        loadClientsFromLocalDB();

        setupBackButton(btnBack, true);
        return view;
    }

    private void performSearch() {
        // Убираем лишние пробелы по краям
        String inn = etSearchInn.getText().toString().trim();
        String name = etSearchName.getText().toString().trim();

        if (inn.isEmpty() && name.isEmpty()) {
            Toast.makeText(getContext(), "Заполните хотя бы одно поле", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext().getApplicationContext());
            List<ClientEntity> allEntities = db.clientDao().getAllClientsSync();
            List<ClientModel> filteredModels = new ArrayList<>();

            for (ClientEntity e : allEntities) {
                // 1. Точное совпадение по ИНН (включая дефисы, если они есть в базе)
                boolean matchesInn = !inn.isEmpty() && e.inn != null && e.inn.equalsIgnoreCase(inn);

                // 2. Поиск по части имени или владельца (без учета регистра)
                boolean matchesName = !name.isEmpty() && (
                        (e.name != null && e.name.toLowerCase().contains(name.toLowerCase())) ||
                                (e.ownerName != null && e.ownerName.toLowerCase().contains(name.toLowerCase()))
                );

                // Если совпало по ИНН ИЛИ по Имени
                if (matchesInn || matchesName) {
                    filteredModels.add(mapToModel(e));
                }
            }

            requireActivity().runOnUiThread(() -> {
                clientList = filteredModels;
                if (clientList.isEmpty()) {
                    Toast.makeText(getContext(), "Совпадений не найдено", Toast.LENGTH_SHORT).show();
                }
                // Пересоздаем адаптер с отфильтрованным списком
                adapter = new ClientAdapter(clientList, this::showClientInfoDialog);
                recyclerClients.setAdapter(adapter);
            });
        }).start();
    }

    private void loadClientsFromLocalDB() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext().getApplicationContext());
            List<ClientEntity> entities = db.clientDao().getAllClientsSync();
            List<ClientModel> models = new ArrayList<>();
            for (ClientEntity e : entities) {
                models.add(mapToModel(e));
            }

            requireActivity().runOnUiThread(() -> {
                clientList = models;
                if (clientList.isEmpty()) {
                    Toast.makeText(getContext(), "Список пуст. Загрузите данные в Синхронизации.", Toast.LENGTH_LONG).show();
                }
                adapter = new ClientAdapter(clientList, this::showClientInfoDialog);
                recyclerClients.setAdapter(adapter);
            });
        }).start();
    }

    // Вынес маппинг в отдельный метод, чтобы не дублировать код
    private ClientModel mapToModel(ClientEntity e) {
        ClientModel model = new ClientModel();
        model.id = e.id;
        model.name = e.name;
        model.address = e.address;
        model.debt = e.debt;
        model.inn = e.inn;
        model.ownerName = e.ownerName;
        model.routeDay = e.routeDay;
        return model;
    }

    private void showClientInfoDialog(ClientModel client) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_client_info, null);
        TextView tvName = dialogView.findViewById(R.id.tvClientInfoName);
        TextView tvAddress = dialogView.findViewById(R.id.tvClientInfoAddress);
        TextView tvIp = dialogView.findViewById(R.id.tvClientInfoIp);
        Button btnClose = dialogView.findViewById(R.id.btnCloseClientInfo);

        tvName.setText(client.getName());
        tvAddress.setText(client.getAddress());
        tvIp.setText("ИНН/ՀՎՀՀ: " + client.inn );

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        dialog.setContentView(dialogView);
        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}