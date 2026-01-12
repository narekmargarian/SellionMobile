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
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.ReturnEntity;
import com.sellion.mobile.helper.NavigationHelper;


public class ReturnsFragment extends BaseFragment {

    private RecyclerView recyclerView;
    private ReturnAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_return, container, false);

        ImageButton btnBack = view.findViewById(R.id.btnBackReturn);
        ImageButton btnAddReturn = view.findViewById(R.id.btnAddReturn);
        recyclerView = view.findViewById(R.id.recyclerReturns);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Настройка заголовка в Toolbar
        setupToolbarTitle(view);



        btnAddReturn.setOnClickListener(v -> getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new CreateReturnFragment())
                .addToBackStack(null)
                .commit());

        // Запуск наблюдения за базой данных
        observeReturns();
//        setupBackButton(btnBack, true); // true — значит выход на главный экран




        btnBack.setOnClickListener(v -> {
            // Очищаем стек и выходим на главный экран
            NavigationHelper.backToDashboard(getParentFragmentManager());
        });


        return view;
    }

    private void observeReturns() {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        // LiveData автоматически обновит список, если добавится новый возврат или изменится статус
        db.returnDao().getAllReturnsLive().observe(getViewLifecycleOwner(), returnEntities -> {
            if (returnEntities != null) {
                adapter = new ReturnAdapter(returnEntities, this::openReturnDetailsView);
                recyclerView.setAdapter(adapter);
            }
        });
    }

    private void openReturnDetailsView(ReturnEntity returnEntity) {
        ReturnDetailsViewFragment fragment = new ReturnDetailsViewFragment();
        Bundle args = new Bundle();
        args.putString("order_shop_name", returnEntity.shopName);
        args.putInt("return_id", returnEntity.id);
        fragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void setupToolbarTitle(View view) {
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbarReturn);
        if (toolbar != null) {
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
    }
}