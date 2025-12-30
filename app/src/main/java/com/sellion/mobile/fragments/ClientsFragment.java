package com.sellion.mobile.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sellion.mobile.R;


public class ClientsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_clients, container, false);
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        ImageButton btnAdd = view.findViewById(R.id.btnAddClient);


        btnBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        btnAdd.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_client, null);

            EditText etName = dialogView.findViewById(R.id.etShopName);
            EditText etAddress = dialogView.findViewById(R.id.etShopAddress);
            EditText etIP = dialogView.findViewById(R.id.etShopIP);

            Context context = getContext();
            if (context != null) {
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
                        .setTitle("Добавление нового клиента")
                        .setView(dialogView)
                        .setNegativeButton("Отмена", null)
                        .setPositiveButton("Создать", (dialog, which) -> {

                            String name = etName.getText().toString();
                            String address = etAddress.getText().toString();
                            String hvhh = etIP.getText().toString();

                            if (!name.isEmpty() && !address.isEmpty() && !hvhh.isEmpty()) {
                                // 3. Если нажали "Создать", показываем второе окно (Успех)
                                showSuccessDialog();
                            } else {
                                Toast.makeText(getContext(), "Введите все данные!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
            } //TODO esle petqe grel

        });
        return view;

    }

    private void showSuccessDialog() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(getContext())
                .setTitle("Успешно")
                .setMessage("Клиент создан. Пожалуйста, отправьте фото ИП для завершения регистрации.")
                .setPositiveButton("Понятно", (dialog, which) -> {
                    // Здесь в будущем мы добавим вызов камеры
                    Toast.makeText(getContext(), "Открываем камеру...", Toast.LENGTH_SHORT).show();
                })
                .setIcon(R.drawable.ic_add) // Создай иконку "галочка" в Vector Asset
                .show();
    }
}