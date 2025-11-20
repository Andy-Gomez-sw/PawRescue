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
import com.refugio.pawrescue.model.Animal;

import java.io.Serializable;

/**
 * Fragmento que muestra la información general del animal (RF-06).
 */
public class InfoFragment extends Fragment {

    private static final String ARG_ANIMAL = "animal_object";
    private Animal animal;

    public static InfoFragment newInstance(Animal animal) {
        InfoFragment fragment = new InfoFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ANIMAL, (Serializable) animal);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Se debe usar Parcelable/Serializable, asumimos Serializable para simplificar.
            animal = (Animal) getArguments().getSerializable(ARG_ANIMAL);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        if (animal != null) {
            TextView tvId = view.findViewById(R.id.tv_info_id);
            TextView tvNombre = view.findViewById(R.id.tv_info_nombre);
            TextView tvEspecie = view.findViewById(R.id.tv_info_especie);
            TextView tvRaza = view.findViewById(R.id.tv_info_raza);
            TextView tvEstado = view.findViewById(R.id.tv_info_estado);
            TextView tvRescate = view.findViewById(R.id.tv_info_rescate);

            tvId.setText("ID: " + animal.getIdNumerico());
            tvNombre.setText("Nombre: " + animal.getNombre());
            tvEspecie.setText("Especie: " + animal.getEspecie());
            tvRaza.setText("Raza: " + animal.getRaza());
            tvEstado.setText("Estado: " + animal.getEstadoRefugio());

            // Formateo de fecha de rescate
            String fecha = (animal.getFechaRegistro() != null) ? animal.getFechaRegistro().toDate().toString() : "N/A";
            tvRescate.setText("Rescatado: " + fecha);
        }

        return view;
    }
}