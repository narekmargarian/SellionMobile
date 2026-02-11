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

    // Интерфейс должен быть public и использовать конкретную модель
    public interface OnPromoSelectedListener {
        void onConfirmed(PromoAction selectedPromo);
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
        // Убедись, что файл разметки называется именно так
        View v = inflater.inflate(R.layout.fragment_promo_selection_dialog, container, false);

        RecyclerView rv = v.findViewById(R.id.rvPromos);
        Button btnSkip = v.findViewById(R.id.btnSkipPromo);

        if (promos != null) {
            rv.setLayoutManager(new LinearLayoutManager(getContext()));
            // Передаем модель в адаптер
            PromoAdapter adapter = new PromoAdapter(promos, promo -> {
                if (listener != null) {
                    listener.onConfirmed(promo);
                }
                dismiss();
            });
            rv.setAdapter(adapter);
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
        // Зануляем слушателя для предотвращения утечек памяти
        listener = null;
    }
}
