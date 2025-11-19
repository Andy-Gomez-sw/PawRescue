package com.refugio.pawrescue.ui.admin.tabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.refugio.pawrescue.R;

/**
 * Fragmento que muestra el Historial Médico y de Cuidados (RF-09, RF-10).
 */
public class HistoryFragment extends Fragment {

    private static final String ARG_ANIMAL_ID = "animal_id";
    private String animalId;

    public static HistoryFragment newInstance(String animalId) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ANIMAL_ID, animalId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            animalId = getArguments().getString(ARG_ANIMAL_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        TextView tvTitle = view.findViewById(R.id.tv_history_title);
        tvTitle.setText("Historial de Eventos para: " + animalId);

        TextView tvPlaceholder = view.findViewById(R.id.tv_history_placeholder);
        tvPlaceholder.setText("Módulo en desarrollo (RF-09, RF-10). Aquí se listarán los registros médicos y de cuidados.");

        return view;
    }
}