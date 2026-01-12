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
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.ClientEntity;
import com.sellion.mobile.model.ClientModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
                m.id = e.id; m.name = e.name; m.address = e.address;
                models.add(m);
            }

            requireActivity().runOnUiThread(() -> {
                ClientAdapter adapter = new ClientAdapter(models, client -> {
                    Fragment parent = getParentFragment();
                    if (parent instanceof CreateOrderFragment) {
                        ((CreateOrderFragment) parent).onClientSelected(client.getName());
                    } else if (parent instanceof CreateReturnFragment) {
                        ((CreateReturnFragment) parent).onClientSelected(client.getName());
                    }
                });
                rv.setAdapter(adapter);
            });
        }).start();
    }
}