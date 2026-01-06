package com.sellion.mobile.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.sellion.mobile.R;


public class ReturnDetailsViewFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Используем ваш XML fragment_order_details_view
        View view = inflater.inflate(R.layout.fragment_return_details_view, container, false);

        String shopName = getArguments() != null ? getArguments().getString("order_shop_name") : "Магазин";

        // ID из вашего последнего XML
        TextView tvTitle = view.findViewById(R.id.RtvViewOrderTitle);
        ImageButton btnBack = view.findViewById(R.id.RbtnBackFromView);
        Button btnEdit = view.findViewById(R.id.RbtnEditThisReturn);
        RecyclerView rv = view.findViewById(R.id.RrvViewOrderItems);



        TextView tvTotalSum = view.findViewById(R.id.RtvViewOrderTotalSum);


        if (tvTitle != null) tvTitle.setText("Возврат: " + shopName);

        setupBackButton(btnBack, false);

        if (rv != null) {
            rv.setLayoutManager(new LinearLayoutManager(getContext()));
            // Тут устанавливается адаптер товаров
        }

        return view;
    }
}