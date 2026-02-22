package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_client, container, false);
        rv = view.findViewById(R.id.OrderRecyclerClients);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        loadClientsFromDb();
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
                m.defaultPercent = e.defaultPercent; // Передаем процент из БД
                models.add(m);
            }

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    ClientAdapter adapter = new ClientAdapter(models, client -> {

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
                });
            }
        }).start(); // ИСПРАВЛЕНО: Закрыта скобка потока и добавлен запуск
    }
}
