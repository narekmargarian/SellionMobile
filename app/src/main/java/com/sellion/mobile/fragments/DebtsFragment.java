package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;
import com.sellion.mobile.adapters.DebtsAdapter;
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.ClientEntity;
import com.sellion.mobile.helper.NavigationHelper;

import java.util.ArrayList;
import java.util.List;

public class DebtsFragment extends BaseFragment {
    private RecyclerView recyclerView;
    private DebtsAdapter adapter;
    private TextView tvTotalDebtSum;

    private LinearLayout layoutSearchFieldsDebts;
    private EditText etSearchNameDebts;
    private List<ClientEntity> allDebtsList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_debts, container, false);
        recyclerView = view.findViewById(R.id.recyclerDebts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ImageButton btnBack = view.findViewById(R.id.btnBackDebts);
        tvTotalDebtSum = view.findViewById(R.id.tvTotalDebtSum);

        // Элементы поиска
        ImageButton btnOpenSearch = view.findViewById(R.id.btnOpenSearchDebts);
        ImageButton btnCloseSearch = view.findViewById(R.id.btnCloseSearchDebts);
        layoutSearchFieldsDebts = view.findViewById(R.id.layoutSearchFieldsDebts);
        etSearchNameDebts = view.findViewById(R.id.etSearchNameDebts);
        Button btnExecuteSearch = view.findViewById(R.id.btnExecuteSearchDebts);

        if (btnOpenSearch != null && layoutSearchFieldsDebts != null) {
            btnOpenSearch.setOnClickListener(v -> layoutSearchFieldsDebts.setVisibility(View.VISIBLE));
            btnCloseSearch.setOnClickListener(v -> {
                layoutSearchFieldsDebts.setVisibility(View.GONE);
                etSearchNameDebts.setText("");
                filterList("");
            });

            btnExecuteSearch.setOnClickListener(v -> {
                String query = etSearchNameDebts.getText().toString().trim();
                filterList(query);
            });

            etSearchNameDebts.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterList(s.toString().trim());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // НАБЛЮДАЕМ ЗА БАЗОЙ (БЕЗ ИНТЕРНЕТА)
        AppDatabase.getInstance(requireContext()).clientDao().getClientsWithDebtsLive()
                .observe(getViewLifecycleOwner(), clients -> {
                    if (clients != null) {
                        allDebtsList = clients;
                        double totalDebt = 0;
                        for (ClientEntity client : clients) {
                            totalDebt += client.debt;
                        }
                        if (tvTotalDebtSum != null) {
                            tvTotalDebtSum.setText(formatSmart(totalDebt) + " ֏");
                        }

                        // Фильтруем список под текущий поисковый запрос
                        String currentQuery = etSearchNameDebts != null ? etSearchNameDebts.getText().toString().trim() : "";
                        filterList(currentQuery);
                    }
                });

        setupBackButton(btnBack, true); // true — значит выход на главный экран
        return view;
    }

    private void filterList(String query) {
        List<ClientEntity> filteredList = new ArrayList<>();
        if (query == null || query.isEmpty()) {
            filteredList.addAll(allDebtsList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (ClientEntity client : allDebtsList) {
                boolean nameMatch = client.name != null && client.name.toLowerCase().contains(lowerQuery);
                boolean ownerMatch = client.ownerName != null && client.ownerName.toLowerCase().contains(lowerQuery);
                if (nameMatch || ownerMatch) {
                    filteredList.add(client);
                }
            }
        }

        adapter = new DebtsAdapter(filteredList, client -> openDetails(client));
        recyclerView.setAdapter(adapter);
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
        bundle.putString("OWNER_NAME", client.ownerName);

        detailsFragment.setArguments(bundle);

        // 3. Выполняем переход
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailsFragment)
                .addToBackStack(null)
                .commit();
    }
}