package com.refugio.pawrescue.ui.publico;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.Animal;
import com.refugio.pawrescue.ui.adapter.ImagePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class AnimalDetailsPublicActivity extends AppCompatActivity {

    private ViewPager2 viewPagerImages;
    private TabLayout tabDots;
    private Toolbar toolbar;
    private TextView tvAnimalName, tvBreedAge, tvLocation, tvRescueDate, tvAboutContent;
    private Chip chipGender, chipSize;
    private LinearLayout headerAbout, headerHealth, healthContent, headerRequirements, requirementsContent;
    private ImageView iconAbout, iconHealth, iconRequirements;
    private FloatingActionButton fabFavorite;
    private MaterialButton btnSolicitar;

    private FirebaseFirestore db;
    private Animal animal;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animal_details_public);

        initViews();
        initFirebase();
        setupToolbar();

        // Recibir datos
        if (getIntent().hasExtra("ANIMAL_OBJETO")) {
            animal = (Animal) getIntent().getSerializableExtra("ANIMAL_OBJETO");
        }

        if (animal != null) {
            displayAnimalData();
            checkIfFavorite();
        } else {
            // Cargar por ID si no viene el objeto completo
            String animalId = getIntent().getStringExtra("ANIMAL_ID");
            if (animalId != null) {
                loadAnimalFromId(animalId);
            } else {
                Toast.makeText(this, "Error cargando datos", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        setupExpandableSection();
        setupButtons();
    }

    private void initViews() {
        viewPagerImages = findViewById(R.id.viewPagerImages);
        tabDots = findViewById(R.id.tabDots);
        toolbar = findViewById(R.id.toolbar);
        tvAnimalName = findViewById(R.id.tvAnimalName);
        tvBreedAge = findViewById(R.id.tvBreedAge);
        chipGender = findViewById(R.id.chipGender);
        chipSize = findViewById(R.id.chipSize);
        tvLocation = findViewById(R.id.tvLocation);
        tvRescueDate = findViewById(R.id.tvRescueDate);

        headerAbout = findViewById(R.id.headerAbout);
        iconAbout = findViewById(R.id.iconAbout);
        tvAboutContent = findViewById(R.id.tvAboutContent);

        headerHealth = findViewById(R.id.headerHealth);
        iconHealth = findViewById(R.id.iconHealth);
        healthContent = findViewById(R.id.healthContent);

        headerRequirements = findViewById(R.id.headerRequirements);
        iconRequirements = findViewById(R.id.iconRequirements);
        requirementsContent = findViewById(R.id.requirementsContent);

        fabFavorite = findViewById(R.id.fabFavorite);
        btnSolicitar = findViewById(R.id.btnSolicitar);
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupExpandableSection() {
        View.OnClickListener toggleListener = v -> {
            View content = null;
            ImageView icon = null;

            if (v == headerAbout) { content = tvAboutContent; icon = iconAbout; }
            else if (v == headerHealth) { content = healthContent; icon = iconHealth; }
            else if (v == headerRequirements) { content = requirementsContent; icon = iconRequirements; }

            if (content != null) {
                boolean isVisible = content.getVisibility() == View.VISIBLE;
                content.setVisibility(isVisible ? View.GONE : View.VISIBLE);
                if (icon != null) icon.setRotation(isVisible ? 0 : 180);
            }
        };

        headerAbout.setOnClickListener(toggleListener);
        headerHealth.setOnClickListener(toggleListener);
        headerRequirements.setOnClickListener(toggleListener);
    }

    private void setupButtons() {
        fabFavorite.setOnClickListener(v -> toggleFavorite());

        btnSolicitar.setOnClickListener(v -> {
            if (btnSolicitar.isEnabled() && animal != null && animal.getId() != null) {
                Intent intent = new Intent(AnimalDetailsPublicActivity.this, AdoptionFormActivity.class);
                intent.putExtra("ANIMAL_ID", animal.getId());
                intent.putExtra("ANIMAL_NAME", animal.getNombre());
                startActivity(intent);
            }
        });
    }

    private void loadAnimalFromId(String id) {
        db.collection("animales").document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        animal = documentSnapshot.toObject(Animal.class);
                        if (animal != null) {
                            animal.setId(documentSnapshot.getId());
                            displayAnimalData();
                            checkIfFavorite();
                        }
                    }
                });
    }

    private void displayAnimalData() {
        if (animal == null) return;

        // --- 1. DATOS BÁSICOS ---
        tvAnimalName.setText(animal.getNombre());
        tvBreedAge.setText(animal.getRaza() + " • " + animal.getEdadTexto());
        tvLocation.setText(animal.getUbicacionRescate());
        tvRescueDate.setText("Ingreso: " + animal.getFechaRescate());

        // 🟢 CORRECCIÓN CHIPS (Cuadritos) 🟢
        // Validar Sexo
        if (animal.getSexo() != null && !animal.getSexo().isEmpty()) {
            chipGender.setText(animal.getSexo());
            chipGender.setVisibility(View.VISIBLE);
        } else {
            chipGender.setVisibility(View.GONE);
        }

        // Validar Tamaño (Aquí es donde salía el cuadro vacío)
        if (animal.getTamano() != null && !animal.getTamano().isEmpty()) {
            chipSize.setText(animal.getTamano());
            chipSize.setVisibility(View.VISIBLE);
        } else {
            chipSize.setVisibility(View.GONE); // Se oculta si no hay dato
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(animal.getNombre());
        }

        // --- 2. SECCIÓN: SOBRE EL ANIMAL ---
        String descripcion = (animal.getDescripcion() != null && !animal.getDescripcion().isEmpty())
                ? animal.getDescripcion()
                : "Sin descripción disponible.";

        String personalidad = (animal.getPersonalidad() != null && !animal.getPersonalidad().isEmpty())
                ? "\n\nPersonalidad:\n" + animal.getPersonalidad()
                : "";

        tvAboutContent.setText(descripcion + personalidad);

        // --- 3. SECCIÓN: HISTORIAL MÉDICO ---
        healthContent.removeAllViews();

        if (animal.getEstadoSalud() != null && !animal.getEstadoSalud().isEmpty()) {
            addDynamicInfo(healthContent, "Estado General: " + animal.getEstadoSalud());
        }

        if (animal.getCondicionesEspeciales() != null && !animal.getCondicionesEspeciales().isEmpty()) {
            for (String condicion : animal.getCondicionesEspeciales()) {
                addDynamicInfo(healthContent, "✅ " + condicion);
            }
        }

        if (healthContent.getChildCount() == 0) {
            addDynamicInfo(healthContent, "No hay información médica registrada.");
        }

        // --- 4. LÓGICA DE IMÁGENES ---
        List<String> listaImagenes = new ArrayList<>();
        if (animal.getFotosUrls() != null && !animal.getFotosUrls().isEmpty()) {
            listaImagenes.addAll(animal.getFotosUrls());
        }
        if (listaImagenes.isEmpty()) {
            if (animal.getFotoUrl() != null && !animal.getFotoUrl().isEmpty()) {
                listaImagenes.add(animal.getFotoUrl());
            }
        }

        if (!listaImagenes.isEmpty()) {
            ImagePagerAdapter imageAdapter = new ImagePagerAdapter(this, listaImagenes);
            viewPagerImages.setAdapter(imageAdapter);
            new TabLayoutMediator(tabDots, viewPagerImages, (tab, position) -> {}).attach();
        } else {
            viewPagerImages.setBackgroundResource(R.drawable.ic_pet_placeholder);
        }

        // --- 5. DISPONIBILIDAD ---
        String estado = animal.getEstadoRefugio();
        boolean disponible = estado != null &&
                (estado.trim().equalsIgnoreCase("Disponible Adopcion") ||
                        estado.trim().equalsIgnoreCase("Disponible"));

        if (disponible) {
            btnSolicitar.setEnabled(true);
            btnSolicitar.setText("Solicitar Adopción");
            btnSolicitar.setBackgroundColor(getResources().getColor(R.color.primary_orange));
            btnSolicitar.setTextColor(Color.WHITE);
        } else {
            btnSolicitar.setEnabled(false);
            String motivo = estado != null ? estado : "No disponible";
            btnSolicitar.setText(motivo.toUpperCase());
            btnSolicitar.setBackgroundColor(Color.parseColor("#BDBDBD"));
            btnSolicitar.setTextColor(Color.WHITE);
        }
    }

    private void addDynamicInfo(LinearLayout container, String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(14);
        textView.setPadding(0, 0, 0, 8);
        container.addView(textView);
    }

    private void checkIfFavorite() {
        String userId = getCurrentUserId();
        if (userId != null && animal != null && animal.getId() != null) {
            db.collection("usuarios").document(userId)
                    .collection("favoritos")
                    .document(animal.getId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        isFavorite = documentSnapshot.exists();
                        updateFavoriteIcon();
                    });
        }
    }

    private void toggleFavorite() {
        String userId = getCurrentUserId();
        if (userId != null && animal != null && animal.getId() != null) {
            if (isFavorite) {
                db.collection("usuarios").document(userId).collection("favoritos").document(animal.getId()).delete();
                isFavorite = false;
                Toast.makeText(this, "Eliminado de favoritos", Toast.LENGTH_SHORT).show();
            } else {
                db.collection("usuarios").document(userId).collection("favoritos").document(animal.getId()).set(animal);
                isFavorite = true;
                Toast.makeText(this, "Agregado a favoritos", Toast.LENGTH_SHORT).show();
            }
            updateFavoriteIcon();
        } else {
            Toast.makeText(this, "Inicia sesión para favoritos", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateFavoriteIcon() {
        fabFavorite.setImageResource(isFavorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
    }

    private String getCurrentUserId() {
        return FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
    }
}