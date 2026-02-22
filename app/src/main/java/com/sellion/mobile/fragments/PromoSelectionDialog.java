package com.sellion.mobile.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.sellion.mobile.R;
import com.sellion.mobile.adapters.PromoAdapter;
import com.sellion.mobile.model.PromoAction; // Проверь, что в OrderDetailsFragment такой же импорт!

import java.util.List;

public class PromoSelectionDialog extends BottomSheetDialogFragment {
    private List<PromoAction> promos;
    private OnPromoSelectedListener listener;
    private PromoAdapter adapter; // Вынесли в поле, чтобы достать данные при нажатии кнопки

    // ИСПРАВЛЕНО: Теперь принимает List<PromoAction>
    public interface OnPromoSelectedListener {
        void onConfirmed(List<com.sellion.mobile.model.PromoAction> selectedPromos);
        void onSkip();
    }

    public static PromoSelectionDialog newInstance(List<PromoAction> promos, OnPromoSelectedListener listener) {
        PromoSelectionDialog fragment = new PromoSelectionDialog();
        fragment.promos = promos;
        fragment.listener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_promo_selection_dialog, container, false);

        RecyclerView rv = v.findViewById(R.id.rvPromos);
        Button btnSkip = v.findViewById(R.id.btnSkipPromo);

        // ВАЖНО: В разметке R.layout.fragment_promo_selection_dialog
        // должна быть кнопка подтверждения (например, btnConfirmPromo)
        Button btnConfirm = v.findViewById(R.id.btnConfirmPromo);

        if (promos != null) {
            rv.setLayoutManager(new LinearLayoutManager(getContext()));

            // Инициализируем адаптер (логика клика внутри адаптера просто меняет галочки)
            adapter = new PromoAdapter(promos, promo -> {
                // Здесь можно ничего не делать или обновлять какой-то счетчик в UI
            });
            rv.setAdapter(adapter);
        }

        // КНОПКА ПОДТВЕРЖДЕНИЯ (Для применения выбранного списка)
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(view -> {
                if (listener != null && adapter != null) {
                    // Используем метод getSelectedPromos(), который мы добавили в PromoAdapter
                    listener.onConfirmed(adapter.getSelectedPromos());
                }
                dismiss();
            });
        }

        btnSkip.setOnClickListener(view -> {
            if (listener != null) {
                listener.onSkip();
            }
            dismiss();
        });

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        listener = null;
    }
}

