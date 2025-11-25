package com.refugio.pawrescue.ui.publico;

import android.content.Intent;
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
    private String animalId; // Guardamos el ID aquí
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animal_details_public);

        initViews();
        initFirebase();
        setupToolbar();

        // --- LÓGICA DE CARGA SEGURA ---
        animalId = getIntent().getStringExtra("ANIMAL_ID");

        if (animalId != null && !animalId.isEmpty()) {
            loadAnimalFromId(animalId); // Cargar datos de la nube
        } else {
            Toast.makeText(this, "Error: ID no recibido", Toast.LENGTH_SHORT).show();
            finish();
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

    private void loadAnimalFromId(String id) {
        // Toast.makeText(this, "Cargando datos...", Toast.LENGTH_SHORT).show();
        db.collection("animales").document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        animal = documentSnapshot.toObject(Animal.class);
                        if (animal != null) {
                            animal.setId(documentSnapshot.getId()); // Asegurar ID
                            displayAnimalData();
                            checkIfFavorite();
                        }
                    } else {
                        Toast.makeText(this, "El animal ya no existe", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show();
                });
    }

    private void displayAnimalData() {
        if (animal == null) return;

        tvAnimalName.setText(animal.getNombre());
        tvBreedAge.setText(animal.getRaza() + " • " + animal.getEdadTexto());
        chipGender.setText(animal.getSexo());
        chipSize.setText(animal.getTamano());
        tvLocation.setText(animal.getUbicacionRescate());
        tvRescueDate.setText("Fecha: " + animal.getFechaRescate());
        tvAboutContent.setText(animal.getDescripcion() + "\n\nPersonalidad:\n" + animal.getPersonalidad());

        if (animal.getFotosUrls() != null && !animal.getFotosUrls().isEmpty()) {
            ImagePagerAdapter imageAdapter = new ImagePagerAdapter(this, animal.getFotosUrls());
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
                Intent intent = new Intent(this, AdoptionFormActivity.class);
                intent.putExtra("ANIMAL_ID", animal.getId());
                intent.putExtra("ANIMAL_NAME", animal.getNombre());
                startActivity(intent);
            }
        });
    }

    private void checkIfFavorite() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid != null && animal != null) {
            db.collection("usuarios").document(uid).collection("favoritos").document(animal.getId()).get()
                    .addOnSuccessListener(doc -> {
                        isFavorite = doc.exists();
                        updateFavoriteIcon();
                    });
        }
    }

    private void toggleFavorite() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid != null && animal != null) {
            if (isFavorite) {
                db.collection("usuarios").document(uid).collection("favoritos").document(animal.getId()).delete();
                isFavorite = false;
                Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show();
            } else {
                db.collection("usuarios").document(uid).collection("favoritos").document(animal.getId()).set(animal);
                isFavorite = true;
                Toast.makeText(this, "Guardado", Toast.LENGTH_SHORT).show();
            }
            updateFavoriteIcon();
        } else {
            Toast.makeText(this, "Inicia sesión", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateFavoriteIcon() {
        fabFavorite.setImageResource(isFavorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
    }
}