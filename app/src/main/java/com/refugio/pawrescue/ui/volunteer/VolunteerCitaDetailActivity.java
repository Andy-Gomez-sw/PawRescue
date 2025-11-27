package com.refugio.pawrescue.ui.volunteer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
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

    // Cards para ver documentos
    private MaterialCardView cardIneFrente, cardIneReverso, cardComprobante;
    private String urlIneFrente, urlIneReverso, urlComprobante;

    private FirebaseFirestore db;

    private String idSolicitud;
    private String idAnimal;

    private SolicitudAdopcion cita;

    // Campos extra del documento (flat en Firestore)
    private String numAdultos;
    private String numNinos;
    private String familiaAcuerdo;
    private String familiaAlergias;
    private String tuvoMascotasAntes;
    private String tieneMascotasActuales;
    private String detalleMascotas;
    private String horasSolo;
    private String lugarDormir;
    private String planMudanza;

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

        // Cards documentos
        cardIneFrente = findViewById(R.id.cardIneFrente);
        cardIneReverso = findViewById(R.id.cardIneReverso);
        cardComprobante = findViewById(R.id.cardComprobante);

        // De inicio apagadas
        deshabilitarCard(cardIneFrente);
        deshabilitarCard(cardIneReverso);
        deshabilitarCard(cardComprobante);

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

                    // URLs de documentos desde la solicitud
                    urlIneFrente = doc.getString("urlIneFrente");
                    urlIneReverso = doc.getString("urlIneReverso");
                    urlComprobante = doc.getString("urlComprobante");

                    // Campos "sueltos" (no en los mapas)
                    numAdultos = doc.getString("numAdultos");
                    numNinos = doc.getString("numNinos");
                    familiaAcuerdo = doc.getString("familiaAcuerdo");
                    familiaAlergias = doc.getString("familiaAlergias");
                    tuvoMascotasAntes = doc.getString("tuvoMascotasAntes");
                    tieneMascotasActuales = doc.getString("tieneMascotasActuales");
                    detalleMascotas = doc.getString("detalleMascotas");
                    horasSolo = doc.getString("horasSolo");
                    lugarDormir = doc.getString("lugarDormir");
                    planMudanza = doc.getString("planMudanza");

                    // Configurar cards según haya o no URL
                    configurarCardDocumento(cardIneFrente, urlIneFrente);
                    configurarCardDocumento(cardIneReverso, urlIneReverso);
                    configurarCardDocumento(cardComprobante, urlComprobante);

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
            java.text.SimpleDateFormat sdf =
                    new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
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
            java.text.SimpleDateFormat sdfSol =
                    new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            tvFechaSolicitud.setText(sdfSol.format(fechaSolicitud));
        } else {
            tvFechaSolicitud.setText("Sin fecha registrada");
        }

        // Mapas de los 4 pasos del formulario (tal como ya los tenías)
        String datosPersonalesTxt = formatearMapa(cita.getDatosPersonales());
        String datosFamiliaTxt = formatearMapa(cita.getDatosFamilia());
        String datosExperienciaTxt = formatearMapa(cita.getDatosExperiencia());
        String datosCompromisoTxt = formatearMapa(cita.getDatosCompromiso());

        // Extras de familia
        StringBuilder extraFamilia = new StringBuilder();
        if (!esVacio(numAdultos)) {
            extraFamilia.append("• Número de adultos en casa: ").append(noVacio(numAdultos)).append("\n");
        }
        if (!esVacio(numNinos)) {
            extraFamilia.append("• Número de niños en casa: ").append(noVacio(numNinos)).append("\n");
        }
        if (!esVacio(familiaAcuerdo)) {
            extraFamilia.append("• Familia de acuerdo con la adopción: ").append(noVacio(familiaAcuerdo)).append("\n");
        }
        if (!esVacio(familiaAlergias)) {
            extraFamilia.append("• Hay alergias a animales en la familia: ").append(noVacio(familiaAlergias)).append("\n");
        }

        // Extras de experiencia con mascotas
        StringBuilder extraExperiencia = new StringBuilder();
        if (!esVacio(tuvoMascotasAntes)) {
            extraExperiencia.append("• Ha tenido mascotas antes: ").append(noVacio(tuvoMascotasAntes)).append("\n");
        }
        if (!esVacio(tieneMascotasActuales)) {
            extraExperiencia.append("• Tiene mascotas actualmente: ").append(noVacio(tieneMascotasActuales)).append("\n");
        }
        if (!esVacio(detalleMascotas)) {
            extraExperiencia.append("• Detalle de mascotas actuales/anteriores: ").append(noVacio(detalleMascotas)).append("\n");
        }

        // Extras de compromiso/hogar
        StringBuilder extraCompromiso = new StringBuilder();
        if (!esVacio(horasSolo)) {
            extraCompromiso.append("• Horas que el animal estaría solo: ").append(noVacio(horasSolo)).append("\n");
        }
        if (!esVacio(lugarDormir)) {
            extraCompromiso.append("• Lugar donde dormirá el animal: ").append(noVacio(lugarDormir)).append("\n");
        }
        if (!esVacio(planMudanza)) {
            extraCompromiso.append("• Plan en caso de mudanza: ").append(noVacio(planMudanza)).append("\n");
        }

        // Asignar textos combinando mapa + extras
        tvDatosPersonales.setText(datosPersonalesTxt);

        tvDatosFamilia.setText(
                (datosFamiliaTxt.isEmpty() ? "" : datosFamiliaTxt + "\n") +
                        extraFamilia.toString().trim()
        );

        tvDatosExperiencia.setText(
                (datosExperienciaTxt.isEmpty() ? "" : datosExperienciaTxt + "\n") +
                        extraExperiencia.toString().trim()
        );

        tvDatosCompromiso.setText(
                (datosCompromisoTxt.isEmpty() ? "" : datosCompromisoTxt + "\n") +
                        extraCompromiso.toString().trim()
        );

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
            return "";
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

    private boolean esVacio(String s) {
        return s == null || s.trim().isEmpty();
    }

    // ------------ Helpers para documentos ------------

    private void deshabilitarCard(MaterialCardView card) {
        card.setEnabled(false);
        card.setAlpha(0.3f);
        card.setOnClickListener(null);
    }

    private void configurarCardDocumento(MaterialCardView card, String url) {
        if (url == null || url.isEmpty()) {
            deshabilitarCard(card);
            return;
        }

        card.setEnabled(true);
        card.setAlpha(1f);
        card.setOnClickListener(v -> abrirUrl(url));
    }

    private void abrirUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir el documento", Toast.LENGTH_SHORT).show();
        }
    }
}
