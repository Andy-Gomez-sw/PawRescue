package com.refugio.pawrescue.ui.publico;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
    private TextView tvEmptyFavorites;
    private AnimalAdapter adapter;
    private List<Animal> favoriteList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        rvFavorites = findViewById(R.id.rvFavorites);
        tvEmptyFavorites = findViewById(R.id.tvEmptyFavorites);
        db = FirebaseFirestore.getInstance();
        favoriteList = new ArrayList<>();

        setupRecyclerView();
        loadFavorites();
    }

    private void setupRecyclerView() {
        // Usamos el constructor que acepta la lista, el cual arreglamos antes
        adapter = new AnimalAdapter(this, favoriteList, new AnimalAdapter.OnAnimalClickListener() {
            @Override
            public void onAnimalClick(Animal animal) {
                // Ir al detalle del animal
                Intent intent = new Intent(FavoritesActivity.this, AnimalDetailsPublicActivity.class);
                intent.putExtra("ANIMAL_ID", animal.getId());
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(Animal animal) {
                // Opción para quitar de favoritos desde esta misma lista
                removeFavorite(animal);
            }
        });

        rvFavorites.setLayoutManager(new GridLayoutManager(this, 2));
        rvFavorites.setAdapter(adapter);
    }

    private void loadFavorites() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId == null) return;

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
                            animal.setFavorited(true); // Ya sabemos que es favorito porque está en esta colección
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
                    // Lo quitamos de la lista local y actualizamos
                    favoriteList.remove(animal);
                    adapter.notifyDataSetChanged();

                    if (favoriteList.isEmpty()) {
                        tvEmptyFavorites.setVisibility(View.VISIBLE);
                        rvFavorites.setVisibility(View.GONE);
                    }
                    Toast.makeText(this, "Eliminado de favoritos", Toast.LENGTH_SHORT).show();
                });
    }
}