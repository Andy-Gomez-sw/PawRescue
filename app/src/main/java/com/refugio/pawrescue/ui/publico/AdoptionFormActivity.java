package com.refugio.pawrescue.ui.publico;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import com.refugio.pawrescue.R;


public class AdoptionFormActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvProgressText;
    private TextView tvProgressPercent;
    private LinearProgressIndicator progressBar;
    private ViewPager2 viewPagerForm;
    private MaterialButton btnPrevious;
    private MaterialButton btnNext;

    private AdoptionFormPagerAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String animalId;
    private String animalName;
    private int currentStep = 0;
    private final int TOTAL_STEPS = 5;

    // Variables para almacenar datos del formulario
    private Map<String, Object> formData = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adoption_form);

        initViews();
        initFirebase();
        getIntentData();
        setupToolbar();
        setupViewPager();
        setupButtons();
        updateProgress();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvProgressText = findViewById(R.id.tvProgressText);
        tvProgressPercent = findViewById(R.id.tvProgressPercent);
        progressBar = findViewById(R.id.progressBar);
        viewPagerForm = findViewById(R.id.viewPagerForm);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void getIntentData() {
        animalId = getIntent().getStringExtra("ANIMAL_ID");
        animalName = getIntent().getStringExtra("ANIMAL_NAME");
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupViewPager() {
        adapter = new AdoptionFormPagerAdapter(this);
        viewPagerForm.setAdapter(adapter);
        viewPagerForm.setUserInputEnabled(false); // Deshabilitar swipe

        viewPagerForm.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentStep = position;
                updateProgress();
                updateButtons();
                updateStepCircles();
            }
        });
    }

    private void setupButtons() {
        btnPrevious.setOnClickListener(v -> {
            if (currentStep > 0) {
                viewPagerForm.setCurrentItem(currentStep - 1);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (validateCurrentStep()) {
                if (currentStep < TOTAL_STEPS - 1) {
                    viewPagerForm.setCurrentItem(currentStep + 1);
                } else {
                    submitAdoptionRequest();
                }
            }
        });
    }

    private void updateProgress() {
        int progress = ((currentStep + 1) * 100) / TOTAL_STEPS;
        progressBar.setProgress(progress);
        tvProgressText.setText("Paso " + (currentStep + 1) + " de " + TOTAL_STEPS);
        tvProgressPercent.setText(progress + "%");
    }

    private void updateButtons() {
        btnPrevious.setEnabled(currentStep > 0);

        if (currentStep == TOTAL_STEPS - 1) {
            btnNext.setText("Enviar Solicitud");
        } else {
            btnNext.setText("Siguiente");
        }
    }

    private void updateStepCircles() {
        // Actualizar los círculos de paso en la UI
        // Implementar según el diseño de los step circles
    }

    private boolean validateCurrentStep() {
        // Obtener el fragment actual y validar sus datos
        switch (currentStep) {
            case 0: // Datos Personales
                return validateStep1();
            case 1: // Situación Familiar
                return validateStep2();
            case 2: // Experiencia
                return validateStep3();
            case 3: // Compromiso
                return validateStep4();
            case 4: // Revisión
                return true; // Solo revisión, no requiere validación
            default:
                return false;
        }
    }

    private boolean validateStep1() {
        // Validar datos personales
        Step1PersonalDataFragment fragment = (Step1PersonalDataFragment)
                getSupportFragmentManager().findFragmentByTag("f" + currentStep);

        if (fragment != null) {
            Map<String, Object> stepData = fragment.getData();
            if (stepData == null || stepData.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos obligatorios",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
            formData.putAll(stepData);
            return true;
        }
        return false;
    }

    private boolean validateStep2() {
        // Validar situación familiar
        // Similar a validateStep1
        return true;
    }

    private boolean validateStep3() {
        // Validar experiencia con mascotas
        return true;
    }

    private boolean validateStep4() {
        // Validar compromiso
        return true;
    }

    private void submitAdoptionRequest() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (userId == null) {
            Toast.makeText(this, "Debes iniciar sesión para continuar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generar folio único
        String folio = generateFolio();

        // Preparar datos de la solicitud
        Map<String, Object> solicitud = new HashMap<>();
        solicitud.put("folio", folio);
        solicitud.put("animalId", animalId);
        solicitud.put("animalNombre", animalName);
        solicitud.put("usuarioId", userId);
        solicitud.put("datosPersonales", formData);
        solicitud.put("estado", "pendiente");
        solicitud.put("fechaSolicitud", new Date());
        solicitud.put("estadoActual", "Solicitud Recibida");

        // Guardar en Firestore
        db.collection("solicitudes_adopcion")
                .add(solicitud)
                .addOnSuccessListener(documentReference -> {
                    String solicitudId = documentReference.getId();

                    // Mostrar diálogo de éxito
                    showSuccessDialog(folio, solicitudId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al enviar solicitud: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private String generateFolio() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String date = sdf.format(new Date());
        int random = (int) (Math.random() * 9999);
        return "RF-" + date + "-" + String.format("%04d", random);
    }

    private void showSuccessDialog(String folio, String solicitudId) {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("¡Solicitud Enviada!")
                .setMessage("Tu solicitud ha sido enviada exitosamente.\n\n" +
                        "Folio de seguimiento:\n" + folio + "\n\n" +
                        "Recibirás una respuesta pronto.")
                .setPositiveButton("Ver mi solicitud", (dialog, which) -> {
                    Intent intent = new Intent(AdoptionFormActivity.this,
                            RequestDetailActivity.class);
                    intent.putExtra("SOLICITUD_ID", solicitudId);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Volver al inicio", (dialog, which) -> {
                    Intent intent = new Intent(AdoptionFormActivity.this,
                            GalleryActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    public void saveStepData(int step, Map<String, Object> data) {
        formData.putAll(data);
    }
}