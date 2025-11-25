package com.refugio.pawrescue.ui.publico;

import android.content.Intent;
import android.os.Bundle;
import android.view.View; // Importante
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.ui.adapter.RequestAdapter;
import java.util.ArrayList;
import java.util.List;

public class MyRequestsActivity extends AppCompatActivity {

    private RecyclerView rvRequests;

    // 🔴 CORRECCIÓN: Cambiamos TextView por View (para que acepte el LinearLayout del XML)
    private View tvEmptyRequests;

    private RequestAdapter requestAdapter;
    private List<AdoptionRequest> requestList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_requests);

        initViews();
        initFirebase();
        setupRecyclerView();
        loadRequests();
    }

    private void initViews() {
        rvRequests = findViewById(R.id.rvRequests);
        // Ahora esto funcionará porque View es compatible con LinearLayout
        tvEmptyRequests = findViewById(R.id.tvEmptyRequests);
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    private void setupRecyclerView() {
        requestList = new ArrayList<>();
        requestAdapter = new RequestAdapter(this, requestList, request -> {
            Intent intent = new Intent(MyRequestsActivity.this, RequestDetailActivity.class);
            intent.putExtra("REQUEST_OBJ", request);
            startActivity(intent);
        });

        rvRequests.setLayoutManager(new LinearLayoutManager(this));
        rvRequests.setAdapter(requestAdapter);
    }

    private void loadRequests() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("solicitudes_adopcion")
                .whereEqualTo("usuarioId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    requestList.clear();
                    if (queryDocumentSnapshots.isEmpty()) {
                        showEmptyState(true);
                    } else {
                        showEmptyState(false);
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            try {
                                AdoptionRequest req = doc.toObject(AdoptionRequest.class);
                                req.setId(doc.getId());

                                if (req.getAnimalNombre() == null && doc.contains("animalNombre")) {
                                    req.setAnimalNombre(doc.getString("animalNombre"));
                                }
                                requestList.add(req);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        requestAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showEmptyState(true);
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
}