package com.refugio.pawrescue.ui.publico;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioGroup;
import android.widget.Toast; // Agregado para feedback

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth; // Importante
import com.google.firebase.auth.FirebaseUser; // Importante
import com.google.firebase.firestore.FirebaseFirestore; // Importante
import com.refugio.pawrescue.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Step1PersonalDataFragment extends Fragment {

    private TextInputEditText etNombreCompleto;
    private TextInputEditText etFechaNacimiento;
    private TextInputEditText etTelefono;
    private TextInputEditText etEmail;
    private TextInputEditText etDireccion;
    private AutoCompleteTextView actvTipoVivienda;
    private RadioGroup rgPropiedadVivienda;
    private Calendar calendar = Calendar.getInstance();

    // Variables de Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_step1_personal_data, container, false);

        initViews(view);
        initFirebase(); // Inicializamos Firebase
        setupDatePicker();
        setupTipoViviendaDropdown();

        // 🟢 MAGIA: Cargar datos del usuario automáticamente
        loadUserData();

        return view;
    }

    private void initViews(View view) {
        etNombreCompleto = view.findViewById(R.id.etNombreCompleto);
        etFechaNacimiento = view.findViewById(R.id.etFechaNacimiento);
        etTelefono = view.findViewById(R.id.etTelefono);
        etEmail = view.findViewById(R.id.etEmail);
        etDireccion = view.findViewById(R.id.etDireccion);
        actvTipoVivienda = view.findViewById(R.id.actvTipoVivienda);
        rgPropiedadVivienda = view.findViewById(R.id.rgPropiedadVivienda);
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Método para autocompletar los campos con la info del usuario logueado
     */
    private void loadUserData() {
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            // 1. Llenar Email (Siempre lo tenemos del Auth)
            etEmail.setText(user.getEmail());
            // Opcional: Bloquear el campo email para que no lo cambien
            // etEmail.setEnabled(false);

            // 2. Buscar el resto de datos en Firestore (Colección "usuarios")
            String userId = user.getUid();

            db.collection("usuarios").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // NOTA: Asegúrate que los nombres de los campos coincidan con tu BD

                            String nombre = documentSnapshot.getString("nombre"); // o "nombreCompleto"
                            if (nombre != null) etNombreCompleto.setText(nombre);

                            String telefono = documentSnapshot.getString("telefono");
                            if (telefono != null) etTelefono.setText(telefono);

                            String direccion = documentSnapshot.getString("direccion");
                            if (direccion != null) etDireccion.setText(direccion);

                            // Si tienes fecha de nacimiento guardada, también puedes cargarla
                            // String fechaNac = documentSnapshot.getString("fechaNacimiento");
                            // if (fechaNac != null) etFechaNacimiento.setText(fechaNac);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Si falla, no pasa nada, el usuario puede escribir manualmente
                        // Toast.makeText(getContext(), "No se pudieron cargar tus datos", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void setupDatePicker() {
        etFechaNacimiento.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateField();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
    }

    private void updateDateField() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etFechaNacimiento.setText(sdf.format(calendar.getTime()));
    }

    private void setupTipoViviendaDropdown() {
        String[] tiposVivienda = {"Casa", "Departamento", "Otro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                tiposVivienda
        );
        actvTipoVivienda.setAdapter(adapter);
    }

    public Map<String, Object> getData() {
        Map<String, Object> data = new HashMap<>();
        String nombre = etNombreCompleto.getText().toString().trim();
        String fecha = etFechaNacimiento.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();
        String tipoVivienda = actvTipoVivienda.getText().toString().trim();

        if (nombre.isEmpty() || fecha.isEmpty() || telefono.isEmpty() ||
                email.isEmpty() || direccion.isEmpty() || tipoVivienda.isEmpty()) {
            return null; // Retorna null si faltan datos obligatorios
        }

        data.put("nombreCompleto", nombre);
        data.put("fechaNacimiento", fecha);
        data.put("telefono", telefono);
        data.put("email", email);
        data.put("direccion", direccion);
        data.put("tipoVivienda", tipoVivienda);

        int propiedadId = rgPropiedadVivienda.getCheckedRadioButtonId();
        if (propiedadId == R.id.rbPropietario) {
            data.put("propiedadVivienda", "Propietario");
        } else {
            data.put("propiedadVivienda", "Renta");
        }
        return data;
    }
}