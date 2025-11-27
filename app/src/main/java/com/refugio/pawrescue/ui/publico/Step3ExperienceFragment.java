package com.refugio.pawrescue.ui.publico;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.refugio.pawrescue.R;

import java.util.HashMap;
import java.util.Map;

public class Step3ExperienceFragment extends Fragment {

    private RadioGroup rgMascotasActuales, rgMascotasPasadas;
    private TextInputEditText etDetalleMascotas;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_step3_experience, container, false);

        rgMascotasActuales = view.findViewById(R.id.rgMascotasActuales);
        rgMascotasPasadas = view.findViewById(R.id.rgMascotasPasadas);
        etDetalleMascotas = view.findViewById(R.id.etDetalleMascotas);

        return view;
    }

    public boolean isValidStep() {
        if (rgMascotasActuales.getCheckedRadioButtonId() == -1) {
            Toast.makeText(getContext(), "Indica si tienes mascotas actualmente.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Si dijo que SÍ tiene mascotas, obligamos a llenar el detalle
        if (rgMascotasActuales.getCheckedRadioButtonId() == R.id.rbSiMascotas) {
            String detalle = etDetalleMascotas.getText().toString().trim();
            if (detalle.isEmpty()) {
                Toast.makeText(getContext(), "Por favor describe tus mascotas actuales.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (rgMascotasPasadas.getCheckedRadioButtonId() == -1) {
            Toast.makeText(getContext(), "Indica si has tenido mascotas antes.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public Map<String, Object> getData() {
        Map<String, Object> data = new HashMap<>();

        int actualesId = rgMascotasActuales.getCheckedRadioButtonId();
        boolean tieneMascotas = (actualesId == R.id.rbSiMascotas);
        data.put("tieneMascotasActuales", tieneMascotas ? "Sí" : "No");

        String detalle = etDetalleMascotas.getText().toString().trim();
        data.put("detalleMascotas", tieneMascotas ? detalle : "N/A");

        int pasadasId = rgMascotasPasadas.getCheckedRadioButtonId();
        data.put("tuvoMascotasAntes", (pasadasId == R.id.rbSiAntes) ? "Sí" : "No");

        return data;
    }
}