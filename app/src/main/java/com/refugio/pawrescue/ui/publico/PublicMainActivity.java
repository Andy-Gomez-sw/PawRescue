package com.refugio.pawrescue.ui.publico;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import com.google.firebase.auth.FirebaseAuth;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.Animal;
import com.refugio.pawrescue.databinding.ActivityPublicMainBinding;
import com.refugio.pawrescue.ui.auth.LoginActivity;
import java.util.ArrayList;
import java.util.List;

public class PublicMainActivity extends AppCompatActivity {

    private ActivityPublicMainBinding binding;
    private PublicAnimalsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPublicMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
            startActivity(new Intent(this, MyAdoptionRequestsActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView() {
        adapter = new PublicAnimalsAdapter(animal -> showAnimalDetails(animal));

        binding.rvAnimals.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvAnimals.setAdapter(adapter);
    }

    private void loadAnimals() {
        binding.swipeRefresh.setOnRefreshListener(() -> loadAnimalesDisponibles());
        loadAnimalesDisponibles();
    }

    private void loadAnimalesDisponibles() {
        showLoading(true);

        // Aquí debes implementar tu lógica para cargar los animales
        /*
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("animales")
            .whereEqualTo("estado", "Disponible")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Animal> animales = new ArrayList<>();
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    Animal animal = doc.toObject(Animal.class);
                    if (animal != null) {
                        animales.add(animal);
                    }
                }
                adapter.submitList(animales);
                updateEmptyState(animales.isEmpty());
                showLoading(false);
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this,
                    "Error al cargar animales: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            });
        */

        // Simulación temporal (elimina esto en producción)
        new android.os.Handler().postDelayed(() -> {
            List<Animal> animales = new ArrayList<>();
            // Aquí obtendrías los datos reales
            adapter.submitList(animales);
            updateEmptyState(animales.isEmpty());
            showLoading(false);
        }, 1500);
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