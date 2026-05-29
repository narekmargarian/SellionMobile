package com.sellion.mobile.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.sellion.mobile.R;
import com.sellion.mobile.database.AppDatabase;
import com.sellion.mobile.entity.ClientEntity;
import com.sellion.mobile.entity.ReturnReason;
import com.sellion.mobile.managers.CartManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ReturnInfoFragment extends BaseFragment {
    private TextView tvReturnDate;
    private TextView tvSelectedClientReturn;

    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru"));
    private final SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_return_info, container, false);

        tvReturnDate = view.findViewById(R.id.tvReturnDate);
        tvSelectedClientReturn = view.findViewById(R.id.tvSelectedClientReturn);
        RadioGroup radioGroupReason = view.findViewById(R.id.radioGroupReturnReason);
        LinearLayout layoutSelectDate = view.findViewById(R.id.layoutSelectReturnDate);
        LinearLayout layoutSelectClientReturn = view.findViewById(R.id.layoutSelectClientReturn);

        // Показываем текущего клиента
        Fragment parent = getParentFragment();
        if (parent instanceof ReturnDetailsFragment) {
            String currentStore = ((ReturnDetailsFragment) parent).getStoreName();
            if (tvSelectedClientReturn != null && currentStore != null) {
                tvSelectedClientReturn.setText(currentStore);
            }
        }

        // ОТКРЫТИЕ ВЫБОРА КЛИЕНТА ПРИ НАЖАТИИ (ДЛЯ ВОЗВРАТА)
        if (layoutSelectClientReturn != null) {
            layoutSelectClientReturn.setOnClickListener(v -> showClientReturnSelectionDialog());
        }

        // Логика даты возврата
        String savedDate = CartManager.getInstance().getReturnDate();
        if (savedDate != null && !savedDate.isEmpty()) {
            try {
                Date date = serverFormat.parse(savedDate);
                tvReturnDate.setText(displayFormat.format(date));
            } catch (Exception e) {
                tvReturnDate.setText(savedDate);
            }
        } else {
            SharedPreferences prefs = requireContext().getSharedPreferences("SyncSettings", Context.MODE_PRIVATE);
            boolean isSixDayWorkWeek = prefs.getBoolean("is_six_day_work", false);

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

            if (isSixDayWorkWeek) {
                if (dayOfWeek == Calendar.SUNDAY) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
            } else {
                if (dayOfWeek == Calendar.SATURDAY) {
                    calendar.add(Calendar.DAY_OF_MONTH, 2);
                } else if (dayOfWeek == Calendar.SUNDAY) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
            }

            String dateForServer = serverFormat.format(calendar.getTime());
            String dateForDisplay = displayFormat.format(calendar.getTime());
            tvReturnDate.setText(dateForDisplay);
            CartManager.getInstance().setReturnDate(dateForServer);
        }

        // Восстановление причины
        ReturnReason savedReason = CartManager.getInstance().getReturnReason();
        if (savedReason != null) {
            for (int i = 0; i < radioGroupReason.getChildCount(); i++) {
                View child = radioGroupReason.getChildAt(i);
                if (child instanceof RadioButton) {
                    RadioButton rb = (RadioButton) child;
                    if (rb.getText().toString().equals(savedReason.getTitle())) {
                        rb.setChecked(true);
                        break;
                    }
                }
            }
        }

        layoutSelectDate.setOnClickListener(v -> showDatePicker());
        radioGroupReason.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton rb = group.findViewById(checkedId);
            if (rb != null) {
                String text = rb.getText().toString();
                for (ReturnReason reason : ReturnReason.values()) {
                    if (reason.getTitle().equals(text)) {
                        CartManager.getInstance().setReturnReason(reason);
                        break;
                    }
                }
            }
        });

        return view;
    }

    // ДИАЛОГ СМЕНЫ МАГАЗИНА ДЛЯ ВОЗВРАТА С ОБНОВЛЕНИЕМ СКИДКИ
    private void showClientReturnSelectionDialog() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext().getApplicationContext());
            List<ClientEntity> entities = db.clientDao().getAllClientsSync();

            List<String> clientNames = new ArrayList<>();
            for (ClientEntity e : entities) {
                if (e.name != null) clientNames.add(e.name);
            }

            String[] namesArray = clientNames.toArray(new String[0]);

            requireActivity().runOnUiThread(() -> {
                if (isAdded()) {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Выберите магазин")
                            .setItems(namesArray, (dialog, which) -> {
                                String selectedStoreName = namesArray[which];

                                // Ищем выбранного клиента в списке
                                ClientEntity selectedClient = null;
                                for (ClientEntity e : entities) {
                                    if (selectedStoreName.equals(e.name)) {
                                        selectedClient = e;
                                        break;
                                    }
                                }

                                // ОБНОВЛЯЕМ ПРОЦЕНТ В КОРЗИНЕ ДЛЯ СИНХРОННОСТИ
                                if (selectedClient != null) {
                                    if (selectedClient.defaultPercent > 0) {
                                        CartManager.getInstance().setClientDefaultPercent(
                                                java.math.BigDecimal.valueOf(selectedClient.defaultPercent)
                                        );
                                    } else {
                                        CartManager.getInstance().setClientDefaultPercent(java.math.BigDecimal.ZERO);
                                    }
                                }

                                if (tvSelectedClientReturn != null) {
                                    tvSelectedClientReturn.setText(selectedStoreName);
                                }

                                Fragment parent = getParentFragment();
                                if (parent instanceof ReturnDetailsFragment) {
                                    if (parent.getArguments() != null) {
                                        parent.getArguments().putString("store_name", selectedStoreName);
                                    }

                                    TextView tvReturnStoreName = parent.getView().findViewById(R.id.tvReturnStoreName);
                                    if (tvReturnStoreName != null) {
                                        tvReturnStoreName.setText(selectedStoreName + " (Возврат)");
                                    }
                                }
                            })
                            .setNegativeButton("Отмена", null)
                            .show();
                }
            });
        }).start();
    }


    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Выберите дату возврата")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);
            Date date = calendar.getTime();
            CartManager.getInstance().setReturnDate(serverFormat.format(date));
            tvReturnDate.setText(displayFormat.format(date));
        });
        datePicker.show(getChildFragmentManager(), "RETURN_DATE_PICKER");
    }
}
