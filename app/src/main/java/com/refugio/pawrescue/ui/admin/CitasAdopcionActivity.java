package com.refugio.pawrescue.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.SolicitudAdopcion;
import com.refugio.pawrescue.ui.adapter.CitasAdapter;

import java.util.ArrayList;
import java.util.List;

public class CitasAdopcionActivity extends AppCompatActivity implements CitasAdapter.OnCitaClickListener {

    private static final String TAG = "CitasAdopcionActivity";

    private RecyclerView recyclerView;
    private CitasAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private FirebaseFirestore db;
    private ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_citas_adopcion);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Gestión de Solicitudes");
        }

        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recycler_citas);
        progressBar = findViewById(R.id.progress_bar_citas);
        tvEmpty = findViewById(R.id.tv_empty_citas);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CitasAdapter(this, this);
        recyclerView.setAdapter(adapter);

        cargarTodasLasSolicitudes();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void cargarTodasLasSolicitudes() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        Query query = db.collection("solicitudes_adopcion")
                .orderBy("fechaSolicitud", Query.Direction.DESCENDING);

        listenerRegistration = query.addSnapshotListener((snapshots, e) -> {
            progressBar.setVisibility(View.GONE);

            if (e != null) {
                Log.e(TAG, "Error al cargar:", e);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Error: " + e.getMessage());
                return;
            }

            if (snapshots != null) {
                List<SolicitudAdopcion> listaMix = new ArrayList<>();

                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    try {
                        SolicitudAdopcion solicitud = doc.toObject(SolicitudAdopcion.class);

                        if (solicitud != null) {
                            solicitud.setIdSolicitud(doc.getId());

                            // 🔹 PARCHE: Leer campos manualmente si Firestore falló
                            if (solicitud.getNombreAnimal() == null) {
                                String nombre = doc.getString("animalNombre");
                                if (nombre != null) solicitud.setNombreAnimal(nombre);
                            }

                            if (solicitud.getIdAnimal() == null) {
                                String id = doc.getString("animalId");
                                if (id != null) solicitud.setIdAnimal(id);
                            }

                            if (solicitud.getNombreCompleto() == null) {
                                String nombre = doc.getString("nombreCompleto");
                                if (nombre != null) solicitud.setNombreCompleto(nombre);
                            }

                            if (solicitud.getTelefono() == null) {
                                String tel = doc.getString("telefono");
                                if (tel != null) solicitud.setTelefono(tel);
                            }

                            if (solicitud.getEmail() == null) {
                                String email = doc.getString("email");
                                if (email != null) solicitud.setEmail(email);
                            }

                            if (solicitud.getEstadoSolicitud() == null) {
                                String estado = doc.getString("estado");
                                if (estado == null) estado = doc.getString("estadoSolicitud");
                                if (estado != null) solicitud.setEstadoSolicitud(estado);
                            }

                            listaMix.add(solicitud);

                            // 🔹 LOG para debug
                            Log.d(TAG, String.format("✅ Solicitud: ID=%s | Animal=%s | Adoptante=%s | Tel=%s",
                                    solicitud.getIdSolicitud(),
                                    solicitud.getNombreAnimal(),
                                    solicitud.getNombreAdoptante(),
                                    solicitud.getTelefonoAdoptante()
                            ));
                        }
                    } catch (Exception ex) {
                        Log.w(TAG, "Error parseando solicitud: " + doc.getId(), ex);
                    }
                }

                adapter.setCitasList(listaMix);

                if (listaMix.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("No hay solicitudes registradas");
                    recyclerView.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    Log.d(TAG, "📊 Total solicitudes: " + listaMix.size());
                }
            }
        });
    }

    @Override
    public void onCitaClick(SolicitudAdopcion solicitud) {
        if (solicitud.getIdAnimal() != null && !solicitud.getIdAnimal().isEmpty()) {
            Intent intent = new Intent(this, DetalleAnimalActivity.class);
            intent.putExtra("animalId", solicitud.getIdAnimal());
            startActivity(intent);
        } else {
            Toast.makeText(this, "ID de animal no encontrado en esta solicitud", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Solicitud sin idAnimal: " + solicitud.getIdSolicitud());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}