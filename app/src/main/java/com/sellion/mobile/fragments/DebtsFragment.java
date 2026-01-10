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
import com.sellion.mobile.entity.ClientModel;
import com.sellion.mobile.entity.DebtModel;
import com.sellion.mobile.managers.ClientManager;

import java.util.ArrayList;
import java.util.List;


public class DebtsFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_debts, container, false);
        ImageButton btnBack = view.findViewById(R.id.btnBackDebts);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerDebts);

        setupBackButton(btnBack, false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Используем реальные магазины из менеджера для создания тестовых долгов
        List<DebtModel> debtList = new ArrayList<>();

        // Получаем реальные модели из менеджера
        ClientModel client1 = ClientManager.getInstance().getClientByName("ZOVQ Arshakunyac");
        ClientModel client2 = ClientManager.getInstance().getClientByName("Carrefour ТЦ Ереван Мол");
        ClientModel client3 = ClientManager.getInstance().getClientByName("MG Маркет Аван");

        if (client1 != null)
            debtList.add(new DebtModel(client1.getName(), client1.getIp(), "0254871", client1.getAddress(), 45000));
        if (client2 != null)
            debtList.add(new DebtModel(client2.getName(), client2.getIp(), "0312457", client2.getAddress(), 125000));
        if (client3 != null)
            debtList.add(new DebtModel(client3.getName(), client3.getIp(), "0125478", client3.getAddress(), 0));

        DebtsAdapter adapter = new DebtsAdapter(debtList, this::openDetails);
        recyclerView.setAdapter(adapter);
        return view;
    }

    private void openDetails(DebtModel debt) {
        DebtDetailsFragment detailsFragment = new DebtDetailsFragment();

        Bundle bundle = new Bundle();
        bundle.putString("SHOP_NAME", debt.getShopName());
        bundle.putString("OWNER_NAME", debt.getOwnerName());
        bundle.putString("INN", debt.getInn());
        bundle.putString("ADDRESS", debt.getAddress());
        bundle.putDouble("AMOUNT", debt.getDebtAmount());
        detailsFragment.setArguments(bundle);

        // Переходим на экран деталей
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailsFragment)
                .addToBackStack(null)
                .commit();
    }
}
