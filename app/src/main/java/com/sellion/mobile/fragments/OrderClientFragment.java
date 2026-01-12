package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;
import com.sellion.mobile.adapters.ClientAdapter;
import com.sellion.mobile.api.ApiClient;
import com.sellion.mobile.api.ApiService;
import com.sellion.mobile.model.ClientModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderClientFragment extends Fragment {

    RecyclerView rv;
    private List<ClientModel> clientList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_client, container, false);
        rv = view.findViewById(R.id.OrderRecyclerClients);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        loadClients();

        return view;
    }

    private void loadClients() {
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getClients().enqueue(new Callback<List<ClientModel>>() {
            @Override
            public void onResponse(Call<List<ClientModel>> call, Response<List<ClientModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    clientList = response.body();

                    // Создаем адаптер с новым списком объектов
                    ClientAdapter adapter = new ClientAdapter(clientList, client -> {
                        // ИСПРАВЛЕНИЕ: берем имя из объекта client.getName()
                        String name = client.getName();

                        Fragment parent = getParentFragment();
                        if (parent instanceof CreateOrderFragment) {
                            ((CreateOrderFragment) parent).onClientSelected(name);
                        } else if (parent instanceof CreateReturnFragment) {
                            ((CreateReturnFragment) parent).onClientSelected(name);
                        }
                    });
                    rv.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<ClientModel>> call, Throwable t) {
                if (getContext() != null)
                    Toast.makeText(getContext(), "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }
}