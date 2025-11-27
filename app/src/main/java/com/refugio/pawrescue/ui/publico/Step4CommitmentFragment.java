package com.refugio.pawrescue.ui.publico;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.refugio.pawrescue.R;

import java.util.HashMap;
import java.util.Map;

public class Step4CommitmentFragment extends Fragment {

    private TextInputEditText etLugarDormir, etHorasSolo, etMudanza;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_step4_commitment, container, false);

        etLugarDormir = view.findViewById(R.id.etLugarDormir);
        etHorasSolo = view.findViewById(R.id.etHorasSolo);
        etMudanza = view.findViewById(R.id.etMudanza);

        return view;
    }

    public boolean isValidStep() {
        String lugar = etLugarDormir.getText().toString().trim();
        String horas = etHorasSolo.getText().toString().trim();
        String mudanza = etMudanza.getText().toString().trim();

        if (lugar.isEmpty()) {
            Toast.makeText(getContext(), "¿Dónde dormirá la mascota?", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (horas.isEmpty()) {
            Toast.makeText(getContext(), "Indica cuántas horas pasará solo.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (mudanza.isEmpty()) {
            Toast.makeText(getContext(), "Describe el plan en caso de mudanza.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public Map<String, Object> getData() {
        Map<String, Object> data = new HashMap<>();
        data.put("lugarDormir", etLugarDormir.getText().toString().trim());
        data.put("horasSolo", etHorasSolo.getText().toString().trim());
        data.put("planMudanza", etMudanza.getText().toString().trim());
        return data;
    }
}