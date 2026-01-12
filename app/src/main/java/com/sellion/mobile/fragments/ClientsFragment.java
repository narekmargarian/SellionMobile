package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clients, container, false);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        ImageButton btnAdd = view.findViewById(R.id.btnAddClient);
        recyclerClients = view.findViewById(R.id.recyclerClients);

        recyclerClients.setLayoutManager(new LinearLayoutManager(getContext()));

        // ИЗМЕНЕНО: Теперь загружаем данные из локальной базы Room
        loadClientsFromLocalDB();

        btnAdd.setOnClickListener(v -> showAddClientDialog());

        setupBackButton(btnBack, true); // Выход на Dashboard
        return view;
    }

    private void loadClientsFromLocalDB() {
        // Запускаем фоновый поток для работы с Room
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext().getApplicationContext());
            // Получаем список сущностей из БД
            List<ClientEntity> entities = db.clientDao().getAllClientsSync();

            // Преобразуем ClientEntity в ClientModel для адаптера
            List<ClientModel> models = new ArrayList<>();
            for (ClientEntity e : entities) {
                // Используем конструктор, который подходит под ваш ClientModel
                ClientModel model = new ClientModel();
                model.id = e.id;
                model.name = e.name;
                model.address = e.address;
                model.debt = e.debt;
                model.inn = e.inn;
                model.ownerName = e.ownerName;
                model.routeDay = e.routeDay;
                models.add(model);
            }

            // Обновляем UI в основном потоке
            requireActivity().runOnUiThread(() -> {
                clientList = models;
                if (clientList.isEmpty()) {
                    Toast.makeText(getContext(), "Список пуст. Загрузите данные в Синхронизации.", Toast.LENGTH_LONG).show();
                }
                adapter = new ClientAdapter(clientList, client -> showClientInfoDialog(client));
                recyclerClients.setAdapter(adapter);
            });
        }).start();
    }

    private void showClientInfoDialog(ClientModel client) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_client_info, null);
        TextView tvName = dialogView.findViewById(R.id.tvClientInfoName);
        TextView tvAddress = dialogView.findViewById(R.id.tvClientInfoAddress);
        TextView tvIp = dialogView.findViewById(R.id.tvClientInfoIp);
        Button btnClose = dialogView.findViewById(R.id.btnCloseClientInfo);

        tvName.setText(client.getName());
        tvAddress.setText(client.getAddress());
        tvIp.setText(client.ownerName + " (ИНН: " + client.inn + ")");

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        dialog.setContentView(dialogView);
        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showAddClientDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_client, null);
        EditText etName = dialogView.findViewById(R.id.etShopName);
        EditText etAddress = dialogView.findViewById(R.id.etShopAddress);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Добавление нового клиента")
                .setView(dialogView)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Создать", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String address = etAddress.getText().toString().trim();

                    if (!name.isEmpty() && !address.isEmpty()) {
                        ClientModel newClient = new ClientModel(name, address, "");
                        clientList.add(newClient);
                        adapter.notifyItemInserted(clientList.size() - 1);
                        showSuccessDialog();
                    } else {
                        Toast.makeText(getContext(), "Введите все данные!", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void showSuccessDialog() {
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Успешно")
                .setMessage("Клиент создан.")
                .setPositiveButton("Понятно", null)
                .setIcon(R.drawable.ic_add)
                .show();
    }
}

