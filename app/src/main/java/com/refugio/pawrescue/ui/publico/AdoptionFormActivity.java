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
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adoption_form);

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
            if (currentItem < adapter.getItemCount() - 1) {
                viewPager.setCurrentItem(currentItem + 1);
            } else {
                // Intentar enviar
                validarYEnviar();
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

    /**
     * Método principal que inicia el proceso de validación y envío
     */
    private void validarYEnviar() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        // Bloqueamos el botón para evitar doble clic
        btnNext.setEnabled(false);
        btnNext.setText("Verificando...");

        // 🔴 PASO 1: VERIFICAR DUPLICADOS
        // Buscamos si YA existe una solicitud de este usuario para este animal
        db.collection("solicitudes_adopcion")
                .whereEqualTo("usuarioId", userId)
                .whereEqualTo("animalId", animalId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // ¡YA EXISTE! Mostramos error y no guardamos
                        Toast.makeText(this, "⚠️ Ya tienes una solicitud activa para " + animalName, Toast.LENGTH_LONG).show();

                        // Rehabilitamos el botón por si quieren corregir algo (aunque aquí no aplicaría mucho)
                        btnNext.setEnabled(true);
                        btnNext.setText("Enviar Solicitud");
                    } else {
                        // NO EXISTE, procedemos a guardar
                        guardarSolicitudEnFirebase(userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error de conexión al verificar", Toast.LENGTH_SHORT).show();
                    btnNext.setEnabled(true);
                    btnNext.setText("Enviar Solicitud");
                });
    }

    /**
     * Método privado que realmente guarda los datos (solo se llama si no hay duplicados)
     */
    private void guardarSolicitudEnFirebase(String userId) {
        Toast.makeText(this, "Enviando solicitud...", Toast.LENGTH_SHORT).show();

        // Generar Folio único con fecha
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
        solicitud.put("estado", "pendiente");

        // Recolectar datos de los fragments
        // Importante: Asegúrate que tus Fragments tengan el método getData implementado
        // Si alguno devuelve null, podríamos detenernos aquí, pero por ahora asumimos que validaste campos obligatorios
        Map<String, Object> data1 = adapter.getStep1().getData();
        if (data1 != null) solicitud.putAll(data1);

        // ... (Repetir para los otros pasos si tienen datos) ...
        // Map<String, Object> data2 = adapter.getStep2().getData();
        // if (data2 != null) solicitud.putAll(data2);

        db.collection("solicitudes_adopcion")
                .document(idPersonalizado)
                .set(solicitud)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "¡Solicitud enviada con éxito!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnNext.setEnabled(true);
                    btnNext.setText("Enviar Solicitud");
                    Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}