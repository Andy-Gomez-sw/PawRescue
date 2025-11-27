package com.refugio.pawrescue.ui.volunteer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.Cita;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Actividad para que el VOLUNTARIO:
 * 1. Vea los detalles de la cita asignada
 * 2. Revise documentos de la SOLICITUD (INE frente, reverso, comprobante)
 * 3. Evalúe al usuario (checklist)
 * 4. Escriba observaciones y recomendaciones
 * 5. Envíe reporte completo al Admin
 */
public class VolunteerMyAppointmentsActivity extends AppCompatActivity {

    private TextView tvAnimalName, tvUsuarioEmail, tvFechaHora;
    private CheckBox cbIneRecibida, cbComprobanteRecibido, cbDomicilioAdecuado,
            cbActitudPositiva, cbExperienciaPrevia, cbRecomiendaAprobacion;
    private EditText etObservaciones, etRecomendaciones;
    private LinearLayout llDocumentosContainer;
    private MaterialButton btnSubirDocumento, btnEnviarReporte; // btnSubir se ocultará

    private ImageButton btnBack;   // flecha del toolbar

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String citaId;
    private Cita cita;

    // URLs de documentos de la solicitud
    private String urlIneFrente;
    private String urlIneReverso;
    private String urlComprobante;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 👇 Asegúrate de que el XML se llame activity_volunteer_my_appointment.xml
        setContentView(R.layout.activity_volunteer_my_appointments);

        initViews();
        initFirebase();
        getIntentData();
        setupButtons();
    }

    private void initViews() {
        tvAnimalName = findViewById(R.id.tvAnimalName);
        tvUsuarioEmail = findViewById(R.id.tvUsuarioEmail);
        tvFechaHora = findViewById(R.id.tvFechaHora);

        cbIneRecibida = findViewById(R.id.cbIneRecibida);
        cbComprobanteRecibido = findViewById(R.id.cbComprobanteRecibido);
        cbDomicilioAdecuado = findViewById(R.id.cbDomicilioAdecuado);
        cbActitudPositiva = findViewById(R.id.cbActitudPositiva);
        cbExperienciaPrevia = findViewById(R.id.cbExperienciaPrevia);
        cbRecomiendaAprobacion = findViewById(R.id.cbRecomiendaAprobacion);

        etObservaciones = findViewById(R.id.etObservaciones);
        etRecomendaciones = findViewById(R.id.etRecomendaciones);

        llDocumentosContainer = findViewById(R.id.llDocumentosContainer);
        btnSubirDocumento = findViewById(R.id.btnSubirDocumento);
        btnEnviarReporte = findViewById(R.id.btnEnviarReporte);

        // ---- TOOLBAR Y FLECHA DE REGRESO ----
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            // El título visual lo maneja el TextView del layout
            getSupportActionBar().setTitle("");
        }

        btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Ya no se suben documentos aquí, sólo se consultan → ocultar botón de subir
        if (btnSubirDocumento != null) {
            btnSubirDocumento.setVisibility(View.GONE);
        }
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void getIntentData() {
        citaId = getIntent().getStringExtra("CITA_ID");
        if (citaId == null) {
            Toast.makeText(this, "Error: Cita no encontrada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadCitaData();
    }

    private void loadCitaData() {
        db.collection("citas").document(citaId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        cita = documentSnapshot.toObject(Cita.class);
                        if (cita != null) {
                            cita.setId(documentSnapshot.getId());
                            displayCitaData();
                            // Después de cargar la cita, cargar los documentos de la solicitud
                            loadSolicitudDocs(cita.getSolicitudId());
                        } else {
                            Toast.makeText(this, "Error al leer datos de la cita", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Cita no encontrada", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayCitaData() {
        tvAnimalName.setText("Cita para adoptar a " + cita.getAnimalNombre());
        tvUsuarioEmail.setText("Usuario: " + cita.getUsuarioEmail());
        tvFechaHora.setText("Fecha: " + cita.getFechaHoraCompleta());
    }

    /**
     * Carga las URLs de INE frente, reverso y comprobante
     * desde la colección solicitudes_adopcion.
     */
    private void loadSolicitudDocs(String solicitudId) {
        if (solicitudId == null || solicitudId.isEmpty()) {
            Toast.makeText(this, "Sin solicitud vinculada a la cita", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("solicitudes_adopcion")
                .document(solicitudId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Solicitud de adopción no encontrada", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    urlIneFrente = doc.getString("urlIneFrente");
                    urlIneReverso = doc.getString("urlIneReverso");
                    urlComprobante = doc.getString("urlComprobante");

                    setupDocumentoButtons();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar documentos: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Muestra en llDocumentosContainer tres filas con switches:
     * - Ver INE frente
     * - Ver INE reverso
     * - Ver comprobante de domicilio
     */
    private void setupDocumentoButtons() {
        if (llDocumentosContainer == null) return;

        llDocumentosContainer.removeAllViews();

        // INE frente y reverso marcan la misma casilla: cbIneRecibida
        addDocRow("Ver INE - Frente", urlIneFrente, cbIneRecibida);
        addDocRow("Ver INE - Reverso", urlIneReverso, cbIneRecibida);

        // Comprobante marca cbComprobanteRecibido
        addDocRow("Ver comprobante de domicilio", urlComprobante, cbComprobanteRecibido);
    }

    private void addDocRow(String label, String url, CheckBox checkToMark) {
        // Creamos un switch de Material
        SwitchMaterial sw = new SwitchMaterial(this);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        params.setMargins(0, 8, 0, 0);
        sw.setLayoutParams(params);

        sw.setText(label);
        sw.setTextSize(14f);
        sw.setTextColor(getResources().getColor(R.color.text_primary));
        sw.setPadding(8, 8, 8, 8);
        sw.setChecked(false);

        sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                // Si lo apagan, no hacemos nada extra
                return;
            }

            if (url == null || url.isEmpty()) {
                Toast.makeText(this, "No hay documento disponible para este campo", Toast.LENGTH_SHORT).show();
                // Regresarlo a apagado si no hay URL
                buttonView.setChecked(false);
                return;
            }

            // Abrir el documento
            openUrl(url);

            // Marcar el checkbox correspondiente como revisado
            if (checkToMark != null && !checkToMark.isChecked()) {
                checkToMark.setChecked(true);
            }
        });

        llDocumentosContainer.addView(sw);
    }

    private void openUrl(String url) {
        if (url == null || url.isEmpty()) {
            Toast.makeText(this, "No hay documento disponible para este campo", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void setupButtons() {
        // btnSubirDocumento ya no se usa (oculto en initViews)

        btnEnviarReporte.setOnClickListener(v -> {
            if (validarFormulario()) {
                enviarReporte();
            }
        });
    }

    private boolean validarFormulario() {
        if (!cbIneRecibida.isChecked() || !cbComprobanteRecibido.isChecked()) {
            Toast.makeText(this, "⚠️ Debes marcar que revisaste INE y comprobante", Toast.LENGTH_LONG).show();
            return false;
        }

        if (etObservaciones.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "⚠️ Escribe tus observaciones", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void enviarReporte() {
        btnEnviarReporte.setEnabled(false);
        btnEnviarReporte.setText("Enviando...");

        // Ya no se suben documentos aquí, sólo se guarda el reporte
        saveReporte();
    }

    private void saveReporte() {
        Map<String, Object> reporte = new HashMap<>();
        reporte.put("citaId", citaId);
        reporte.put("solicitudId", cita.getSolicitudId());
        reporte.put("animalId", cita.getAnimalId());
        reporte.put("usuarioId", cita.getUsuarioId());
        reporte.put("voluntarioId", auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null);
        reporte.put("fechaReporte", new Date());

        // Checklist
        reporte.put("ineRecibida", cbIneRecibida.isChecked());
        reporte.put("comprobanteRecibido", cbComprobanteRecibido.isChecked());
        reporte.put("domicilioAdecuado", cbDomicilioAdecuado.isChecked());
        reporte.put("actitudPositiva", cbActitudPositiva.isChecked());
        reporte.put("experienciaPrevia", cbExperienciaPrevia.isChecked());
        reporte.put("recomiendaAprobacion", cbRecomiendaAprobacion.isChecked());

        // Comentarios
        reporte.put("observaciones", etObservaciones.getText().toString().trim());
        reporte.put("recomendaciones", etRecomendaciones.getText().toString().trim());

        // Guardar en Firestore
        db.collection("reportes_citas")
                .add(reporte)
                .addOnSuccessListener(documentReference -> {
                    String reporteId = documentReference.getId();

                    // Actualizar la cita
                    Map<String, Object> citaUpdates = new HashMap<>();
                    citaUpdates.put("estado", "completada");
                    citaUpdates.put("reporteCompleto", true);
                    citaUpdates.put("reporteId", reporteId);

                    db.collection("citas").document(citaId)
                            .update(citaUpdates)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "✅ Reporte enviado con éxito", Toast.LENGTH_LONG).show();
                                finish(); // volver al detalle de cita
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error al actualizar la cita: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                btnEnviarReporte.setEnabled(true);
                                btnEnviarReporte.setText("Enviar Reporte");
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar reporte: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnEnviarReporte.setEnabled(true);
                    btnEnviarReporte.setText("Enviar Reporte");
                });
    }

    // Maneja la flecha de navegación del Toolbar (la flecha “nativa”)
    @Override
    public boolean onSupportNavigateUp() {
        finish();  // vuelve a Detalle de Cita
        return true;
    }
}
