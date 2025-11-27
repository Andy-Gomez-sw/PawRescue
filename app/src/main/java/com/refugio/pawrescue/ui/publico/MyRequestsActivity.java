package com.refugio.pawrescue.ui.publico;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout; // 🟢 Cambiado a LinearLayout
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.ui.adapter.RequestAdapter; // 🟢 Asegúrate de usar RequestAdapter
import java.util.ArrayList;
import java.util.List;

public class MyRequestsActivity extends AppCompatActivity {

    private RecyclerView rvRequests;
    private LinearLayout tvEmptyRequests; // 🟢 Tipo corregido
    private BottomNavigationView bottomNavigation;

    private RequestAdapter requestAdapter;
    private List<AdoptionRequest> requestList;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ⚠️ Verifica que el nombre de tu layout sea correcto.
        // Si tu archivo se llama 'activity_my_adoption_requests.xml', cámbialo aquí.
        setContentView(R.layout.activity_my_adoption_requests);

        initFirebase();
        initViews();
        setupRecyclerView();
        setupBottomNavigation();

        // Cargar datos inicialmente
        loadRequests();
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void initViews() {
        rvRequests = findViewById(R.id.rvRequests);
        tvEmptyRequests = findViewById(R.id.tvEmptyRequests);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupRecyclerView() {
        requestList = new ArrayList<>();

        // Inicializamos el adaptador con el listener de clic
        requestAdapter = new RequestAdapter(this, requestList, request -> {
            Intent intent = new Intent(MyRequestsActivity.this, RequestDetailActivity.class);
            intent.putExtra("REQUEST_OBJ", request);
            intent.putExtra("REQUEST_ID", request.getId()); // 🟢 Respaldo importante
            startActivity(intent);
        });

        rvRequests.setLayoutManager(new LinearLayoutManager(this));
        rvRequests.setAdapter(requestAdapter);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_requests);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, PublicMainActivity.class)); // 🟢 Ajustado a PublicMainActivity (común en tu app)
                finish();
                return true;
            } else if (id == R.id.nav_favorites) {
                startActivity(new Intent(this, FavoritesActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_requests) {
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadRequests() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        db.collection("solicitudes_adopcion")
                .whereEqualTo("usuarioId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    requestList.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        showEmptyState(true);
                    } else {
                        showEmptyState(false);
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            try {
                                AdoptionRequest req = doc.toObject(AdoptionRequest.class);
                                if (req != null) {
                                    req.setId(doc.getId());

                                    // Validación extra por si faltan datos en Firestore
                                    if (req.getAnimalNombre() == null && doc.contains("animalNombre")) {
                                        req.setAnimalNombre(doc.getString("animalNombre"));
                                    }
                                    requestList.add(req);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        requestAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar solicitudes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showEmptyState(true); // Mostrar vacío en caso de error también
                });
    }

    private void showEmptyState(boolean isEmpty) {
        if (isEmpty) {
            tvEmptyRequests.setVisibility(View.VISIBLE);
            rvRequests.setVisibility(View.GONE);
        } else {
            tvEmptyRequests.setVisibility(View.GONE);
            rvRequests.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRequests(); // Recargar al volver para actualizar estados
    }
}