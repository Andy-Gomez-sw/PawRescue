package com.refugio.pawrescue.ui.admin;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.HistorialMedico;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Activity para registrar un nuevo evento médico (RF-09).
 */
public class RegistroEventoMedicoActivity extends AppCompatActivity {

    private static final String TAG = "RegistroEventoMedico";

    private String animalId;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // Componentes UI
    private Spinner spinnerTipoEvento;
    private TextInputEditText etDiagnostico, etTratamiento, etVeterinario, etNotas;
    private Button btnSeleccionarFecha, btnGuardar;
    private ProgressBar progressBar;

    private Date fechaSeleccionada;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_evento_medico);

        // Obtener ID del animal
        animalId = getIntent().getStringExtra("animalId");
        if (animalId == null) {
            Toast.makeText(this, "Error: ID de animal no proporcionado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Registrar Evento Médico");
        }

        // Enlazar vistas
        spinnerTipoEvento = findViewById(R.id.spinner_tipo_evento);
        etDiagnostico = findViewById(R.id.et_diagnostico);
        etTratamiento = findViewById(R.id.et_tratamiento);
        etVeterinario = findViewById(R.id.et_veterinario);
        etNotas = findViewById(R.id.et_notas);
        btnSeleccionarFecha = findViewById(R.id.btn_seleccionar_fecha);
        btnGuardar = findViewById(R.id.btn_guardar_evento);
        progressBar = findViewById(R.id.progress_bar_evento);

        // Configurar Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.tipos_evento_medico, android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoEvento.setAdapter(adapter);

        // Fecha por defecto: hoy
        fechaSeleccionada = new Date();
        btnSeleccionarFecha.setText("Fecha: " + dateFormat.format(fechaSeleccionada));

        // Listeners
        btnSeleccionarFecha.setOnClickListener(v -> mostrarSelectorFecha());
        btnGuardar.setOnClickListener(v -> intentarGuardarEvento());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void mostrarSelectorFecha() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fechaSeleccionada);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCal = Calendar.getInstance();
                    selectedCal.set(year, month, dayOfMonth);
                    fechaSeleccionada = selectedCal.getTime();
                    btnSeleccionarFecha.setText("Fecha: " + dateFormat.format(fechaSeleccionada));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void intentarGuardarEvento() {
        String diagnostico = etDiagnostico.getText().toString().trim();
        String tratamiento = etTratamiento.getText().toString().trim();
        String veterinario = etVeterinario.getText().toString().trim();
        String notas = etNotas.getText().toString().trim();
        String tipoEvento = spinnerTipoEvento.getSelectedItem().toString();

        // Validaciones
        if (diagnostico.isEmpty()) {
            Toast.makeText(this, "El diagnóstico es obligatorio", Toast.LENGTH_SHORT).show();
            etDiagnostico.requestFocus();
            return;
        }

        if (veterinario.isEmpty()) {
            Toast.makeText(this, "El nombre del veterinario es obligatorio", Toast.LENGTH_SHORT).show();
            etVeterinario.requestFocus();
            return;
        }

        // Mostrar progreso
        btnGuardar.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        // Crear objeto HistorialMedico
        HistorialMedico evento = new HistorialMedico();
        evento.setIdAnimal(animalId);
        evento.setTipoEvento(tipoEvento);
        evento.setDiagnostico(diagnostico);
        evento.setTratamiento(tratamiento);
        evento.setVeterinario(veterinario);
        evento.setNotas(notas);
        evento.setFecha(new Timestamp(fechaSeleccionada));

        // Guardar en Firestore
        db.collection("animales")
                .document(animalId)
                .collection("historialMedico")
                .add(evento)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "✅ Evento médico registrado exitosamente", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnGuardar.setEnabled(true);
                    Toast.makeText(this, "❌ Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}