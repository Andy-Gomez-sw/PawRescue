package com.refugio.pawrescue.ui.publico;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.refugio.pawrescue.R;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AdoptionFormActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ImageButton btnBack;
    private MaterialButton btnPrevious, btnNext;
    private ProgressBar progressBar;
    private TextView tvStepTitle;
    private AdoptionFormPagerAdapter adapter;

    private String animalId;
    private String animalName;
    private FirebaseFirestore db; // Base de datos
    private FirebaseAuth auth;    // Usuario

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adoption_form);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

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
        viewPager.setOffscreenPageLimit(5); // Importante: Mantiene los 5 pasos en memoria

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
            if (currentItem < adapter.getItemCount() - 1) {
                viewPager.setCurrentItem(currentItem + 1);
            } else {
                submitForm(); // Enviar a Firebase
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

    private void submitForm() {
        // 1. Verificar usuario
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Error: Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        btnNext.setEnabled(false); // Evitar doble clic
        Toast.makeText(this, "Enviando solicitud...", Toast.LENGTH_SHORT).show();

        // 2. Recolectar datos de TODOS los pasos
        Map<String, Object> solicitud = new HashMap<>();

        // Datos básicos
        solicitud.put("usuarioId", auth.getCurrentUser().getUid());
        solicitud.put("usuarioEmail", auth.getCurrentUser().getEmail());
        solicitud.put("animalId", animalId);
        solicitud.put("animalNombre", animalName);
        solicitud.put("fechaSolicitud", new Date());
        solicitud.put("estado", "pendiente"); // Importante para el Admin

        // Datos del Paso 1 (Personal)
        Map<String, Object> dataPaso1 = adapter.getStep1().getData();
        if (dataPaso1 != null) solicitud.putAll(dataPaso1);
        else {
            Toast.makeText(this, "Faltan datos en el Paso 1", Toast.LENGTH_SHORT).show();
            btnNext.setEnabled(true);
            return;
        }

        // Datos del Paso 2 (Familiar) - Asegúrate de implementar getData en Step2FamilyFragment
        Map<String, Object> dataPaso2 = adapter.getStep2().getData();
        if (dataPaso2 != null) solicitud.putAll(dataPaso2);

        // Datos del Paso 3 (Experiencia)
        Map<String, Object> dataPaso3 = adapter.getStep3().getData();
        if (dataPaso3 != null) solicitud.putAll(dataPaso3);

        // Datos del Paso 4 (Compromiso)
        Map<String, Object> dataPaso4 = adapter.getStep4().getData();
        if (dataPaso4 != null) solicitud.putAll(dataPaso4);

        // 3. Enviar a Firebase
        // Usamos la colección "solicitudes_adopcion" (Asegúrate que tu Admin lea de aquí)
        db.collection("solicitudes_adopcion")
                .add(solicitud)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "¡Solicitud enviada con éxito!", Toast.LENGTH_LONG).show();
                    finish(); // Cerrar pantalla
                })
                .addOnFailureListener(e -> {
                    btnNext.setEnabled(true);
                    Toast.makeText(this, "Error al enviar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}