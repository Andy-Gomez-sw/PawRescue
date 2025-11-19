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
 * Fragmento que gestiona las Solicitudes de Adopción (RF-14, RF-15, RF-16).
 */
public class AdoptionFragment extends Fragment {

    private static final String ARG_ANIMAL_ID = "animal_id";
    private String animalId;

    public static AdoptionFragment newInstance(String animalId) {
        AdoptionFragment fragment = new AdoptionFragment();
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
        View view = inflater.inflate(R.layout.fragment_adoption, container, false);

        TextView tvTitle = view.findViewById(R.id.tv_adoption_title);
        tvTitle.setText("Proceso de Adopción para: " + animalId);

        TextView tvPlaceholder = view.findViewById(R.id.tv_adoption_placeholder);
        tvPlaceholder.setText("Módulo en desarrollo (RF-14, RF-15, RF-16). Aquí se gestionarán las solicitudes y citas.");

        return view;
    }
}