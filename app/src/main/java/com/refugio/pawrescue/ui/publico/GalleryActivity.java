package com.refugio.pawrescue.ui.publico;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.Animal;
import com.refugio.pawrescue.ui.adapter.AnimalAdapter;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private static final String TAG = "GalleryActivity";

    private RecyclerView recyclerViewAnimals;
    private AnimalAdapter animalAdapter;
    private List<Animal> animalList;
    private List<Animal> filteredList;
    private EditText etSearch;
    private ImageButton btnProfile;
    private ChipGroup chipGroupEspecie;
    private ChipGroup chipGroupEdad;
    private ChipGroup chipGroupTamano;
    private BottomNavigationView bottomNavigation;
    private FirebaseFirestore db;

    // Filtros actuales
    private String currentFilterEspecie = "Todos";
    private String currentFilterEdad = "Todas";
    private String currentFilterTamano = "Todos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        initViews();
        initFirebase();
        setupRecyclerView();
        setupSearch();
        setupFilters();
        setupBottomNavigation();
        loadAnimals();
    }

    private void initViews() {
        recyclerViewAnimals = findViewById(R.id.recyclerViewAnimals);
        etSearch = findViewById(R.id.etSearch);
        btnProfile = findViewById(R.id.btnProfile);
        chipGroupEspecie = findViewById(R.id.chipGroupEspecie);
        chipGroupEdad = findViewById(R.id.chipGroupEdad);
        chipGroupTamano = findViewById(R.id.chipGroupTamano);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        animalList = new ArrayList<>();
        filteredList = new ArrayList<>();
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    private void setupRecyclerView() {
        animalAdapter = new AnimalAdapter(this, filteredList, new AnimalAdapter.OnAnimalClickListener() {
            @Override
            public void onAnimalClick(Animal animal) {
                if (animal == null || animal.getId() == null || animal.getId().trim().isEmpty()) {
                    Toast.makeText(GalleryActivity.this, "Error: Animal sin ID válido", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "❌ Animal sin ID. Nombre: " + (animal != null ? animal.getNombre() : "null"));
                    return;
                }

                Log.d(TAG, "✅ Abriendo detalle. ID: " + animal.getId() + ", Nombre: " + animal.getNombre());

                Intent intent = new Intent(GalleryActivity.this, AnimalDetailsPublicActivity.class);
                intent.putExtra("ANIMAL_ID", animal.getId());
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(Animal animal) {
                Toast.makeText(GalleryActivity.this, "Favorito: " + animal.getNombre(), Toast.LENGTH_SHORT).show();
            }
        });

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerViewAnimals.setLayoutManager(layoutManager);
        recyclerViewAnimals.setAdapter(animalAdapter);
    }

    private void loadAnimals() {
        Log.d(TAG, "🔍 Iniciando carga de animales desde Firestore...");

        db.collection("animales")
                .whereEqualTo("estadoRefugio", "Disponible Adopcion")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    animalList.clear();
                    int count = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Animal animal = document.toObject(Animal.class);

                            // ASIGNACIÓN CRÍTICA DEL ID
                            String docId = document.getId();
                            animal.setIdAnimal(docId);
                            animal.setId(docId);

                            animalList.add(animal);
                            count++;

                            Log.d(TAG, String.format("✅ Animal #%d: ID=%s, Nombre=%s, Especie=%s",
                                    count, docId, animal.getNombre(), animal.getEspecie()));

                        } catch (Exception e) {
                            Log.e(TAG, "❌ Error parseando animal: " + document.getId(), e);
                        }
                    }

                    Log.d(TAG, "📊 Total de animales cargados: " + count);

                    if (count == 0) {
                        Toast.makeText(this, "No hay animales disponibles para adopción", Toast.LENGTH_SHORT).show();
                    }

                    applyFilters();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error cargando animales de Firestore", e);
                    Toast.makeText(this, "Error al cargar animales: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void applyFilters() {
        filteredList.clear();
        String searchLower = etSearch.getText().toString().toLowerCase();

        for (Animal animal : animalList) {
            // Filtro de búsqueda por texto
            boolean matchesSearch = searchLower.isEmpty() ||
                    (animal.getNombre() != null && animal.getNombre().toLowerCase().contains(searchLower)) ||
                    (animal.getRaza() != null && animal.getRaza().toLowerCase().contains(searchLower));

            // Filtro por Especie
            boolean matchesEspecie = currentFilterEspecie.equals("Todos");
            if (!matchesEspecie && animal.getEspecie() != null) {
                String especie = animal.getEspecie().toLowerCase();
                switch (currentFilterEspecie) {
                    case "Perros":
                        matchesEspecie = especie.contains("perro");
                        break;
                    case "Gatos":
                        matchesEspecie = especie.contains("gato");
                        break;
                    case "Aves":
                        matchesEspecie = especie.contains("ave") || especie.contains("pájaro");
                        break;
                    case "Otros":
                        matchesEspecie = !especie.contains("perro") &&
                                !especie.contains("gato") &&
                                !especie.contains("ave") &&
                                !especie.contains("pájaro");
                        break;
                }
            }

            // Filtro por Edad
            boolean matchesEdad = currentFilterEdad.equals("Todas");
            if (!matchesEdad && animal.getEdadAprox() != null) {
                String edad = animal.getEdadAprox().toLowerCase();
                switch (currentFilterEdad) {
                    case "Cachorro":
                        matchesEdad = edad.contains("cachorro") || edad.contains("bebé");
                        break;
                    case "Joven":
                        matchesEdad = edad.contains("joven");
                        break;
                    case "Adulto":
                        matchesEdad = edad.contains("adulto");
                        break;
                    case "Senior":
                        matchesEdad = edad.contains("senior") || edad.contains("anciano");
                        break;
                }
            }

            // Filtro por Tamaño
            boolean matchesTamano = currentFilterTamano.equals("Todos");
            if (!matchesTamano && animal.getTamano() != null) {
                String tamano = animal.getTamano().toLowerCase();
                switch (currentFilterTamano) {
                    case "Pequeño":
                        matchesTamano = tamano.contains("pequeño") || tamano.contains("chico");
                        break;
                    case "Mediano":
                        matchesTamano = tamano.contains("mediano");
                        break;
                    case "Grande":
                        matchesTamano = tamano.contains("grande");
                        break;
                }
            }

            // Si cumple todos los filtros, lo agregamos
            if (matchesSearch && matchesEspecie && matchesEdad && matchesTamano) {
                filteredList.add(animal);
            }
        }

        Log.d(TAG, "🔎 Filtrados: " + filteredList.size() + " de " + animalList.size());
        animalAdapter.notifyDataSetChanged();

        // Mostrar mensaje si no hay resultados
        if (filteredList.isEmpty() && !animalList.isEmpty()) {
            Toast.makeText(this, "No se encontraron resultados con estos filtros", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }
            public void afterTextChanged(Editable s) {}
        });
        btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void setupFilters() {
        // Filtro por Especie
        chipGroupEspecie.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = findViewById(checkedIds.get(0));
                if (chip != null) {
                    currentFilterEspecie = chip.getText().toString();
                    Log.d(TAG, "Filtro Especie: " + currentFilterEspecie);
                    applyFilters();
                }
            } else {
                currentFilterEspecie = "Todos";
                applyFilters();
            }
        });

        // Filtro por Edad
        if (chipGroupEdad != null) {
            chipGroupEdad.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (!checkedIds.isEmpty()) {
                    Chip chip = findViewById(checkedIds.get(0));
                    if (chip != null) {
                        currentFilterEdad = chip.getText().toString();
                        Log.d(TAG, "Filtro Edad: " + currentFilterEdad);
                        applyFilters();
                    }
                } else {
                    currentFilterEdad = "Todas";
                    applyFilters();
                }
            });
        }

        // Filtro por Tamaño
        if (chipGroupTamano != null) {
            chipGroupTamano.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (!checkedIds.isEmpty()) {
                    Chip chip = findViewById(checkedIds.get(0));
                    if (chip != null) {
                        currentFilterTamano = chip.getText().toString();
                        Log.d(TAG, "Filtro Tamaño: " + currentFilterTamano);
                        applyFilters();
                    }
                } else {
                    currentFilterTamano = "Todos";
                    applyFilters();
                }
            });
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_favorites) startActivity(new Intent(this, FavoritesActivity.class));
            else if (id == R.id.nav_requests) startActivity(new Intent(this, MyRequestsActivity.class));
            else if (id == R.id.nav_profile) startActivity(new Intent(this, ProfileActivity.class));
            return id == R.id.nav_home;
        });
    }
}