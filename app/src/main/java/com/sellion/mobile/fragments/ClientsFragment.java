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
import com.sellion.mobile.entity.ClientModel;
import com.sellion.mobile.helper.NavigationHelper;
import com.sellion.mobile.managers.ClientManager;

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

        // Загружаем данные с сервера
        loadClientsFromServer();

        btnAdd.setOnClickListener(v -> showAddClientDialog());

        btnBack.setOnClickListener(v -> NavigationHelper.backToDashboard(getParentFragmentManager()));
        return view;
    }

    private void loadClientsFromServer() {
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getClients().enqueue(new Callback<List<ClientModel>>() {
            @Override
            public void onResponse(Call<List<ClientModel>> call, Response<List<ClientModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    clientList = response.body();
                    // Обновляем список
                    adapter = new ClientAdapter(clientList, client -> showClientInfoDialog(client));
                    recyclerClients.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<ClientModel>> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                }
            }
        });
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



