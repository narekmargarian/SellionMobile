package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;
import com.sellion.mobile.adapters.DebtsAdapter;
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.ClientEntity;
import com.sellion.mobile.helper.NavigationHelper;

public class DebtsFragment extends BaseFragment {
    private RecyclerView recyclerView;
    private DebtsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_debts, container, false);
        recyclerView = view.findViewById(R.id.recyclerDebts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ImageButton btnBack = view.findViewById(R.id.btnBackDebts);

        // НАБЛЮДАЕМ ЗА БАЗОЙ (БЕЗ ИНТЕРНЕТА)
        AppDatabase.getInstance(requireContext()).clientDao().getClientsWithDebtsLive()
                .observe(getViewLifecycleOwner(), clients -> {
                    if (clients != null) {
                        // Превращаем ClientEntity обратно в ClientModel для адаптера или обновляем адаптер
                        adapter = new DebtsAdapter(clients, client -> openDetails(client));
                        recyclerView.setAdapter(adapter);
                    }
                });
        setupBackButton(btnBack, true); // true — значит выход на главный экран
        return view;
    }

    private void openDetails(ClientEntity client) {
        // 1. Создаем фрагмент деталей (убедитесь, что класс DebtDetailsFragment существует)
        DebtDetailsFragment detailsFragment = new DebtDetailsFragment();

        // 2. Упаковываем данные из базы (ID, Имя, Долг) для передачи
        Bundle bundle = new Bundle();
        bundle.putInt("CLIENT_ID", client.id);
        bundle.putString("SHOP_NAME", client.name);
        bundle.putDouble("AMOUNT", client.debt);
        bundle.putString("ADDRESS", client.address);
        bundle.putString("INN", client.inn);

        detailsFragment.setArguments(bundle);

        // 3. Выполняем переход
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailsFragment)
                .addToBackStack(null)
                .commit();
    }


}
