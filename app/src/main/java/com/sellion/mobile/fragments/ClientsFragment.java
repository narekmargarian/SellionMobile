package com.sellion.mobile.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.sellion.mobile.R;


public class ClientsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clients, container, false);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        ImageButton btnAdd = view.findViewById(R.id.btnAddClient);

        // Кнопка НАЗАД
        btnBack.setOnClickListener(v -> {
            // Возвращаемся на предыдущий экран (Dashboard)
            getParentFragmentManager().popBackStack();
        });

        btnAdd.setOnClickListener(v -> {
            // 1. Создаем вид из нашего XML
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_client, null);

            // Находим поля внутри этого вида
            EditText etName = dialogView.findViewById(R.id.etShopName);
            EditText etAddress = dialogView.findViewById(R.id.etShopAddress);
            EditText etIP = dialogView.findViewById(R.id.etShopIP);

            // 2. Строим первое окно (Ввод данных)
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(getContext())
                    .setTitle("Добавление нового клиента")
                    .setView(dialogView)
                    .setNegativeButton("Отмена", null)
                    .setPositiveButton("Создать", (dialog, which) -> {

                        String name = etName.getText().toString();

                        if (!name.isEmpty()) {
                            // 3. Если нажали "Создать", показываем второе окно (Успех)
                            showSuccessDialog();
                        } else {
                            Toast.makeText(getContext(), "Введите название!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
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