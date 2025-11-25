package com.refugio.pawrescue.ui.publico;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

    private RecyclerView recyclerViewAnimals;
    private AnimalAdapter animalAdapter;
    private List<Animal> animalList;
    private List<Animal> filteredList;
    private EditText etSearch;
    private ImageButton btnFilter;
    private ImageButton btnProfile;
    private ChipGroup chipGroup;
    private BottomNavigationView bottomNavigation;
    private FirebaseFirestore db;
    private String currentFilter = "Todos";

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
        btnFilter = findViewById(R.id.btnFilter);
        btnProfile = findViewById(R.id.btnProfile);
        chipGroup = findViewById(R.id.chipGroup);
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
                // --- MÉTODO SEGURO: SOLO ENVIAMOS EL ID ---
                if (animal.getId() != null && !animal.getId().isEmpty()) {
                    // Toast.makeText(GalleryActivity.this, "Abriendo: " + animal.getNombre(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(GalleryActivity.this, AnimalDetailsPublicActivity.class);
                    intent.putExtra("ANIMAL_ID", animal.getId()); // Solo enviamos texto
                    startActivity(intent);
                } else {
                    Toast.makeText(GalleryActivity.this, "Error: Animal sin ID", Toast.LENGTH_SHORT).show();
                }
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
        db.collection("animales").get().addOnSuccessListener(queryDocumentSnapshots -> {
            animalList.clear();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                try {
                    Animal animal = document.toObject(Animal.class);
                    // Forzamos guardar el ID del documento
                    animal.setId(document.getId());
                    animal.setIdAnimal(document.getId());
                    animalList.add(animal);
                } catch (Exception e) {
                    Log.e("Gallery", "Error parsing animal", e);
                }
            }
            filterAnimals(etSearch.getText().toString(), currentFilter);
        });
    }

    private void filterAnimals(String searchText, String filterCategory) {
        filteredList.clear();
        String searchLower = searchText.toLowerCase();
        for (Animal animal : animalList) {
            boolean matchesSearch = searchText.isEmpty() ||
                    (animal.getNombre() != null && animal.getNombre().toLowerCase().contains(searchLower));

            boolean matchesFilter = true;
            if (!filterCategory.equals("Todos")) {
                if (animal.getEspecie() != null) {
                    matchesFilter = animal.getEspecie().equalsIgnoreCase(filterCategory);
                    if (filterCategory.equals("Cachorros")) matchesFilter = animal.getEdad() < 1;
                } else {
                    matchesFilter = false;
                }
            }

            if (matchesSearch && matchesFilter) filteredList.add(animal);
        }
        animalAdapter.notifyDataSetChanged();
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { filterAnimals(s.toString(), currentFilter); }
            public void afterTextChanged(Editable s) {}
        });
        btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void setupFilters() {
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = findViewById(checkedIds.get(0));
                if (chip != null) {
                    currentFilter = chip.getText().toString();
                    if(currentFilter.equals("Perros")) currentFilter = "Perro";
                    if(currentFilter.equals("Gatos")) currentFilter = "Gato";
                    filterAnimals(etSearch.getText().toString(), currentFilter);
                }
            } else {
                currentFilter = "Todos";
                filterAnimals(etSearch.getText().toString(), currentFilter);
            }
        });
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