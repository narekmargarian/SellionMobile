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
import com.sellion.mobile.managers.ClientManager;

import java.util.List;

public class OrderClientFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_client, container, false);
        RecyclerView rv = view.findViewById(R.id.OrderRecyclerClients);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        List<String> clients = ClientManager.getInstance().getStoreNames();

        ClientAdapter adapter = new ClientAdapter(clients, name -> {
            // Получаем родителя фрагмента (это будет CreateOrderFragment или CreateReturnFragment)
            Fragment parent = getParentFragment();

            if (parent instanceof CreateOrderFragment) {
                // Если открыли через "Заказ"
                ((CreateOrderFragment) parent).onClientSelected(name);
            } else if (parent instanceof CreateReturnFragment) {
                // Если открыли через "Возврат"
                ((CreateReturnFragment) parent).onClientSelected(name);
            }
        });

        rv.setAdapter(adapter);
        return view;
    }
}