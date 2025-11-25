package com.refugio.pawrescue.ui.publico;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import java.util.Collections;

public class AnimalDetailsPublicActivity extends AppCompatActivity {

    private static final String TAG = "AnimalDetailsPublic";

    private ViewPager2 viewPagerImages;
    private TabLayout tabDots;
    private Toolbar toolbar;
    private TextView tvAnimalName, tvBreedAge, tvLocation, tvRescueDate, tvAboutContent;
    private Chip chipGender, chipSize;
    private LinearLayout headerAbout, headerHealth, healthContent, headerRequirements, requirementsContent;
    private ImageView iconAbout, iconHealth, iconRequirements;
    private FloatingActionButton fabFavorite;
    private MaterialButton btnSolicitar;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private Animal animal;
    private String animalId;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animal_details_public);

        initViews();
        initFirebase();
        setupToolbar();

        animalId = getIntent().getStringExtra("ANIMAL_ID");
        Log.d(TAG, "📥 ID recibido: " + animalId);

        if (animalId == null || animalId.trim().isEmpty()) {
            Log.e(TAG, "❌ Error: ID no válido");
            Toast.makeText(this, "Error: No se recibió el ID del animal", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setupExpandableSection();
        setupButtons();
        loadAnimalFromId(animalId);
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
        progressBar = findViewById(R.id.progressBar);
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    private void loadAnimalFromId(String id) {
        Log.d(TAG, "🔍 Buscando animal: " + id);
        showLoading(true);

        db.collection("animales")
                .document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    showLoading(false);

                    if (!documentSnapshot.exists()) {
                        Log.e(TAG, "❌ Documento no existe");
                        Toast.makeText(this, "El animal no existe", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }

                    try {
                        animal = documentSnapshot.toObject(Animal.class);
                        if (animal == null) {
                            Log.e(TAG, "❌ Error al convertir documento");
                            finish();
                            return;
                        }

                        animal.setId(documentSnapshot.getId());
                        animal.setIdAnimal(documentSnapshot.getId());
                        Log.d(TAG, "✅ Animal cargado: " + animal.getNombre());

                        displayAnimalData();
                        checkIfFavorite();

                    } catch (Exception e) {
                        Log.e(TAG, "❌ Excepción", e);
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "❌ Error de conexión", e);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void displayAnimalData() {
        if (animal == null) return;

        tvAnimalName.setText(animal.getNombre() != null ? animal.getNombre() : "Sin nombre");

        String raza = animal.getRaza() != null ? animal.getRaza() : "Raza desconocida";
        String edad = animal.getEdadTexto();
        tvBreedAge.setText(raza + " • " + edad);

        chipGender.setText(animal.getSexo() != null ? animal.getSexo() : "Desconocido");

        String tamano = animal.getTamano();
        if (tamano == null || tamano.isEmpty()) {
            tamano = "Perro".equalsIgnoreCase(animal.getEspecie()) ? "Mediano" : "Pequeño";
        }
        chipSize.setText(tamano);

        tvLocation.setText(animal.getUbicacionRescate() != null ? animal.getUbicacionRescate() : "No disponible");
        tvRescueDate.setText("Fecha: " + animal.getFechaRescate());

        String descripcion = animal.getDescripcion();
        String personalidad = animal.getPersonalidad();

        StringBuilder aboutText = new StringBuilder();
        if (descripcion != null && !descripcion.isEmpty()) {
            aboutText.append(descripcion);
        } else {
            aboutText.append("Este adorable animal busca un hogar lleno de amor.");
        }

        if (personalidad != null && !personalidad.isEmpty()) {
            aboutText.append("\n\nPersonalidad:\n").append(personalidad);
        }

        tvAboutContent.setText(aboutText.toString());

        if (animal.getFotosUrls() != null && !animal.getFotosUrls().isEmpty()) {
            ImagePagerAdapter imageAdapter = new ImagePagerAdapter(this, animal.getFotosUrls());
            viewPagerImages.setAdapter(imageAdapter);
            new TabLayoutMediator(tabDots, viewPagerImages, (tab, position) -> {}).attach();
        } else if (animal.getFotoUrl() != null && !animal.getFotoUrl().isEmpty()) {
            ImagePagerAdapter imageAdapter = new ImagePagerAdapter(this, Collections.singletonList(animal.getFotoUrl()));
            viewPagerImages.setAdapter(imageAdapter);
            new TabLayoutMediator(tabDots, viewPagerImages, (tab, position) -> {}).attach();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(animal.getNombre());
        }
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
        View.OnClickListener toggle = v -> {
            View content = null;
            ImageView icon = null;
            if (v == headerAbout) { content = tvAboutContent; icon = iconAbout; }
            else if (v == headerHealth) { content = healthContent; icon = iconHealth; }
            else if (v == headerRequirements) { content = requirementsContent; icon = iconRequirements; }

            if (content != null) {
                boolean show = content.getVisibility() == View.GONE;
                content.setVisibility(show ? View.VISIBLE : View.GONE);
                icon.setRotation(show ? 180 : 0);
            }
        };
        headerAbout.setOnClickListener(toggle);
        headerHealth.setOnClickListener(toggle);
        headerRequirements.setOnClickListener(toggle);
    }

    private void setupButtons() {
        fabFavorite.setOnClickListener(v -> toggleFavorite());

        btnSolicitar.setOnClickListener(v -> {
            if (animal != null && animal.getId() != null) {
                Log.d(TAG, "🚀 Iniciando solicitud para: " + animal.getNombre());
                Intent intent = new Intent(this, AdoptionFormActivity.class);
                intent.putExtra("ANIMAL_ID", animal.getId());
                intent.putExtra("ANIMAL_NAME", animal.getNombre());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Error: Datos no disponibles", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfFavorite() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (uid != null && animal != null) {
            db.collection("usuarios")
                    .document(uid)
                    .collection("favoritos")
                    .document(animal.getId())
                    .get()
                    .addOnSuccessListener(doc -> {
                        isFavorite = doc.exists();
                        updateFavoriteIcon();
                    });
        }
    }

    private void toggleFavorite() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (uid != null && animal != null) {
            if (isFavorite) {
                db.collection("usuarios").document(uid).collection("favoritos").document(animal.getId()).delete();
                isFavorite = false;
                Toast.makeText(this, "Eliminado de favoritos", Toast.LENGTH_SHORT).show();
            } else {
                db.collection("usuarios").document(uid).collection("favoritos").document(animal.getId()).set(animal);
                isFavorite = true;
                Toast.makeText(this, "Guardado en favoritos", Toast.LENGTH_SHORT).show();
            }
            updateFavoriteIcon();
        } else {
            Toast.makeText(this, "Inicia sesión para guardar favoritos", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateFavoriteIcon() {
        fabFavorite.setImageResource(isFavorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}