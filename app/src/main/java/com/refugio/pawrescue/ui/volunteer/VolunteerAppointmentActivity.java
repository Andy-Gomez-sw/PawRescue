package com.refugio.pawrescue.ui.volunteer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.Cita;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Actividad para que el VOLUNTARIO:
 * 1. Vea los detalles de la cita asignada
 * 2. Reciba documentos del usuario (INE, comprobante)
 * 3. Evalúe al usuario (checklist)
 * 4. Suba fotos y comentarios
 * 5. Envíe reporte completo al Admin
 */
public class VolunteerAppointmentActivity extends AppCompatActivity {

    private TextView tvAnimalName, tvUsuarioEmail, tvFechaHora;
    private CheckBox cbIneRecibida, cbComprobanteRecibido, cbDomicilioAdecuado,
            cbActitudPositiva, cbExperienciaPrevia, cbRecomiendaAprobacion;
    private EditText etObservaciones, etRecomendaciones;
    private LinearLayout llDocumentosContainer;
    private MaterialButton btnSubirDocumento, btnEnviarReporte;

    private ImageButton btnBack;   // <- flecha del toolbar

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;

    private String citaId;
    private Cita cita;
    private List<Uri> documentosUris = new ArrayList<>();
    private List<String> documentosUrls = new ArrayList<>();

    private ActivityResultLauncher<Intent> documentLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_appointment);

        initViews();
        initFirebase();
        getIntentData();
        setupDocumentLauncher();
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

        // --------- TOOLBAR Y FLECHA DE REGRESO ---------
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            // El título visual lo maneja el TextView del layout, por eso lo dejamos vacío aquí
            getSupportActionBar().setTitle("");
            // Activa la flecha de navegación del toolbar

        }

        // Flecha que está dentro del propio layout (ImageButton)
        btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
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
                        }
                    } else {
                        Toast.makeText(this, "Cita no encontrada", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void displayCitaData() {
        tvAnimalName.setText("Cita para adoptar a " + cita.getAnimalNombre());
        tvUsuarioEmail.setText("Usuario: " + cita.getUsuarioEmail());
        tvFechaHora.setText("Fecha: " + cita.getFechaHoraCompleta());
    }

    private void setupDocumentLauncher() {
        documentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedFile = result.getData().getData();
                        if (selectedFile != null) {
                            documentosUris.add(selectedFile);
                            updateDocumentosPreview();
                        }
                    }
                }
        );
    }

    private void setupButtons() {
        btnSubirDocumento.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            documentLauncher.launch(intent);
        });

        btnEnviarReporte.setOnClickListener(v -> {
            if (validarFormulario()) {
                enviarReporte();
            }
        });
    }

    private void updateDocumentosPreview() {
        llDocumentosContainer.removeAllViews();
        for (Uri uri : documentosUris) {
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(200, 200));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageURI(uri);
            llDocumentosContainer.addView(imageView);
        }
        Toast.makeText(this, "Documentos: " + documentosUris.size(), Toast.LENGTH_SHORT).show();
    }

    private boolean validarFormulario() {
        if (!cbIneRecibida.isChecked() || !cbComprobanteRecibido.isChecked()) {
            Toast.makeText(this, "⚠️ Debes recibir INE y Comprobante de domicilio", Toast.LENGTH_LONG).show();
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

        // Primero subir documentos si hay
        if (!documentosUris.isEmpty()) {
            uploadDocumentos();
        } else {
            saveReporte();
        }
    }

    private void uploadDocumentos() {
        final int[] uploadedCount = {0};

        for (Uri uri : documentosUris) {
            String fileName = "reporte_" + citaId + "_" + System.currentTimeMillis() + ".jpg";
            StorageReference ref = storage.getReference()
                    .child("reportes_citas")
                    .child(citaId)
                    .child(fileName);

            ref.putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> {
                        ref.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            documentosUrls.add(downloadUri.toString());
                            uploadedCount[0]++;

                            if (uploadedCount[0] == documentosUris.size()) {
                                saveReporte();
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al subir documento", Toast.LENGTH_SHORT).show();
                        btnEnviarReporte.setEnabled(true);
                        btnEnviarReporte.setText("Enviar Reporte");
                    });
        }
    }

    private void saveReporte() {
        Map<String, Object> reporte = new HashMap<>();
        reporte.put("citaId", citaId);
        reporte.put("solicitudId", cita.getSolicitudId());
        reporte.put("animalId", cita.getAnimalId());
        reporte.put("usuarioId", cita.getUsuarioId());
        reporte.put("voluntarioId", auth.getCurrentUser().getUid());
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

        // Documentos
        if (!documentosUrls.isEmpty()) {
            reporte.put("documentos", documentosUrls);
        }

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
                                finish(); // ← Al terminar, vuelve a Detalle de Cita
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
