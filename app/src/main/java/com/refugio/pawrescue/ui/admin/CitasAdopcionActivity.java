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

                            // 🔴 PARCHE MEJORADO: Lectura robusta de campos
                            // Solo aplicamos el parche si el campo está vacío en el objeto

                            // 1. Nombre del Animal
                            if (solicitud.getNombreAnimal() == null) {
                                String animalNombre = doc.getString("animalNombre");
                                if (animalNombre != null) {
                                    solicitud.setNombreAnimal(animalNombre);
                                }
                            }

                            // 2. Estado
                            if (solicitud.getEstadoSolicitud() == null) {
                                String estado = doc.getString("estado");
                                if (estado == null) {
                                    estado = doc.getString("estadoSolicitud");
                                }
                                if (estado != null) {
                                    solicitud.setEstadoSolicitud(estado);
                                }
                            }

                            // 3. ID del Animal
                            if (solicitud.getIdAnimal() == null) {
                                String animalId = doc.getString("animalId");
                                if (animalId != null) {
                                    solicitud.setIdAnimal(animalId);
                                }
                            }

                            // 4. Nombre completo del adoptante
                            if (solicitud.getNombreCompleto() == null) {
                                String nombreCompleto = doc.getString("nombreCompleto");
                                if (nombreCompleto != null) {
                                    solicitud.setNombreCompleto(nombreCompleto);
                                }
                            }

                            // 5. Teléfono
                            if (solicitud.getTelefono() == null) {
                                String telefono = doc.getString("telefono");
                                if (telefono != null) {
                                    solicitud.setTelefono(telefono);
                                }
                            }

                            // 6. Email
                            if (solicitud.getEmail() == null) {
                                String email = doc.getString("email");
                                if (email != null) {
                                    solicitud.setEmail(email);
                                }
                            }

                            listaMix.add(solicitud);

                            // Log para debug
                            Log.d(TAG, String.format("Solicitud cargada - ID: %s, Animal: %s, Estado: %s, Adoptante: %s",
                                    solicitud.getIdSolicitud(),
                                    solicitud.getNombreAnimal(),
                                    solicitud.getEstadoSolicitud(),
                                    solicitud.getNombreAdoptante()
                            ));
                        }
                    } catch (Exception ex) {
                        Log.w(TAG, "Error leyendo solicitud: " + doc.getId(), ex);
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
                    Log.d(TAG, "Total de solicitudes cargadas: " + listaMix.size());
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