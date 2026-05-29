package com.sellion.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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
import com.sellion.mobile.entity.PaymentMethod;
import com.sellion.mobile.managers.CartManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class OrderInfoFragment extends BaseFragment {
    private TextView tvDeliveryDate;
    private TextView tvSelectedClientOrder;

    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru"));
    private final SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_info, container, false);

        tvDeliveryDate = view.findViewById(R.id.tvDeliveryDate);
        tvSelectedClientOrder = view.findViewById(R.id.tvSelectedClientOrder);
        RadioGroup radioGroupPaymentMethod = view.findViewById(R.id.radioGroupPaymentMethod);
        CheckBox checkboxSeparateInvoice = view.findViewById(R.id.checkboxSeparateInvoice);
        LinearLayout layoutSelectDeliveryDate = view.findViewById(R.id.layoutSelectDeliveryDate);
        LinearLayout layoutSelectClientOrder = view.findViewById(R.id.layoutSelectClientOrder);

        // Показываем текущего клиента
        Fragment parent = getParentFragment();
        if (parent instanceof OrderDetailsFragment) {
            String currentStore = ((OrderDetailsFragment) parent).getStoreName();
            if (tvSelectedClientOrder != null && currentStore != null) {
                tvSelectedClientOrder.setText(currentStore);
            }
        }

        // ОТКРЫТИЕ ВЫБОРА КЛИЕНТА ПРИ НАЖАТИИ
        if (layoutSelectClientOrder != null) {
            layoutSelectClientOrder.setOnClickListener(v -> showClientSelectionDialog());
        }

        // Логика даты доставки
        String savedDate = CartManager.getInstance().getDeliveryDate();
        if (savedDate != null && !savedDate.isEmpty()) {
            try {
                Date date = serverFormat.parse(savedDate);
                tvDeliveryDate.setText(displayFormat.format(date));
            } catch (Exception e) {
                tvDeliveryDate.setText(savedDate);
            }
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Calendar.SATURDAY) {
                calendar.add(Calendar.DAY_OF_MONTH, 2);
            } else if (dayOfWeek == Calendar.SUNDAY) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            String dateForServer = serverFormat.format(calendar.getTime());
            String dateForDisplay = displayFormat.format(calendar.getTime());
            tvDeliveryDate.setText(dateForDisplay);
            CartManager.getInstance().setDeliveryDate(dateForServer);
        }

        // Восстановление оплаты
        PaymentMethod savedPayment = CartManager.getInstance().getPaymentMethod();
        if (savedPayment == null) {
            savedPayment = PaymentMethod.TRANSFER;
            CartManager.getInstance().setPaymentMethod(PaymentMethod.TRANSFER);
        }
        if (savedPayment == PaymentMethod.TRANSFER) {
            radioGroupPaymentMethod.check(R.id.radioTransfer);
        } else {
            radioGroupPaymentMethod.check(R.id.radioCash);
        }

        checkboxSeparateInvoice.setChecked(CartManager.getInstance().isSeparateInvoice());

        layoutSelectDeliveryDate.setOnClickListener(v -> showDatePicker());
        checkboxSeparateInvoice.setOnCheckedChangeListener((button, isChecked) -> CartManager.getInstance().setSeparateInvoice(isChecked));
        radioGroupPaymentMethod.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioTransfer) {
                CartManager.getInstance().setPaymentMethod(PaymentMethod.TRANSFER);
            } else if (checkedId == R.id.radioCash) {
                CartManager.getInstance().setPaymentMethod(PaymentMethod.CASH);
            }
        });

        return view;
    }

    // ДИАЛОГ СМЕНЫ МАГАЗИНА ДЛЯ ЗАКАЗА (БЕЗ УДАЛЕНИЯ ТОВАРОВ)
    // ДИАЛОГ СМЕНЫ МАГАЗИНА С АВТОМАТИЧЕСКИМ ПЕРЕРАСЧЕТОМ СКИДКИ
    private void showClientSelectionDialog() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext().getApplicationContext());
            // Загружаем полные сущности клиентов из БД
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

                                // Ищем выбранного клиента в списке, чтобы узнать его процент
                                ClientEntity selectedClient = null;
                                for (ClientEntity e : entities) {
                                    if (selectedStoreName.equals(e.name)) {
                                        selectedClient = e;
                                        break;
                                    }
                                }

                                // ОБНОВЛЯЕМ ПРОЦЕНТ СКИДКИ В КОРЗИНЕ
                                if (selectedClient != null) {
                                    if (selectedClient.defaultPercent > 0) {
                                        CartManager.getInstance().setClientDefaultPercent(
                                                java.math.BigDecimal.valueOf(selectedClient.defaultPercent)
                                        );
                                    } else {
                                        CartManager.getInstance().setClientDefaultPercent(java.math.BigDecimal.ZERO);
                                    }
                                }

                                // Обновляем текст магазина на экране инпута
                                if (tvSelectedClientOrder != null) {
                                    tvSelectedClientOrder.setText(selectedStoreName);
                                }

                                // Получаем родительский фрагмент для обновленияToolbar и аргументов
                                Fragment parent = getParentFragment();
                                if (parent instanceof OrderDetailsFragment) {
                                    if (parent.getArguments() != null) {
                                        parent.getArguments().putString("store_name", selectedStoreName);
                                    }

                                    TextView tvStoreName = parent.getView().findViewById(R.id.tvStoreName);
                                    if (tvStoreName != null) {
                                        tvStoreName.setText(selectedStoreName);
                                    }

                                    // Принудительно уведомляем адаптеры корзины о смене скидки,
                                    // чтобы суммы на экранах пересчитались с новым процентом
                                    AppDatabase.getInstance(requireContext()).cartDao().getCartItemsLive();
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
                .setTitleText("Выберите день доставки")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);
            Date date = calendar.getTime();
            CartManager.getInstance().setDeliveryDate(serverFormat.format(date));
            tvDeliveryDate.setText(displayFormat.format(date));
        });
        datePicker.show(getChildFragmentManager(), "DELIVERY_DATE_PICKER");
    }
}
