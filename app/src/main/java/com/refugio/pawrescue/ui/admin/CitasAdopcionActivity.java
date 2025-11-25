package com.refugio.pawrescue.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.SolicitudAdopcion;
import com.refugio.pawrescue.ui.adapter.CitasAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Activity para visualizar todas las citas de adopción programadas (RF-15).
 * Muestra citas pendientes y permite navegar al detalle del animal.
 */
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

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Citas de Adopción Programadas");
        }

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();

        // Enlazar vistas
        recyclerView = findViewById(R.id.recycler_citas);
        progressBar = findViewById(R.id.progress_bar_citas);
        tvEmpty = findViewById(R.id.tv_empty_citas);

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CitasAdapter(this, this);
        recyclerView.setAdapter(adapter);

        // Cargar citas
        cargarCitasProgramadas();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /**
     * Carga todas las solicitudes con citas agendadas, ordenadas por fecha.
     */
    private void cargarCitasProgramadas() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        // Consulta: Solicitudes con estado "Cita Agendada" y fecha de cita no nula
        Query query = db.collection("solicitudes_adopcion")
                .whereEqualTo("estadoSolicitud", "Cita Agendada")
                .orderBy("fechaCita", Query.Direction.ASCENDING);

        listenerRegistration = query.addSnapshotListener((snapshots, e) -> {
            progressBar.setVisibility(View.GONE);

            if (e != null) {
                Log.w(TAG, "Error al escuchar citas:", e);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Error al cargar citas: " + e.getMessage());
                return;
            }

            if (snapshots != null) {
                List<SolicitudAdopcion> citas = new ArrayList<>();
                Date ahora = new Date();

                for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                    SolicitudAdopcion solicitud = doc.toObject(SolicitudAdopcion.class);
                    if (solicitud != null && solicitud.getFechaCita() != null) {
                        solicitud.setIdSolicitud(doc.getId());

                        // Solo mostrar citas futuras o del día actual
                        Date fechaCita = solicitud.getFechaCita().toDate();
                        if (!fechaCita.before(ahora)) {
                            citas.add(solicitud);
                        }
                    }
                }

                adapter.setCitasList(citas);

                if (citas.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("No hay citas programadas próximamente");
                    recyclerView.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onCitaClick(SolicitudAdopcion solicitud) {
        // Navegar al detalle del animal
        if (solicitud.getIdAnimal() != null) {
            android.content.Intent intent = new android.content.Intent(this, DetalleAnimalActivity.class);
            intent.putExtra("animalId", solicitud.getIdAnimal());
            startActivity(intent);
        } else {
            Toast.makeText(this, "Error: ID de animal no disponible", Toast.LENGTH_SHORT).show();
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