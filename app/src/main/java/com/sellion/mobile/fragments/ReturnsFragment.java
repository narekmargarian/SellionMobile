package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.sellion.mobile.R;
import com.sellion.mobile.adapters.ReturnAdapter;
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.ReturnEntity;
import com.sellion.mobile.helper.NavigationHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ReturnsFragment extends BaseFragment {
    private RecyclerView recyclerView;
    private ReturnAdapter adapter;
    private TextView tvTitle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_return, container, false);

        recyclerView = view.findViewById(R.id.recyclerReturns);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        Toolbar toolbar = view.findViewById(R.id.toolbarReturn);
        if (toolbar != null && toolbar.getChildAt(0) instanceof RelativeLayout) {
            RelativeLayout rl = (RelativeLayout) toolbar.getChildAt(0);
            for (int i = 0; i < rl.getChildCount(); i++) {
                if (rl.getChildAt(i) instanceof TextView) {
                    tvTitle = (TextView) rl.getChildAt(i);
                    break;
                }
            }
        }

        view.findViewById(R.id.btnBackReturn).setOnClickListener(v -> NavigationHelper.backToDashboard(getParentFragmentManager()));
        view.findViewById(R.id.btnAddReturn).setOnClickListener(v ->
                getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, new CreateReturnFragment()).addToBackStack(null).commit()
        );

        view.findViewById(R.id.btnFilterOrders).setOnClickListener(this::showFilterMenu);

        // По умолчанию за сегодня
        showReturnsToday();

        return view;
    }

    private void showReturnsToday() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        loadReturns(today + "T00:00:00", today + "T23:59:59", "Возвраты сегодня");
    }

    private void showReturnsThisMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        String start = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime()) + "T00:00:00";

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        String end = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime()) + "T23:59:59";

        loadReturns(start, end, "Возвраты за месяц");
    }

    private void showDateRangePicker() {
        MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Выберите период возвратов")
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection.first != null && selection.second != null) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String start = df.format(new Date(selection.first)) + "T00:00:00";
                String end = df.format(new Date(selection.second)) + "T23:59:59";
                loadReturns(start, end, "Период: " + df.format(new Date(selection.first)) + " - " + df.format(new Date(selection.second)));
            }
        });
        picker.show(getParentFragmentManager(), "RANGE_PICKER");
    }

    private void loadReturns(String start, String end, String title) {
        if (tvTitle != null) tvTitle.setText(title);
        AppDatabase.getInstance(requireContext()).returnDao()
                .getReturnsBetweenDates(start, end)
                .observe(getViewLifecycleOwner(), list -> {
                    adapter = new ReturnAdapter(list, this::openReturnDetailsView);
                    recyclerView.setAdapter(adapter);
                });
    }

    private void showFilterMenu(View v) {
        PopupMenu popup = new PopupMenu(requireContext(), v);
        popup.getMenu().add("За сегодня");
        popup.getMenu().add("За этот месяц");
        popup.getMenu().add("Выбрать период");
        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.equals("За сегодня")) showReturnsToday();
            else if (title.equals("За этот месяц")) showReturnsThisMonth();
            else if (title.equals("Выбрать период")) showDateRangePicker();
            return true;
        });
        popup.show();
    }

    private void openReturnDetailsView(ReturnEntity returnEntity) {
        ReturnDetailsViewFragment fragment = new ReturnDetailsViewFragment();
        Bundle args = new Bundle();
        args.putString("order_shop_name", returnEntity.shopName);
        args.putInt("return_id", returnEntity.id);
        fragment.setArguments(args);
        getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
    }
}