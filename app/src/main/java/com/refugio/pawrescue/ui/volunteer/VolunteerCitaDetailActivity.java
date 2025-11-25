package com.refugio.pawrescue.ui.volunteer;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.SolicitudAdopcion;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class VolunteerCitaDetailActivity extends AppCompatActivity {

    private TextView tvAdoptante, tvCorreo, tvFecha;
    private Spinner spEstado;
    private MaterialButton btnGuardar;

    private FirebaseFirestore db;

    private String idSolicitud;
    private String idAnimal;

    private SolicitudAdopcion cita;

    private final SimpleDateFormat dateTimeFormat =
            new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("es", "MX"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_cita_detail);

        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Detalle de cita");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        tvAdoptante = findViewById(R.id.tv_adoptante);
        tvCorreo = findViewById(R.id.tv_correo);
        tvFecha = findViewById(R.id.tv_fecha);
        spEstado = findViewById(R.id.sp_estado);
        btnGuardar = findViewById(R.id.btn_guardar_estado);

        // Spinner
        ArrayAdapter<String> estadoAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Ninguno", "Listo para adoptar"}
        );
        estadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spEstado.setAdapter(estadoAdapter);

        // Recuperar IDs
        idSolicitud = getIntent().getStringExtra("idSolicitud");
        idAnimal = getIntent().getStringExtra("idAnimal");

        if (idSolicitud == null || idAnimal == null) {
            Toast.makeText(this, "Datos insuficientes.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        cargarCita();

        btnGuardar.setOnClickListener(v -> guardarCambioEstado());
    }

    private void cargarCita() {
        db.collection("solicitudes_adopcion")
                .document(idSolicitud)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "La cita no existe.", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }

                    cita = doc.toObject(SolicitudAdopcion.class);
                    if (cita == null) {
                        Toast.makeText(this, "Error al cargar la cita.", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }

                    mostrarDatos();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void mostrarDatos() {
        tvAdoptante.setText(noVacio(cita.getNombreAdoptante()));
        tvCorreo.setText(noVacio(cita.getCorreoAdoptante()));

// 🟢 CORRECCIÓN: Usamos Date directamente porque el modelo ya lo devuelve así
        Date fecha = cita.getFechaCita();

        if (fecha != null) {
            // Formateamos la fecha directamente
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
            tvFecha.setText(sdf.format(fecha)); // Asegúrate que tvFecha sea tu TextView de la fecha
        } else {
            tvFecha.setText("Sin fecha asignada");
        }
    }

    private void guardarCambioEstado() {
        String valor = (String) spEstado.getSelectedItem();

        if ("Ninguno".equals(valor)) {
            Toast.makeText(this, "Sin cambios.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("estadoRefugio", "Listo para adoptar");
        data.put("fechaUltimaActualizacion", new Timestamp(new Date()));

        db.collection("animales").document(idAnimal)
                .update(data)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Estado actualizado.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private String noVacio(String s) {
        return (s == null || s.isEmpty()) ? "-" : s;
    }
}
