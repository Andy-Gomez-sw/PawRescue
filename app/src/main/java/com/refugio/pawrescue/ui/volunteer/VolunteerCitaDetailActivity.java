package com.refugio.pawrescue.ui.volunteer;

import android.content.Intent;
import android.os.Bundle;
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
    private TextView tvTelefono, tvDireccion, tvTipoVivienda, tvPropiedadVivienda, tvFechaNacimiento;
    private TextView tvFolio, tvFechaSolicitud;
    private TextView tvDatosPersonales, tvDatosFamilia, tvDatosExperiencia, tvDatosCompromiso;

    private Spinner spEstado;
    private MaterialButton btnGuardar, btnReporteVisita;

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

        tvTelefono = findViewById(R.id.tv_telefono);
        tvDireccion = findViewById(R.id.tv_direccion);
        tvTipoVivienda = findViewById(R.id.tv_tipo_vivienda);
        tvPropiedadVivienda = findViewById(R.id.tv_propiedad_vivienda);
        tvFechaNacimiento = findViewById(R.id.tv_fecha_nacimiento);

        tvFolio = findViewById(R.id.tv_folio);
        tvFechaSolicitud = findViewById(R.id.tv_fecha_solicitud);

        tvDatosPersonales = findViewById(R.id.tv_datos_personales);
        tvDatosFamilia = findViewById(R.id.tv_datos_familia);
        tvDatosExperiencia = findViewById(R.id.tv_datos_experiencia);
        tvDatosCompromiso = findViewById(R.id.tv_datos_compromiso);

        spEstado = findViewById(R.id.sp_estado);
        btnGuardar = findViewById(R.id.btn_guardar_estado);
        btnReporteVisita = findViewById(R.id.btn_reporte_visita);




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
        // Nombre y correo del adoptante
        tvAdoptante.setText(noVacio(cita.getNombreAdoptante()));
        tvCorreo.setText(noVacio(cita.getCorreoAdoptante()));

        // Fecha de la cita
        Date fechaCita = cita.getFechaCita();
        if (fechaCita != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
            tvFecha.setText(sdf.format(fechaCita));
        } else {
            tvFecha.setText("Sin fecha asignada");
        }

        // Datos básicos
        tvTelefono.setText(noVacio(cita.getTelefono()));
        tvDireccion.setText(noVacio(cita.getDireccion()));
        tvTipoVivienda.setText(noVacio(cita.getTipoVivienda()));
        tvPropiedadVivienda.setText(noVacio(cita.getPropiedadVivienda()));
        tvFechaNacimiento.setText(noVacio(cita.getFechaNacimiento()));

        // Folio y fecha de solicitud
        tvFolio.setText(noVacio(cita.getFolio()));

        Date fechaSolicitud = cita.getFechaSolicitud();
        if (fechaSolicitud != null) {
            java.text.SimpleDateFormat sdfSol = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            tvFechaSolicitud.setText(sdfSol.format(fechaSolicitud));
        } else {
            tvFechaSolicitud.setText("Sin fecha registrada");
        }

        // Mapas de los 4 pasos del formulario
        tvDatosPersonales.setText(formatearMapa(cita.getDatosPersonales()));
        tvDatosFamilia.setText(formatearMapa(cita.getDatosFamilia()));
        tvDatosExperiencia.setText(formatearMapa(cita.getDatosExperiencia()));
        tvDatosCompromiso.setText(formatearMapa(cita.getDatosCompromiso()));

        // Configurar botón de reporte
        final String citaId = cita.getCitaId();

        if (citaId != null && !citaId.isEmpty()) {
            btnReporteVisita.setEnabled(true);
            btnReporteVisita.setText("Llenar reporte de visita");
            btnReporteVisita.setOnClickListener(v -> abrirPantallaReporte(citaId));
        } else {
            btnReporteVisita.setEnabled(false);
            btnReporteVisita.setText("Sin cita vinculada");
            btnReporteVisita.setOnClickListener(null);
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


    private String formatearMapa(Map<String, Object> datos) {
        if (datos == null || datos.isEmpty()) {
            return "Sin información capturada.";
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : datos.entrySet()) {
            String clave = formatearClave(entry.getKey());
            String valor = entry.getValue() == null ? "-" : String.valueOf(entry.getValue());

            sb.append("• ")
                    .append(clave)
                    .append(": ")
                    .append(valor)
                    .append("\n");
        }
        return sb.toString().trim();
    }

    private String formatearClave(String key) {
        if (key == null) return "";
        String result = key.replace("_", " ");
        result = result.replaceAll("([a-z])([A-Z])", "$1 $2");
        return result;
    }

    private void abrirPantallaReporte(String citaId) {
        Intent intent = new Intent(this, VolunteerAppointmentActivity.class);
        intent.putExtra("CITA_ID", citaId);
        startActivity(intent);
    }


    private String noVacio(String s) {
        return (s == null || s.isEmpty()) ? "-" : s;
    }
}
