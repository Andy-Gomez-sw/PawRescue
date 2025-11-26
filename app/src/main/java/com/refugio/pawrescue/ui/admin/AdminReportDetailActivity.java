package com.refugio.pawrescue.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.refugio.pawrescue.R;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Actividad para que el ADMIN vea el reporte completo del voluntario
 * y decida APROBAR o RECHAZAR la adopción
 */
public class AdminReportDetailActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvAnimalName, tvUsuarioEmail, tvVoluntarioNombre, tvFechaCita;
    private TextView tvObservaciones, tvRecomendaciones;
    private CheckBox cbIne, cbComprobante, cbDomicilio, cbActitud, cbExperiencia, cbRecomendacion;
    private LinearLayout llFotosContainer;
    private MaterialButton btnAprobar, btnRechazar;

    private FirebaseFirestore db;
    private String citaId;
    private String solicitudId;
    private String reporteId;
    private Map<String, Object> reporteData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_report_detail);

        db = FirebaseFirestore.getInstance();

        initViews();
        getIntentData();
        setupButtons();
        loadReporteData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvAnimalName = findViewById(R.id.tvAnimalName);
        tvUsuarioEmail = findViewById(R.id.tvUsuarioEmail);
        tvVoluntarioNombre = findViewById(R.id.tvVoluntarioNombre);
        tvFechaCita = findViewById(R.id.tvFechaCita);
        tvObservaciones = findViewById(R.id.tvObservaciones);
        tvRecomendaciones = findViewById(R.id.tvRecomendaciones);
        cbIne = findViewById(R.id.cbIne);
        cbComprobante = findViewById(R.id.cbComprobante);
        cbDomicilio = findViewById(R.id.cbDomicilio);
        cbActitud = findViewById(R.id.cbActitud);
        cbExperiencia = findViewById(R.id.cbExperiencia);
        cbRecomendacion = findViewById(R.id.cbRecomendacion);
        llFotosContainer = findViewById(R.id.llFotosContainer);
        btnAprobar = findViewById(R.id.btnAprobar);
        btnRechazar = findViewById(R.id.btnRechazar);
    }

    private void getIntentData() {
        citaId = getIntent().getStringExtra("CITA_ID");
        reporteId = getIntent().getStringExtra("REPORTE_ID");

        if (citaId == null || reporteId == null) {
            Toast.makeText(this, "Error: Datos incompletos", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> onBackPressed());
        btnAprobar.setOnClickListener(v -> showConfirmacionDialog(true));
        btnRechazar.setOnClickListener(v -> showConfirmacionDialog(false));
    }

    private void loadReporteData() {
        // Cargar datos del reporte
        db.collection("reportes_citas")
                .document(reporteId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        reporteData = documentSnapshot.getData();
                        solicitudId = documentSnapshot.getString("solicitudId");
                        displayReporteData();
                        loadCitaData();
                    } else {
                        Toast.makeText(this, "Reporte no encontrado", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void loadCitaData() {
        db.collection("citas")
                .document(citaId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String animalNombre = documentSnapshot.getString("animalNombre");
                        String usuarioEmail = documentSnapshot.getString("usuarioEmail");
                        String voluntarioNombre = documentSnapshot.getString("voluntarioNombre");
                        String fecha = documentSnapshot.getString("fecha");
                        String hora = documentSnapshot.getString("hora");

                        tvAnimalName.setText("Reporte de Adopción: " + animalNombre);
                        tvUsuarioEmail.setText("Usuario: " + usuarioEmail);
                        tvVoluntarioNombre.setText("Evaluado por: " + voluntarioNombre);
                        tvFechaCita.setText("Fecha de cita: " + fecha + " a las " + hora);
                    }
                });
    }

    private void displayReporteData() {
        if (reporteData == null) return;

        // Checkboxes de documentos
        cbIne.setChecked(getBooleanFromData("ineRecibida"));
        cbComprobante.setChecked(getBooleanFromData("comprobanteRecibido"));
        cbDomicilio.setChecked(getBooleanFromData("domicilioAdecuado"));
        cbActitud.setChecked(getBooleanFromData("actitudPositiva"));
        cbExperiencia.setChecked(getBooleanFromData("experienciaPrevia"));
        cbRecomendacion.setChecked(getBooleanFromData("recomiendaAprobacion"));

        // Deshabilitar checkboxes (solo lectura)
        cbIne.setEnabled(false);
        cbComprobante.setEnabled(false);
        cbDomicilio.setEnabled(false);
        cbActitud.setEnabled(false);
        cbExperiencia.setEnabled(false);
        cbRecomendacion.setEnabled(false);

        // Observaciones
        String observaciones = (String) reporteData.get("observaciones");
        tvObservaciones.setText(observaciones != null ? observaciones : "Sin observaciones");

        String recomendaciones = (String) reporteData.get("recomendaciones");
        tvRecomendaciones.setText(recomendaciones != null ? recomendaciones : "Sin recomendaciones");

        // Cargar fotos si hay
        if (reporteData.containsKey("documentos")) {
            List<String> fotos = (List<String>) reporteData.get("documentos");
            if (fotos != null && !fotos.isEmpty()) {
                for (String url : fotos) {
                    ImageView imageView = new ImageView(this);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(300, 300);
                    params.setMargins(8, 8, 8, 8);
                    imageView.setLayoutParams(params);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    Glide.with(this)
                            .load(url)
                            .placeholder(R.drawable.ic_document)
                            .into(imageView);

                    llFotosContainer.addView(imageView);

                    // Click para ver en grande
                    imageView.setOnClickListener(v -> {
                        // TODO: Abrir imagen en pantalla completa
                        Toast.makeText(this, "Ver imagen completa", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }
    }

    private boolean getBooleanFromData(String key) {
        if (reporteData != null && reporteData.containsKey(key)) {
            Object value = reporteData.get(key);
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
        }
        return false;
    }

    /**
     * Muestra diálogo de confirmación antes de aprobar/rechazar
     */
    private void showConfirmacionDialog(boolean aprobar) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_decision_adopcion, null);
        TextInputEditText etMotivo = dialogView.findViewById(R.id.etMotivo);
        TextView tvTitulo = dialogView.findViewById(R.id.tvTitulo);
        MaterialButton btnConfirmar = dialogView.findViewById(R.id.btnConfirmar);
        MaterialButton btnCancelar = dialogView.findViewById(R.id.btnCancelar);

        if (aprobar) {
            tvTitulo.setText("✅ Aprobar Adopción");
            etMotivo.setHint("Comentarios adicionales (opcional)");
            btnConfirmar.setText("Aprobar");
        } else {
            tvTitulo.setText("❌ Rechazar Adopción");
            etMotivo.setHint("Motivo del rechazo (requerido)");
            btnConfirmar.setText("Rechazar");
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnConfirmar.setOnClickListener(v -> {
            String motivo = etMotivo.getText().toString().trim();

            if (!aprobar && motivo.isEmpty()) {
                Toast.makeText(this, "⚠️ Debes indicar el motivo del rechazo", Toast.LENGTH_SHORT).show();
                return;
            }

            procesarDecision(aprobar, motivo);
            dialog.dismiss();
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * Procesa la decisión del admin y actualiza Firestore
     */
    private void procesarDecision(boolean aprobar, String motivo) {
        btnAprobar.setEnabled(false);
        btnRechazar.setEnabled(false);

        String nuevoEstado = aprobar ? "aprobada" : "rechazada";

        // 1. Actualizar la solicitud de adopción
        Map<String, Object> solicitudUpdates = new HashMap<>();
        solicitudUpdates.put("estado", nuevoEstado);
        solicitudUpdates.put("decisionAdmin", aprobar ? "aprobada" : "rechazada");
        solicitudUpdates.put("motivoDecision", motivo);
        solicitudUpdates.put("fechaDecision", com.google.firebase.Timestamp.now());

        db.collection("solicitudes_adopcion")
                .document(solicitudId)
                .update(solicitudUpdates)
                .addOnSuccessListener(aVoid -> {
                    // 2. Actualizar el animal si fue aprobado
                    if (aprobar && reporteData.containsKey("animalId")) {
                        String animalId = (String) reporteData.get("animalId");
                        actualizarEstadoAnimal(animalId);
                    }

                    Toast.makeText(this,
                            aprobar ? "✅ Adopción APROBADA" : "❌ Adopción RECHAZADA",
                            Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnAprobar.setEnabled(true);
                    btnRechazar.setEnabled(true);
                });
    }

    /**
     * Actualiza el estado del animal a "Adoptado"
     */
    private void actualizarEstadoAnimal(String animalId) {
        Map<String, Object> animalUpdates = new HashMap<>();
        animalUpdates.put("estadoRefugio", "Adoptado");
        animalUpdates.put("fechaAdopcion", com.google.firebase.Timestamp.now());

        db.collection("animales")
                .document(animalId)
                .update(animalUpdates)
                .addOnSuccessListener(aVoid -> {
                    // Animal actualizado exitosamente
                })
                .addOnFailureListener(e -> {
                    // No crítico, solo log
                    android.util.Log.e("AdminReport", "Error actualizando animal: " + e.getMessage());
                });
    }
}