package com.refugio.pawrescue.ui.voluntario;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.Cita;
import com.refugio.pawrescue.ui.adapter.VolunteerAppointmentAdapter;
import com.refugio.pawrescue.ui.volunteer.VolunteerAppointmentActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Actividad para que los VOLUNTARIOS vean sus citas asignadas
 */
public class VolunteerMyAppointmentsActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private RecyclerView rvAppointments;
    private TextView tvEmptyState;
    private VolunteerAppointmentAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private List<Cita> citasList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_my_appointments);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews();
        setupTabs();
        setupRecyclerView();
        loadMisCitas("asignada"); // Por defecto: citas pendientes
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout);
        rvAppointments = findViewById(R.id.rvAppointments);
        tvEmptyState = findViewById(R.id.tvEmptyState);
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Pendientes"));
        tabLayout.addTab(tabLayout.newTab().setText("Completadas"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    loadMisCitas("asignada");
                } else {
                    loadMisCitas("completada");
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new VolunteerAppointmentAdapter(citasList, cita -> {
            // Si la cita está asignada (pendiente), abrir para crear reporte
            if ("asignada".equals(cita.getEstado())) {
                Intent intent = new Intent(this, VolunteerAppointmentActivity.class);
                intent.putExtra("CITA_ID", cita.getId());
                startActivity(intent);
            } else {
                // Si ya está completada, ver reporte (opcional)
                Toast.makeText(this, "Reporte ya enviado", Toast.LENGTH_SHORT).show();
            }
        });

        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        rvAppointments.setAdapter(adapter);
    }

    /**
     * Carga las citas asignadas a este voluntario
     */
    private void loadMisCitas(String estado) {
        String voluntarioId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (voluntarioId == null) {
            Toast.makeText(this, "Error de sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        citasList.clear();

        db.collection("citas")
                .whereEqualTo("voluntarioAsignado", voluntarioId)
                .whereEqualTo("estado", estado)
                .orderBy("fecha")
                .orderBy("hora")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Cita cita = doc.toObject(Cita.class);
                        cita.setId(doc.getId());
                        citasList.add(cita);
                    }

                    adapter.notifyDataSetChanged();

                    if (citasList.isEmpty()) {
                        showEmptyState(true);
                    } else {
                        showEmptyState(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showEmptyState(true);
                });
    }

    private void showEmptyState(boolean isEmpty) {
        if (isEmpty) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvAppointments.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvAppointments.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar citas al volver (en caso de que se haya completado una)
        int currentTab = tabLayout.getSelectedTabPosition();
        if (currentTab == 0) {
            loadMisCitas("asignada");
        } else {
            loadMisCitas("completada");
        }
    }
}