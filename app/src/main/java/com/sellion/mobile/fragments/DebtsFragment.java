package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;
import com.sellion.mobile.adapters.DebtsAdapter;
import com.sellion.mobile.entity.DebtModel;

import java.util.ArrayList;
import java.util.List;


public class DebtsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_debts, container, false);
        ImageButton btnBack = view.findViewById(R.id.btnBackDebts);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerDebts);

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        List<DebtModel> debtList = new ArrayList<>();
        debtList.add(new DebtModel("Магазин Ани", "ИП Саргсян А.", "0254871", "Ереван, Абовяна 12", 45000));
        debtList.add(new DebtModel("Продукты Гюмри", "ИП Карапетян М.", "0312457", "Гюмри, Ширакаци 5", 125000));
        debtList.add(new DebtModel("Зигзаг Маркет", "ООО Ривенто", "0125478", "Ереван, Комитаса 44", 0));


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
