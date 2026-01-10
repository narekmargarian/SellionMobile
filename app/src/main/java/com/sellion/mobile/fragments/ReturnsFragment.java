package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sellion.mobile.R;
import com.sellion.mobile.adapters.ReturnAdapter;
import com.sellion.mobile.entity.ReturnModel;
import com.sellion.mobile.managers.ReturnHistoryManager;

import java.util.List;


public class ReturnsFragment extends BaseFragment {

    private RecyclerView recyclerView;
    private ReturnAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_return, container, false);

        // 1. Инициализация кнопок
        ImageButton btnBack = view.findViewById(R.id.btnBackReturn);
        ImageButton btnAddReturn = view.findViewById(R.id.btnAddReturn);
        recyclerView = view.findViewById(R.id.recyclerReturns);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbarReturn);
        if (toolbar != null) {
            // Находим TextView внутри Toolbar (в вашем XML он второй по счету в RelativeLayout)
            for (int i = 0; i < toolbar.getChildCount(); i++) {
                View child = toolbar.getChildAt(i);
                if (child instanceof RelativeLayout) {
                    RelativeLayout rl = (RelativeLayout) child;
                    for (int j = 0; j < rl.getChildCount(); j++) {
                        if (rl.getChildAt(j) instanceof TextView) {
                            ((TextView) rl.getChildAt(j)).setText("Возвраты сегодня");
                        }
                    }
                }
            }
        }


        // Настройка кнопки назад
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        // Кнопка создания нового возврата
        setupBackButton(btnBack, false);

        btnAddReturn.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CreateReturnFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void updateReturnsList() {
        // Теперь получаем правильный список ReturnModel
        List<ReturnModel> returns = ReturnHistoryManager.getInstance().getReturns();
        // Адаптер теперь должен принимать List<ReturnModel>
        adapter = new ReturnAdapter(returns, this::openReturnDetailsView);
        recyclerView.setAdapter(adapter);
    }

    // ИСПРАВЛЕНО: Аргумент теперь ReturnModel
    private void openReturnDetailsView(ReturnModel returnModel) {
        ReturnDetailsViewFragment fragment = new ReturnDetailsViewFragment();
        Bundle args = new Bundle();
        // Берем имя магазина из модели возврата
        args.putString("order_shop_name", returnModel.shopName);
        fragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateReturnsList();
    }
}