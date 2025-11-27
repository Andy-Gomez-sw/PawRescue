package com.refugio.pawrescue.ui.publico;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.Tasks;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.refugio.pawrescue.R;
import com.refugio.pawrescue.data.helper.FirebaseHelper;
import com.refugio.pawrescue.model.Cita;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AdoptionFormActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ImageButton btnBack;
    private MaterialButton btnPrevious, btnNext;
    private ProgressBar progressBar;
    private TextView tvStepTitle;
    private AdoptionFormPagerAdapter adapter;

    private String animalId;
    private String animalName;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adoption_form);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        animalId = getIntent().getStringExtra("ANIMAL_ID");
        animalName = getIntent().getStringExtra("ANIMAL_NAME");

        initViews();
        setupViewPager();
        setupButtons();
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        btnBack = findViewById(R.id.btnBack);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        progressBar = findViewById(R.id.progressBar);
        tvStepTitle = findViewById(R.id.tvTitle);
    }

    private void setupViewPager() {
        adapter = new AdoptionFormPagerAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(false);
        viewPager.setOffscreenPageLimit(5);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateUI(position);
            }
        });
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnNext.setOnClickListener(v -> {
            int currentItem = viewPager.getCurrentItem();
            boolean isValid = false;

            // 🟢 VALIDACIÓN POR PASO
            switch (currentItem) {
                case 0: // Paso 1: Datos y Archivos
                    Step1PersonalDataFragment step1 = adapter.getStep1();
                    if (step1 != null) isValid = step1.isValidStep();
                    break;
                case 1: // Paso 2: Familia
                    Step2FamilyFragment step2 = adapter.getStep2();
                    if (step2 != null) isValid = step2.isValidStep();
                    break;
                case 2: // Paso 3: Experiencia
                    Step3ExperienceFragment step3 = adapter.getStep3();
                    if (step3 != null) isValid = step3.isValidStep();
                    break;
                case 3: // Paso 4: Compromiso
                    Step4CommitmentFragment step4 = adapter.getStep4();
                    if (step4 != null) isValid = step4.isValidStep();
                    break;
                default:
                    isValid = true; // El paso 5 se valida en validarYEnviar()
                    break;
            }

            if (isValid) {
                if (currentItem < adapter.getItemCount() - 1) {
                    viewPager.setCurrentItem(currentItem + 1);
                } else {
                    // Estamos en el último paso, intentamos enviar
                    validarYEnviar();
                }
            }
        });

        btnPrevious.setOnClickListener(v -> {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem > 0) {
                viewPager.setCurrentItem(currentItem - 1);
            }
        });
    }

    private void updateUI(int position) {
        int progress = (int) (((float) (position + 1) / adapter.getItemCount()) * 100);
        if (progressBar != null) progressBar.setProgress(progress);

        if (position == 0) {
            btnPrevious.setVisibility(View.INVISIBLE);
        } else {
            btnPrevious.setVisibility(View.VISIBLE);
        }

        if (position == adapter.getItemCount() - 1) {
            btnNext.setText("Enviar Solicitud");
        } else {
            btnNext.setText("Siguiente");
        }

        if(tvStepTitle != null) {
            String titulo = "Paso " + (position + 1);
            if (animalName != null) titulo += " - " + animalName;
            tvStepTitle.setText(titulo);
        }
    }

    private void validarYEnviar() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar campos del último paso (Paso 5)
        Step5ReviewFragment step5 = adapter.getStep5();
        if (step5 != null && !step5.validateFields()) {
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        btnNext.setEnabled(false);
        btnNext.setText("Verificando...");

        // VERIFICAR DUPLICADOS
        db.collection("solicitudes_adopcion")
                .whereEqualTo("usuarioId", userId)
                .whereEqualTo("animalId", animalId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Toast.makeText(this, "⚠️ Ya tienes una solicitud activa para " + animalName, Toast.LENGTH_LONG).show();
                        btnNext.setEnabled(true);
                        btnNext.setText("Enviar Solicitud");
                    } else {
                        // NO EXISTE, procedemos a subir documentos
                        subirDocumentosYGuardar(userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error de conexión al verificar", Toast.LENGTH_SHORT).show();
                    btnNext.setEnabled(true);
                    btnNext.setText("Enviar Solicitud");
                });
    }

    private void subirDocumentosYGuardar(String userId) {
        Toast.makeText(this, "Subiendo documentos, espera un momento...", Toast.LENGTH_LONG).show();
        btnNext.setText("Subiendo Archivos...");

        Step1PersonalDataFragment step1 = adapter.getStep1();
        Map<String, android.net.Uri> archivos = step1.getFileUris();

        StorageReference storageRef = storage.getReference().child("documentos_adopcion").child(userId);

        // Tarea 1: INE Frente
        StorageReference refIneF = storageRef.child("ine_frente_" + UUID.randomUUID().toString() + ".jpg");
        var task1 = refIneF.putFile(archivos.get("ineFrente")).continueWithTask(task -> refIneF.getDownloadUrl());

        // Tarea 2: INE Reverso
        StorageReference refIneR = storageRef.child("ine_reverso_" + UUID.randomUUID().toString() + ".jpg");
        var task2 = refIneR.putFile(archivos.get("ineReverso")).continueWithTask(task -> refIneR.getDownloadUrl());

        // Tarea 3: Comprobante PDF
        StorageReference refPdf = storageRef.child("comprobante_" + UUID.randomUUID().toString() + ".pdf");
        var task3 = refPdf.putFile(archivos.get("comprobante")).continueWithTask(task -> refPdf.getDownloadUrl());

        Tasks.whenAllSuccess(task1, task2, task3).addOnSuccessListener(objects -> {
            String urlIneFrente = objects.get(0).toString();
            String urlIneReverso = objects.get(1).toString();
            String urlPdf = objects.get(2).toString();

            guardarSolicitudFinal(userId, urlIneFrente, urlIneReverso, urlPdf);

        }).addOnFailureListener(e -> {
            btnNext.setEnabled(true);
            btnNext.setText("Enviar Solicitud");
            Toast.makeText(this, "Error al subir documentos: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void guardarSolicitudFinal(String userId, String urlIneF, String urlIneR, String urlPdf) {
        Toast.makeText(this, "Guardando solicitud...", Toast.LENGTH_SHORT).show();

        String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd-HHmmss", java.util.Locale.getDefault()).format(new Date());
        String idPersonalizado = "FOL-" + timeStamp;

        Map<String, Object> solicitud = new HashMap<>();
        solicitud.put("id", idPersonalizado);
        solicitud.put("folio", idPersonalizado);
        solicitud.put("usuarioId", userId);
        solicitud.put("usuarioEmail", auth.getCurrentUser().getEmail());
        solicitud.put("animalId", animalId != null ? animalId : "SinID");
        solicitud.put("animalNombre", animalName != null ? animalName : "Desconocido");
        solicitud.put("fechaSolicitud", new Date());
        solicitud.put("estado", "Cita Agendada");

        // 🟢 RECOLECCIÓN DE DATOS DE TODOS LOS FRAGMENTS
        Step1PersonalDataFragment step1 = adapter.getStep1();
        if (step1 != null) solicitud.putAll(step1.getData());

        Step2FamilyFragment step2 = adapter.getStep2();
        if (step2 != null) solicitud.putAll(step2.getData());

        Step3ExperienceFragment step3 = adapter.getStep3();
        if (step3 != null) solicitud.putAll(step3.getData());

        Step4CommitmentFragment step4 = adapter.getStep4();
        if (step4 != null) solicitud.putAll(step4.getData());

        Step5ReviewFragment step5 = adapter.getStep5();
        Map<String, Object> data5 = (step5 != null) ? step5.getData() : new HashMap<>();

        // Agregar URLs y Fecha Cita
        solicitud.put("urlIneFrente", urlIneF);
        solicitud.put("urlIneReverso", urlIneR);
        solicitud.put("urlComprobante", urlPdf);
        solicitud.put("fechaCita", data5.get("fechaCita"));

        // Crear Cita
        String fechaCitaStr = (String) data5.get("fechaCitaString");
        String horaCitaStr = (String) data5.get("horaCitaString");

        Cita nuevaCita = new Cita();
        nuevaCita.setSolicitudId(idPersonalizado);
        nuevaCita.setAnimalId(animalId);
        nuevaCita.setAnimalNombre(animalName);
        nuevaCita.setUsuarioId(userId);
        nuevaCita.setUsuarioEmail(auth.getCurrentUser().getEmail());
        nuevaCita.setFecha(fechaCitaStr);
        nuevaCita.setHora(horaCitaStr);
        nuevaCita.setEstado("agendada");
        nuevaCita.setFechaCreacion(new Date());

        FirebaseHelper.getInstance().addCita(nuevaCita, new FirebaseHelper.CitaAddListener() {
            @Override
            public void onCitaAdded(String citaId) {
                solicitud.put("citaId", citaId);

                db.collection("solicitudes_adopcion")
                        .document(idPersonalizado)
                        .set(solicitud)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(AdoptionFormActivity.this, "¡Solicitud enviada! Cita agendada para el " + fechaCitaStr + " a las " + horaCitaStr, Toast.LENGTH_LONG).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            btnNext.setEnabled(true);
                            btnNext.setText("Enviar Solicitud");
                            Toast.makeText(AdoptionFormActivity.this, "Error al guardar solicitud: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }

            @Override
            public void onError(Exception e) {
                btnNext.setEnabled(true);
                btnNext.setText("Enviar Solicitud");
                Toast.makeText(AdoptionFormActivity.this, "Error al crear la cita: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}