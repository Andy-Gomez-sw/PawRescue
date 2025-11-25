package com.refugio.pawrescue.ui.publico;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.Animal;
import com.refugio.pawrescue.databinding.ActivityPublicMainBinding;
import com.refugio.pawrescue.ui.auth.LoginActivity;
import com.refugio.pawrescue.ui.adapter.AnimalAdapter;

import java.util.ArrayList;
import java.util.List;

public class PublicMainActivity extends AppCompatActivity {

    private static final String TAG = "PublicMain";

    private ActivityPublicMainBinding binding;
    private AnimalAdapter adapter;
    private FirebaseFirestore db;

    private List<Animal> animalList = new ArrayList<>();
    private List<Animal> filteredList = new ArrayList<>();
    private String currentFilter = "Todos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPublicMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        setupToolbar();
        setupRecyclerView();
        setupSearch();
        setupFilters();
        loadAnimals();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Adopta un Amigo");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.public_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_my_requests) {
            startActivity(new Intent(this, MyRequestsActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView() {
        adapter = new AnimalAdapter(this, filteredList, new AnimalAdapter.OnAnimalClickListener() {
            @Override
            public void onAnimalClick(Animal animal) {
                showAnimalDetails(animal);
            }

            @Override
            public void onFavoriteClick(Animal animal) {
                Toast.makeText(PublicMainActivity.this,
                        "Favorito: " + animal.getNombre(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        binding.rvAnimals.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvAnimals.setAdapter(adapter);
    }

    private void setupSearch() {
        if (binding.etSearch != null) {
            binding.etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterAnimals(s.toString(), currentFilter);
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void setupFilters() {
        if (binding.chipGroup != null) {
            binding.chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (!checkedIds.isEmpty()) {
                    Chip chip = findViewById(checkedIds.get(0));
                    if (chip != null) {
                        currentFilter = chip.getText().toString();
                        android.util.Log.d(TAG, "🏷️ Filtro seleccionado: " + currentFilter);
                        filterAnimals(
                                binding.etSearch != null ? binding.etSearch.getText().toString() : "",
                                currentFilter
                        );
                    }
                } else {
                    currentFilter = "Todos";
                    android.util.Log.d(TAG, "🏷️ Filtro: Mostrar todos");
                    filterAnimals(
                            binding.etSearch != null ? binding.etSearch.getText().toString() : "",
                            currentFilter
                    );
                }
            });
        }
    }

    private void loadAnimals() {
        binding.swipeRefresh.setOnRefreshListener(this::loadAnimalesDisponibles);
        loadAnimalesDisponibles();
    }

    private void loadAnimalesDisponibles() {
        showLoading(true);

        db.collection("animales")
                .whereEqualTo("estadoRefugio", "Disponible Adopcion")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    animalList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Animal animal = doc.toObject(Animal.class);
                            if (animal != null) {
                                animal.setIdAnimal(doc.getId());
                                animal.setId(doc.getId());

                                android.util.Log.d(TAG, "✅ Animal: " + animal.getNombre() +
                                        " | ID: " + animal.getId() +
                                        " | Especie: " + animal.getEspecie() +
                                        " | Estado: " + animal.getEstadoRefugio());

                                animalList.add(animal);
                            }
                        } catch (Exception e) {
                            android.util.Log.e(TAG, "❌ Error parseando animal: " + doc.getId(), e);
                        }
                    }

                    android.util.Log.d(TAG, "📊 Total animales cargados: " + animalList.size());

                    showLoading(false);

                    // Aplicar filtros después de cargar
                    String searchText = binding.etSearch != null ?
                            binding.etSearch.getText().toString() : "";
                    filterAnimals(searchText, currentFilter);

                    if (animalList.isEmpty()) {
                        Toast.makeText(this,
                                "No hay animales disponibles para adopción",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    android.util.Log.e(TAG, "❌ Error de Firestore", e);
                    Toast.makeText(this,
                            "Error al cargar animales: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    updateEmptyState(true);
                });
    }

    private void filterAnimals(String searchText, String filterCategory) {
        filteredList.clear();
        String searchLower = searchText.toLowerCase().trim();

        android.util.Log.d(TAG, "🔎 Filtrando: Búsqueda='" + searchText +
                "', Categoría='" + filterCategory + "'");
        android.util.Log.d(TAG, "📊 Total en animalList: " + animalList.size());

        for (Animal animal : animalList) {
            // Filtro de búsqueda por texto
            boolean matchesSearch = searchText.isEmpty() ||
                    (animal.getNombre() != null && animal.getNombre().toLowerCase().contains(searchLower)) ||
                    (animal.getRaza() != null && animal.getRaza().toLowerCase().contains(searchLower));

            // Filtro por categoría
            boolean matchesFilter = true;
            if (!filterCategory.equals("Todos")) {
                String especie = animal.getEspecie();

                // Log para debug
                android.util.Log.d(TAG, "🐾 Animal: " + animal.getNombre() +
                        " | Especie en BD: '" + especie + "' | Filtro: '" + filterCategory + "'");

                if (especie != null && !especie.isEmpty()) {
                    switch (filterCategory) {
                        case "Perros":
                        case "Perro":
                            matchesFilter = especie.equalsIgnoreCase("Perro");
                            break;
                        case "Gatos":
                        case "Gato":
                            matchesFilter = especie.equalsIgnoreCase("Gato");
                            break;
                        case "Aves":
                        case "Ave":
                            matchesFilter = especie.equalsIgnoreCase("Ave") ||
                                    especie.equalsIgnoreCase("Aves");
                            break;
                        case "Otros":
                        case "Otro":
                            matchesFilter = !especie.equalsIgnoreCase("Perro") &&
                                    !especie.equalsIgnoreCase("Gato") &&
                                    !especie.equalsIgnoreCase("Ave") &&
                                    !especie.equalsIgnoreCase("Aves");
                            break;
                        default:
                            matchesFilter = true;
                    }

                    android.util.Log.d(TAG, "  ➡️ matchesFilter: " + matchesFilter);
                } else {
                    android.util.Log.w(TAG, "⚠️ Animal sin especie: " + animal.getNombre());
                    matchesFilter = false;
                }
            }

            if (matchesSearch && matchesFilter) {
                filteredList.add(animal);
                android.util.Log.d(TAG, "  ✅ AGREGADO a filteredList");
            } else {
                android.util.Log.d(TAG, "  ❌ NO agregado (search:" + matchesSearch +
                        ", filter:" + matchesFilter + ")");
            }
        }

        android.util.Log.d(TAG, "📋 Resultados FINALES: " + filteredList.size() +
                " de " + animalList.size() + " animales");
        android.util.Log.d(TAG, "============================================");

        adapter.notifyDataSetChanged();
        updateEmptyState(filteredList.isEmpty());
    }

    private void showAnimalDetails(Animal animal) {
        Intent intent = new Intent(this, AnimalDetailsPublicActivity.class);
        intent.putExtra("ANIMAL_ID", animal.getId());
        startActivity(intent);
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            binding.rvAnimals.setVisibility(View.GONE);
            binding.llEmptyState.setVisibility(View.VISIBLE);
        } else {
            binding.rvAnimals.setVisibility(View.VISIBLE);
            binding.llEmptyState.setVisibility(View.GONE);
        }
    }

    private void showLoading(boolean isLoading) {
        binding.swipeRefresh.setRefreshing(isLoading);
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}