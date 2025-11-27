package com.refugio.pawrescue.ui.publico;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout; // 🟢 IMPORTACIÓN AGREGADA
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.Animal;
import com.refugio.pawrescue.ui.adapter.AnimalAdapter;
import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView rvFavorites;
    private LinearLayout tvEmptyFavorites; // 🟢 CAMBIO: De TextView a LinearLayout
    private BottomNavigationView bottomNavigation;
    private AnimalAdapter adapter;
    private List<Animal> favoriteList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        rvFavorites = findViewById(R.id.rvFavorites);
        tvEmptyFavorites = findViewById(R.id.tvEmptyFavorites); // Ahora esto es correcto (LinearLayout)
        bottomNavigation = findViewById(R.id.bottomNavigation);
        db = FirebaseFirestore.getInstance();
        favoriteList = new ArrayList<>();

        setupRecyclerView();
        setupBottomNavigation();
        loadFavorites();
    }

    private void setupRecyclerView() {
        adapter = new AnimalAdapter(this, favoriteList, new AnimalAdapter.OnAnimalClickListener() {
            @Override
            public void onAnimalClick(Animal animal) {
                Intent intent = new Intent(FavoritesActivity.this, AnimalDetailsPublicActivity.class);
                intent.putExtra("ANIMAL_ID", animal.getId());
                intent.putExtra("ANIMAL_OBJETO", animal);
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(Animal animal) {
                removeFavorite(animal);
            }
        });

        rvFavorites.setLayoutManager(new GridLayoutManager(this, 2));
        rvFavorites.setAdapter(adapter);
    }

    private void setupBottomNavigation() {
        // Marcar como seleccionado el item de Favoritos
        bottomNavigation.setSelectedItemId(R.id.nav_favorites);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, GalleryActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_favorites) {
                // Ya estamos aquí
                return true;
            } else if (id == R.id.nav_requests) {
                startActivity(new Intent(this, MyRequestsActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }

            return false;
        });
    }

    private void loadFavorites() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("usuarios").document(userId).collection("favoritos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    favoriteList.clear();
                    if (queryDocumentSnapshots.isEmpty()) {
                        tvEmptyFavorites.setVisibility(View.VISIBLE);
                        rvFavorites.setVisibility(View.GONE);
                    } else {
                        tvEmptyFavorites.setVisibility(View.GONE);
                        rvFavorites.setVisibility(View.VISIBLE);
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Animal animal = doc.toObject(Animal.class);
                            animal.setId(doc.getId());
                            animal.setFavorited(true);
                            favoriteList.add(animal);
                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar favoritos", Toast.LENGTH_SHORT).show();
                });
    }

    private void removeFavorite(Animal animal) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("usuarios").document(userId)
                .collection("favoritos")
                .document(animal.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    favoriteList.remove(animal);
                    adapter.notifyDataSetChanged();

                    if (favoriteList.isEmpty()) {
                        tvEmptyFavorites.setVisibility(View.VISIBLE);
                        rvFavorites.setVisibility(View.GONE);
                    }
                    Toast.makeText(this, "Eliminado de favoritos", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar favoritos al volver a la actividad
        loadFavorites();
    }
}