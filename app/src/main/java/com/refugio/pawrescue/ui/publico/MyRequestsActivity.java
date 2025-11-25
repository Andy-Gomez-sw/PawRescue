package com.refugio.pawrescue.ui.publico;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.refugio.pawrescue.R;

// --- IMPORTANTE: Usamos el modelo correcto ---
import com.refugio.pawrescue.model.AdoptionRequest;
import com.refugio.pawrescue.ui.adapter.RequestAdapter;

import java.util.ArrayList;
import java.util.List;

public class MyRequestsActivity extends AppCompatActivity {

    private RecyclerView rvRequests;
    private TextView tvEmptyRequests;
    private RequestAdapter requestAdapter;
    private List<AdoptionRequest> requestList;
    private List<AdoptionRequest> filteredList; // Por si quisieras filtrar después
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_requests);

        rvRequests = findViewById(R.id.rvRequests);
        tvEmptyRequests = findViewById(R.id.tvEmptyRequests); // Asegúrate de tener este ID en el XML

        db = FirebaseFirestore.getInstance();
        requestList = new ArrayList<>();
        filteredList = new ArrayList<>();

        setupRecyclerView();
        loadRequests();
    }

    private void setupRecyclerView() {
        // Inicializamos el adaptador con la lista filtrada (que al inicio es igual a la original)
        requestAdapter = new RequestAdapter(this, filteredList, request -> {
            // Al hacer click, vamos al detalle
            Intent intent = new Intent(MyRequestsActivity.this, RequestDetailActivity.class);
            intent.putExtra("REQUEST_ID", request.getFolio()); // O el ID del documento
            // También podemos pasar el objeto si es Serializable
            intent.putExtra("REQUEST_OBJ", request);
            startActivity(intent);
        });

        rvRequests.setLayoutManager(new LinearLayoutManager(this));
        rvRequests.setAdapter(requestAdapter);
    }

    private void loadRequests() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId == null) return;

        // Buscamos las solicitudes donde el usuario sea el solicitante
        db.collection("solicitudes")
                .whereEqualTo("usuarioId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    requestList.clear();
                    if (queryDocumentSnapshots.isEmpty()) {
                        showEmptyState(true);
                    } else {
                        showEmptyState(false);
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            AdoptionRequest req = doc.toObject(AdoptionRequest.class);
                            // Asignamos datos extra si faltan (como el ID del documento)
                            // req.setId(doc.getId());
                            requestList.add(req);
                        }
                        // Actualizamos la lista que usa el adaptador
                        filteredList.clear();
                        filteredList.addAll(requestList);
                        requestAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar solicitudes", Toast.LENGTH_SHORT).show();
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