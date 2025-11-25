package com.refugio.pawrescue.ui.volunteer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.Animal;
import com.refugio.pawrescue.ui.adapter.AnimalAdapter;

import java.util.ArrayList;
import java.util.List;

public class VolunteerMainActivity extends AppCompatActivity
        implements AnimalAdapter.OnAnimalClickListener {

    private static final String TAG = "VolunteerMainActivity";

    private TextView tvEmpty;
    private RecyclerView recyclerView;
    private AnimalAdapter animalAdapter;
    private BottomNavigationView bottomNav;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_main);

        // Toolbar / banner
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI
        tvEmpty = findViewById(R.id.tv_empty);
        recyclerView = findViewById(R.id.recycler_animales);
        bottomNav = findViewById(R.id.bottom_navigation_volunteer);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        animalAdapter = new AnimalAdapter(this, this);
        recyclerView.setAdapter(animalAdapter);

        // Bottom navigation (Animales / Mi Cuenta)
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.menu_animales) {
                // Ya estamos en Animales
                return true;

            } else if (id == R.id.menu_mi_cuenta) {
                Intent intent = new Intent(this, VolunteerAccountActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });

        bottomNav.setSelectedItemId(R.id.menu_animales);

        // Cargar animales al entrar
        cargarAnimalesAsignados();
    }

    private void cargarAnimalesAsignados() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Inicia sesión para ver tus animales asignados.");
            recyclerView.setVisibility(View.GONE);
            return;
        }

        String uid = currentUser.getUid();

        tvEmpty.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        Query query = db.collection("animales")
                .whereEqualTo("idVoluntario", uid);

        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }

        listenerRegistration = query.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.e(TAG, "Error obteniendo animales asignados", e);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Error al cargar animales asignados.");
                recyclerView.setVisibility(View.GONE);
                return;
            }

            if (snapshots == null || snapshots.isEmpty()) {
                animalAdapter.setAnimalesList(new ArrayList<>());
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Actualmente no tienes animales asignados.");
                recyclerView.setVisibility(View.GONE);
                return;
            }

            List<Animal> animales = new ArrayList<>();
            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                try {
                    Animal animal = doc.toObject(Animal.class);
                    if (animal != null) {
                        animal.setIdAnimal(doc.getId());
                        animales.add(animal);
                    }
                } catch (Exception ex) {
                    Log.w(TAG, "Documento de animal mal formado: " + doc.getId(), ex);
                }
            }

            animalAdapter.setAnimalesList(animales);
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void onAnimalClick(Animal animal) {
        Intent intent = new Intent(this, VolunteerAnimalDetailActivity.class);
        intent.putExtra(VolunteerAnimalDetailActivity.EXTRA_ANIMAL, animal);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}
