package com.sellion.mobile.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sellion.mobile.R;
import com.sellion.mobile.entity.ClientModel;
import com.sellion.mobile.managers.ClientManager;

import java.util.ArrayList;
import java.util.List;


public class ClientsFragment extends BaseFragment {

    private RecyclerView recyclerClients;
    private ClientAdapter adapter;
    private List<ClientModel> clientList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clients, container, false);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        ImageButton btnAdd = view.findViewById(R.id.btnAddClient);
        recyclerClients = view.findViewById(R.id.recyclerClients);

        setupBackButton(btnBack, false);

        // Создаем 30 тестовых клиентов
        clientList = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            clientList.add(new ClientModel("Магазин №" + i, "Адрес " + i, ""));
        }

        adapter = new ClientAdapter(clientList);
        recyclerClients.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerClients.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> showAddClientDialog());

        return view;
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

    // Адаптер с отображением имени и адреса
    private static class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.ClientViewHolder> {
        private final List<ClientModel> clients;

        public ClientAdapter(List<ClientModel> clients) {
            this.clients = clients;
        }

        @NonNull
        @Override
        public ClientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_client, parent, false);
            return new ClientViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ClientViewHolder holder, int position) {
            ClientModel client = clients.get(position);
            holder.tvName.setText(client.name);
            holder.tvAddress.setText(client.address);
        }

        @Override
        public int getItemCount() {
            return clients.size();
        }

        static class ClientViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvAddress;

            public ClientViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvClientName);
                tvAddress = itemView.findViewById(R.id.tvClientAddress);
            }
        }
    }
}