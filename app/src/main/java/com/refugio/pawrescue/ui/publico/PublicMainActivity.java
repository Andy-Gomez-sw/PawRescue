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
import com.refugio.pawrescue.ui.adapter.AnimalAdapter; // Usamos tu adaptador existente

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

        // 1. Inicializar Firestore
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
        // Asegúrate de tener res/menu/public_menu.xml creado, si no, comenta esta línea
        getMenuInflater().inflate(R.menu.public_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_my_requests) {
            startActivity(new Intent(this, MyAdoptionRequestsActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView() {
        // 2. Configurar el adaptador con el click listener
        adapter = new AnimalAdapter(this, animal -> showAnimalDetails(animal));

        // Usamos GridLayoutManager para que se vea en 2 columnas (como en la imagen de muestra)
        binding.rvAnimals.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvAnimals.setAdapter(adapter);
    }

    private void loadAnimals() {
        binding.swipeRefresh.setOnRefreshListener(() -> loadAnimalesDisponibles());
        loadAnimalesDisponibles();
    }

    private void loadAnimalesDisponibles() {
        showLoading(true);

        // 3. Consulta a Firebase CORREGIDA
        db.collection("animales")
                // --- NOTA: He comentado el filtro para que veas TODOS los animales por ahora ---
                // .whereEqualTo("estadoRefugio", "Disponible Adopcion")
                // -------------------------------------------------------------------------------
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Animal> animales = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Animal animal = doc.toObject(Animal.class);
                            if (animal != null) {
                                // Aseguramos que el ID del documento esté en el objeto
                                animal.setIdAnimal(doc.getId());
                                animales.add(animal);
                            }
                        } catch (Exception e) {
                            // Ignorar documentos mal formados
                        }
                    }

                    // Actualizar la lista en el adaptador
                    adapter.setAnimalesList(animales);

                    // Mostrar imagen de "vacío" si no hay datos
                    updateEmptyState(animales.isEmpty());
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this,
                            "Error al cargar animales: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void showAnimalDetails(Animal animal) {
        Intent intent = new Intent(this, AnimalDetailsPublicActivity.class);
        intent.putExtra("animal", animal);
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