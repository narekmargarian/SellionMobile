package com.sellion.mobile.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.card.MaterialCardView;
import com.sellion.mobile.MainActivity;
import com.sellion.mobile.R;
import com.sellion.mobile.helper.NavigationHelper;
import com.sellion.mobile.managers.SessionManager;


public class DashboardFragment extends BaseFragment {

    public DashboardFragment() {


    }
    TextView managerTitle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Инициализация UI
        managerTitle = view.findViewById(R.id.managerTitle);
        ImageButton btnExit = view.findViewById(R.id.btnExit);
        MaterialCardView cardClients = view.findViewById(R.id.cardClients);
        MaterialCardView cardOrders = view.findViewById(R.id.cardOrders);
        MaterialCardView cardDebts = view.findViewById(R.id.cardDebts);
        MaterialCardView cardSync = view.findViewById(R.id.cardSync);
        MaterialCardView cardReturn = view.findViewById(R.id.cardReturn);
        MaterialCardView cardCatalog = view.findViewById(R.id.cardCatalog);




        btnExit.setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM);

            com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog =
                    new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext());

            View sheetView = getLayoutInflater().inflate(R.layout.layout_logout_sheet, null);

            sheetView.findViewById(R.id.btnConfirmLogout).setOnClickListener(v1 -> {
                bottomSheetDialog.dismiss();
                logout();
            });

            sheetView.findViewById(R.id.btnCancel).setOnClickListener(v1 -> bottomSheetDialog.dismiss());

            bottomSheetDialog.setContentView(sheetView);
            bottomSheetDialog.show();
        });


// Метод для чистого выхода

        // Установка имени менеджера
        String managerId = SessionManager.getInstance().getManagerId();
        if (managerId != null) {
            managerTitle.setText("Менеджер: " + managerId);
        }

        // Использование NavigationHelper для всех переходов
        // Теперь кнопка "Назад" из любого раздела вернет строго сюда

        cardClients.setOnClickListener(v ->
                NavigationHelper.openSection(getParentFragmentManager(), new ClientsFragment()));

        cardOrders.setOnClickListener(v ->
                NavigationHelper.openSection(getParentFragmentManager(), new OrdersFragment()));

        cardDebts.setOnClickListener(v ->
                NavigationHelper.openSection(getParentFragmentManager(), new DebtsFragment()));

        cardSync.setOnClickListener(v ->
                NavigationHelper.openSection(getParentFragmentManager(), new SyncFragment()));

        cardCatalog.setOnClickListener(v ->
                NavigationHelper.openSection(getParentFragmentManager(), new CatalogFragment()));

        cardReturn.setOnClickListener(v ->
                NavigationHelper.openSection(getParentFragmentManager(), new ReturnsFragment()));

        return view;
    }

    private void logout() {
        SessionManager.getInstance().clearSession();
        Intent intent = new Intent(requireContext(), MainActivity.class);
        // Анимация плавного перехода
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
            // Плавное исчезновение экрана
            getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

}