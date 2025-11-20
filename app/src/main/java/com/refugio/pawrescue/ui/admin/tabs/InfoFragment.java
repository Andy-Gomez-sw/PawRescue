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
import java.text.SimpleDateFormat;
import java.util.Locale;

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
            animal = (Animal) getArguments().getSerializable(ARG_ANIMAL);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        if (animal != null) {
            TextView tvId = view.findViewById(R.id.tv_info_id);
            TextView tvNombre = view.findViewById(R.id.tv_info_nombre);
            TextView tvEspecie = view.findViewById(R.id.tv_info_especie);
            TextView tvRaza = view.findViewById(R.id.tv_info_raza);
            TextView tvSexo = view.findViewById(R.id.tv_info_sexo);
            TextView tvEstado = view.findViewById(R.id.tv_info_estado);
            TextView tvRescate = view.findViewById(R.id.tv_info_rescate);
            TextView tvUbicacion = view.findViewById(R.id.tv_info_ubicacion);
            TextView tvCondiciones = view.findViewById(R.id.tv_info_condiciones);

            // ID Numérico formateado correctamente
            String idDisplay = String.format(Locale.US, "#%04d", animal.getIdNumerico());
            tvId.setText("ID: " + idDisplay);

            tvNombre.setText("Nombre: " + (animal.getNombre() != null ? animal.getNombre() : "N/A"));
            tvEspecie.setText("Especie: " + (animal.getEspecie() != null ? animal.getEspecie() : "N/A"));
            tvRaza.setText("Raza: " + (animal.getRaza() != null ? animal.getRaza() : "N/A"));
            tvSexo.setText("Sexo: " + (animal.getSexo() != null ? animal.getSexo() : "N/A"));
            tvEstado.setText("Estado: " + (animal.getEstadoRefugio() != null ? animal.getEstadoRefugio() : "N/A"));

            // Formateo de fecha más legible
            if (animal.getFechaRegistro() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                String fecha = sdf.format(animal.getFechaRegistro().toDate());
                tvRescate.setText("Rescatado: " + fecha);
            } else {
                tvRescate.setText("Rescatado: N/A");
            }

            // Ubicación GPS
            if (animal.getUbicacionRescate() != null) {
                String ubicacion = String.format(Locale.US, "Lat: %.6f, Lon: %.6f",
                        animal.getUbicacionRescate().getLatitude(),
                        animal.getUbicacionRescate().getLongitude());
                tvUbicacion.setText("Ubicación: " + ubicacion);
            } else {
                tvUbicacion.setText("Ubicación: No registrada");
            }

            // Condiciones especiales
            if (animal.getCondicionesEspeciales() != null && !animal.getCondicionesEspeciales().isEmpty()) {
                StringBuilder condiciones = new StringBuilder("Condiciones: ");
                for (int i = 0; i < animal.getCondicionesEspeciales().size(); i++) {
                    condiciones.append(animal.getCondicionesEspeciales().get(i));
                    if (i < animal.getCondicionesEspeciales().size() - 1) {
                        condiciones.append(", ");
                    }
                }
                tvCondiciones.setText(condiciones.toString());
            } else {
                tvCondiciones.setText("Condiciones: Ninguna registrada");
            }
        }

        return view;
    }
}