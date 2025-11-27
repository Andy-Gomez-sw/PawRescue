package com.refugio.pawrescue.ui.publico;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.Animal;
import com.refugio.pawrescue.ui.adapter.PublicAnimalsAdapter;

import java.util.ArrayList;
import java.util.List;

public class PublicMainActivity extends AppCompatActivity {

    private RecyclerView rvPublicAnimals;
    private BottomNavigationView bottomNavigation;
    private ChipGroup chipGroupFilters;

    private PublicAnimalsAdapter adapter;
    private List<Animal> animalList;     // Lista visible
    private List<Animal> fullAnimalList; // Lista completa (respaldo)

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_main);

        initViews();
        initFirebase();
        setupRecyclerView();
        setupFilters();
        setupBottomNavigation();

        loadAnimals();
    }

    private void initViews() {
        rvPublicAnimals = findViewById(R.id.rvPublicAnimals);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        chipGroupFilters = findViewById(R.id.chipGroupFilters);
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    private void setupRecyclerView() {
        animalList = new ArrayList<>();
        fullAnimalList = new ArrayList<>();

        adapter = new PublicAnimalsAdapter(this, animalList, animal -> {
            Intent intent = new Intent(PublicMainActivity.this, AnimalDetailsPublicActivity.class);
            intent.putExtra("ANIMAL_ID", animal.getId());
            intent.putExtra("ANIMAL_OBJETO", animal);
            startActivity(intent);
        });

        rvPublicAnimals.setLayoutManager(new GridLayoutManager(this, 2));
        rvPublicAnimals.setAdapter(adapter);
    }

    private void setupFilters() {
        chipGroupFilters.setOnCheckedChangeListener((group, checkedId) -> {
            String filtro = "Todos";

            if (checkedId == R.id.chipPerro) filtro = "Perro";
            else if (checkedId == R.id.chipGato) filtro = "Gato";
            else if (checkedId == R.id.chipAve) filtro = "Ave";
            else if (checkedId == R.id.chipOtro) filtro = "Otro";

            aplicarFiltro(filtro);
        });
    }

    private void aplicarFiltro(String especie) {
        animalList.clear();

        if (especie.equals("Todos")) {
            animalList.addAll(fullAnimalList);
        } else {
            for (Animal animal : fullAnimalList) {
                // Comparar ignorando mayúsculas
                if (animal.getEspecie() != null && animal.getEspecie().equalsIgnoreCase(especie)) {
                    animalList.add(animal);
                }
            }
        }

        adapter.notifyDataSetChanged();

        if (animalList.isEmpty()) {
            Toast.makeText(this, "No hay " + especie + "s disponibles", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAnimals() {
        // Descargar SOLO los animales disponibles
        db.collection("animales")
                .whereEqualTo("estadoRefugio", "Disponible Adopcion")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    fullAnimalList.clear();
                    animalList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Animal animal = doc.toObject(Animal.class);
                            animal.setId(doc.getId());

                            fullAnimalList.add(animal);
                            animalList.add(animal);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    adapter.notifyDataSetChanged();

                    if (fullAnimalList.isEmpty()) {
                        Toast.makeText(this, "No hay mascotas disponibles por ahora", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_home);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_favorites) {
                startActivity(new Intent(this, FavoritesActivity.class));
                return true;
            } else if (id == R.id.nav_requests) {
                startActivity(new Intent(this, MyRequestsActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }
    }
}