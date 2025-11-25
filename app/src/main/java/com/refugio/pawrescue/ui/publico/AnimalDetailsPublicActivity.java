package com.refugio.pawrescue.ui.publico;

import android.content.Intent;
import android.graphics.Color; // Importante para cambiar colores
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

// Imports
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
            // Plan B: Cargar por ID
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
            // Solo permite clic si está habilitado (aunque visualmente ya lo bloqueamos abajo)
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

        tvAnimalName.setText(animal.getNombre());
        tvBreedAge.setText(animal.getRaza() + " • " + animal.getEdadTexto());
        chipGender.setText(animal.getSexo());
        chipSize.setText(animal.getTamano());
        tvLocation.setText(animal.getUbicacionRescate());
        tvRescueDate.setText("Ingreso: " + animal.getFechaRescate());
        tvAboutContent.setText(animal.getDescripcion() + "\n\nPersonalidad:\n" + animal.getPersonalidad());

        if (animal.getFotosUrls() != null && !animal.getFotosUrls().isEmpty()) {
            ImagePagerAdapter imageAdapter = new ImagePagerAdapter(this, animal.getFotosUrls());
            viewPagerImages.setAdapter(imageAdapter);
            new TabLayoutMediator(tabDots, viewPagerImages, (tab, position) -> {}).attach();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(animal.getNombre());
        }

        // 🔴 LÓGICA DE DISPONIBILIDAD (AQUÍ ESTÁ EL CAMBIO)
        String estado = animal.getEstadoRefugio(); // Ej: "Disponible Adopcion", "En Tratamiento"

        // Normalizamos el texto (quitamos espacios extra y mayúsculas)
        boolean disponible = estado != null &&
                (estado.trim().equalsIgnoreCase("Disponible Adopcion") ||
                        estado.trim().equalsIgnoreCase("Disponible"));

        if (disponible) {
            // CASO 1: Disponible
            btnSolicitar.setEnabled(true);
            btnSolicitar.setText("Solicitar Adopción");
            // Restauramos el color original (o el que tengas en tu tema)
            // btnSolicitar.setBackgroundColor(ContextCompat.getColor(this, R.color.primary));
        } else {
            // CASO 2: NO Disponible (Adoptado, Enfermo, etc.)
            btnSolicitar.setEnabled(false); // Deshabilita el clic

            // Mostramos por qué no está disponible
            String motivo = estado != null ? estado : "No disponible";
            btnSolicitar.setText(motivo.toUpperCase());

            // Cambiamos color a Gris para indicar deshabilitado
            btnSolicitar.setBackgroundColor(Color.parseColor("#BDBDBD")); // Gris
            btnSolicitar.setTextColor(Color.WHITE);
        }
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