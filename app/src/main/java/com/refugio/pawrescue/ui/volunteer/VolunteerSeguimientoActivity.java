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

public class VolunteerSeguimientoActivity extends AppCompatActivity
        implements AnimalAdapter.OnAnimalClickListener {

    private static final String TAG = "VolunteerSeguimiento";

    private BottomNavigationView bottomNav;
    private RecyclerView recyclerView;
    private TextView tvEmpty;

    private AnimalAdapter animalAdapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_seguimiento);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("PawRescue-Seguimiento");
        }

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        tvEmpty = findViewById(R.id.tv_empty_seguimiento);
        recyclerView = findViewById(R.id.recycler_animales_adoptados);
        bottomNav = findViewById(R.id.bottom_navigation_volunteer);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        animalAdapter = new AnimalAdapter(this, this);
        recyclerView.setAdapter(animalAdapter);

        // Bottom navigation
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.menu_animales) {
                Intent intent = new Intent(this, VolunteerMainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;

            } else if (id == R.id.menu_seguimiento) {
                // Ya estamos aquí
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

        // Dejar seleccionado el icono de seguimiento
        bottomNav.setSelectedItemId(R.id.menu_seguimiento);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Sesión no válida. Vuelve a iniciar sesión.");
            recyclerView.setVisibility(View.GONE);
            return;
        }

        String uid = user.getUid();

        // Animales de este voluntario y con estado Adoptado
        Query query = db.collection("animales")
                .whereEqualTo("idVoluntario", uid)
                .whereEqualTo("estadoRefugio", "Adoptado");

        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }

        listenerRegistration = query.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.e(TAG, "Error obteniendo animales adoptados", e);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Error al cargar el seguimiento.");
                recyclerView.setVisibility(View.GONE);
                return;
            }

            if (snapshots == null || snapshots.isEmpty()) {
                animalAdapter.setAnimalesList(new ArrayList<>());
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Aún no tienes animales adoptados con seguimiento.");
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
    protected void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }

    @Override
    public void onAnimalClick(Animal animal) {
        Intent intent = new Intent(this, VolunteerSeguimientoDetailActivity.class);
        intent.putExtra(VolunteerSeguimientoDetailActivity.EXTRA_ANIMAL, animal);
        startActivity(intent);
    }
}
