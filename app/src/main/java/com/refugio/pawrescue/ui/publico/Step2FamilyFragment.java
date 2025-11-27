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

public class Step2FamilyFragment extends Fragment {

    private TextInputEditText etNumAdultos, etNumNinos;
    private RadioGroup rgAcuerdo, rgAlergias;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_step2_family, container, false);

        etNumAdultos = view.findViewById(R.id.etNumAdultos);
        etNumNinos = view.findViewById(R.id.etNumNinos);
        rgAcuerdo = view.findViewById(R.id.rgAcuerdo);
        rgAlergias = view.findViewById(R.id.rgAlergias);

        return view;
    }

    public boolean isValidStep() {
        String adultos = etNumAdultos.getText().toString().trim();
        String ninos = etNumNinos.getText().toString().trim();

        if (adultos.isEmpty()) {
            Toast.makeText(getContext(), "Ingresa el número de adultos.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (ninos.isEmpty()) {
            Toast.makeText(getContext(), "Ingresa el número de niños (pon 0 si no hay).", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (rgAcuerdo.getCheckedRadioButtonId() == -1) {
            Toast.makeText(getContext(), "¿Todos están de acuerdo con la adopción?", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (rgAlergias.getCheckedRadioButtonId() == -1) {
            Toast.makeText(getContext(), "Indica si alguien tiene alergias.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public Map<String, Object> getData() {
        Map<String, Object> data = new HashMap<>();
        data.put("numAdultos", etNumAdultos.getText().toString().trim());
        data.put("numNinos", etNumNinos.getText().toString().trim());

        int acuerdoId = rgAcuerdo.getCheckedRadioButtonId();
        data.put("familiaAcuerdo", (acuerdoId == R.id.rbSiAcuerdo) ? "Sí" : "No");

        int alergiasId = rgAlergias.getCheckedRadioButtonId();
        data.put("familiaAlergias", (alergiasId == R.id.rbSiAlergia) ? "Sí" : "No");

        return data;
    }
}