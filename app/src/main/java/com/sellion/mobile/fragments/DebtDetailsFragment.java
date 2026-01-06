package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sellion.mobile.R;


public class DebtDetailsFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_debt_details, container, false);

        TextView tvName = view.findViewById(R.id.tvDetailShopName);
        TextView tvOwner = view.findViewById(R.id.tvDetailOwnerName);
        TextView tvInn = view.findViewById(R.id.tvDetailINN);
        TextView tvAddress = view.findViewById(R.id.tvDetailAddress);
        TextView tvAmount = view.findViewById(R.id.tvDetailAmount);
        ImageButton btnBack = view.findViewById(R.id.btnBackToDebtsList);

        // Получаем данные, которые передали из списка
        if (getArguments() != null) {
            tvName.setText(getArguments().getString("SHOP_NAME"));
            tvOwner.setText("Имя ИП: " + getArguments().getString("OWNER_NAME"));
            tvInn.setText("ИНН/ИП: " + getArguments().getString("INN"));
            tvAddress.setText(getArguments().getString("ADDRESS"));

            double amount = getArguments().getDouble("AMOUNT");
            tvAmount.setText(String.format("%,.0f Драм", amount)); // Форматируем число
        }

        // Кнопка назад к списку долгов
        setupBackButton(btnBack, false);

        return view;
    }
}