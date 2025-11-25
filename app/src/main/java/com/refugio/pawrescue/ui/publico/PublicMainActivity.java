package com.refugio.pawrescue.ui.publico;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
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

    private ActivityPublicMainBinding binding;
    private AnimalAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPublicMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        setupToolbar();
        setupRecyclerView();
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
        adapter = new AnimalAdapter(this, animal -> showAnimalDetails(animal));
        binding.rvAnimals.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvAnimals.setAdapter(adapter);
    }

    private void loadAnimals() {
        binding.swipeRefresh.setOnRefreshListener(() -> loadAnimalesDisponibles());
        loadAnimalesDisponibles();
    }

    private void loadAnimalesDisponibles() {
        showLoading(true);

        // 🐾 CARGAMOS TODOS LOS ANIMALES SIN FILTRAR
        db.collection("animales")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Animal> animales = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Animal animal = doc.toObject(Animal.class);
                            if (animal != null) {
                                // ✅ ASEGURAMOS QUE EL ID ESTÉ PRESENTE
                                animal.setIdAnimal(doc.getId());
                                animal.setId(doc.getId());

                                // 📝 LOG PARA DEBUG
                                android.util.Log.d("PublicMain", "✅ Animal cargado: " + animal.getNombre() +
                                        " | ID: " + animal.getId() +
                                        " | Estado: " + animal.getEstadoRefugio());

                                // ✅ AGREGAMOS TODOS LOS ANIMALES
                                animales.add(animal);
                            }
                        } catch (Exception e) {
                            android.util.Log.e("PublicMain", "❌ Error parseando animal: " + doc.getId(), e);
                        }
                    }

                    android.util.Log.d("PublicMain", "📊 Total de animales cargados: " + animales.size());

                    adapter.setAnimalesList(animales);
                    updateEmptyState(animales.isEmpty());
                    showLoading(false);

                    if (animales.isEmpty()) {
                        Toast.makeText(this,
                                "No hay animales en la base de datos",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    android.util.Log.e("PublicMain", "❌ Error de Firestore", e);
                    Toast.makeText(this,
                            "Error al cargar animales: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void showAnimalDetails(Animal animal) {
        // ✅ CAMBIO CRÍTICO: Ahora pasamos el ANIMAL_ID en lugar del objeto completo
        Intent intent = new Intent(this, AnimalDetailsPublicActivity.class);
        intent.putExtra("ANIMAL_ID", animal.getId()); // 👈 Esto es lo que espera AnimalDetailsPublicActivity
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