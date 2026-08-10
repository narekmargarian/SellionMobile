package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;
import com.sellion.mobile.adapters.ClientAdapter;
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.ClientEntity;
import com.sellion.mobile.model.ClientModel;

import java.util.ArrayList;
import java.util.List;

public class OrderClientFragment extends Fragment {

    private RecyclerView rv;
    private EditText etSearch;
    private List<ClientModel> fullClientList = new ArrayList<>();
    private ClientAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_client, container, false);
        rv = view.findViewById(R.id.OrderRecyclerClients);
        etSearch = view.findViewById(R.id.etSearchOrderClient);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        loadClientsFromDb();

        // Слушатель для мгновенного поиска по вводу
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterClients(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        return view;
    }

    private void loadClientsFromDb() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext().getApplicationContext());
            List<ClientEntity> entities = db.clientDao().getAllClientsSync();

            List<ClientModel> models = new ArrayList<>();
            for (ClientEntity e : entities) {
                ClientModel m = new ClientModel();
                m.id = e.id;
                m.name = e.name;
                m.address = e.address;
                m.inn = e.inn;
                m.ownerName = e.ownerName;
                m.defaultPercent = e.defaultPercent; // Передаем процент из БД
                models.add(m);
            }

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    fullClientList = models;
                    setupAdapter(fullClientList);
                });
            }
        }).start();
    }

    private void filterClients(String query) {
        List<ClientModel> filteredList = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();

        if (lowerQuery.isEmpty()) {
            filteredList.addAll(fullClientList);
        } else {
            for (ClientModel client : fullClientList) {
                boolean matchesName = client.getName() != null && client.getName().toLowerCase().contains(lowerQuery);
                boolean matchesInn = client.inn != null && client.inn.toLowerCase().contains(lowerQuery);
                boolean matchesOwner = client.ownerName != null && client.ownerName.toLowerCase().contains(lowerQuery);

                if (matchesName || matchesInn || matchesOwner) {
                    filteredList.add(client);
                }
            }
        }
        setupAdapter(filteredList);
    }

    private void setupAdapter(List<ClientModel> list) {
        adapter = new ClientAdapter(list, client -> {

            Fragment parent = getParentFragment();

            if (parent instanceof CreateOrderFragment) {
                // 1. ОЧИСТКА КОРЗИНЫ (от синих товаров)
                com.sellion.mobile.managers.CartManager.getInstance().clearCart();

                // 2. УСТАНОВКА ПРОЦЕНТА (для расчета 5%)
                com.sellion.mobile.managers.CartManager.getInstance()
                        .setClientDefaultPercent(java.math.BigDecimal.valueOf(client.defaultPercent));

                // 3. ПЕРЕДАЧА ОБЪЕКТА (для CreateOrderFragment)
                ((CreateOrderFragment) parent).onClientSelected(client);

            } else if (parent instanceof CreateReturnFragment) {
                // ДЛЯ ВОЗВРАТА: Процент не нужен, передаем только строку имени
                // Это исправит ошибку "Required String, Provided ClientModel"
                ((CreateReturnFragment) parent).onClientSelected(client.getName());
            }
        });
        rv.setAdapter(adapter);
    }
}