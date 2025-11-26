package com.refugio.pawrescue.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.Cita;
import com.refugio.pawrescue.ui.adapter.AdminAppointmentAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Actividad del ADMIN para:
 * 1. Ver todas las citas agendadas por usuarios
 * 2. Asignar voluntarios a las citas
 * 3. Ver reportes de citas completadas
 */
public class AdminAppointmentsActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private RecyclerView rvAppointments;
    private AdminAppointmentAdapter adapter;
    private FirebaseFirestore db;

    private List<Cita> citasList = new ArrayList<>();
    private List<String> voluntariosList = new ArrayList<>();
    private Map<String, String> voluntariosMap = new HashMap<>(); // ID -> Nombre

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_appointments);

        db = FirebaseFirestore.getInstance();

        initViews();
        setupTabs();
        setupRecyclerView();
        loadVoluntarios();
        loadCitas("pendiente_asignacion"); // Por defecto: citas sin asignar
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout);
        rvAppointments = findViewById(R.id.rvAppointments);
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Pendientes"));
        tabLayout.addTab(tabLayout.newTab().setText("Asignadas"));
        tabLayout.addTab(tabLayout.newTab().setText("Completadas"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        loadCitas("pendiente_asignacion");
                        break;
                    case 1:
                        loadCitas("asignada");
                        break;
                    case 2:
                        loadCitas("completada");
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new AdminAppointmentAdapter(citasList, cita -> {
            if ("pendiente_asignacion".equals(cita.getEstado())) {
                showAsignarVoluntarioDialog(cita);
            } else if ("completada".equals(cita.getEstado()) && cita.isReporteCompleto()) {
                verReporte(cita);
            }
        });

        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        rvAppointments.setAdapter(adapter);
    }

    /**
     * Carga la lista de voluntarios disponibles desde Firestore
     */
    private void loadVoluntarios() {
        db.collection("usuarios")
                .whereEqualTo("rol", "Voluntario")
                .whereEqualTo("estadoActivo", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    voluntariosList.clear();
                    voluntariosMap.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String id = doc.getId();
                        String nombre = doc.getString("nombre");
                        if (nombre != null) {
                            voluntariosList.add(nombre);
                            voluntariosMap.put(nombre, id);
                        }
                    }

                    if (voluntariosList.isEmpty()) {
                        Toast.makeText(this, "⚠️ No hay voluntarios activos", Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Carga citas según el estado
     */
    private void loadCitas(String estado) {
        citasList.clear();

        db.collection("citas")
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
                        Toast.makeText(this, "No hay citas " + estado.replace("_", " "), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Muestra diálogo para asignar un voluntario a la cita
     */
    private void showAsignarVoluntarioDialog(Cita cita) {
        if (voluntariosList.isEmpty()) {
            Toast.makeText(this, "No hay voluntarios disponibles", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_asignar_voluntario, null);
        Spinner spinnerVoluntarios = dialogView.findViewById(R.id.spinnerVoluntarios);
        MaterialButton btnAsignar = dialogView.findViewById(R.id.btnAsignar);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                voluntariosList
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVoluntarios.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Asignar Voluntario")
                .setView(dialogView)
                .setCancelable(true)
                .create();

        MaterialButton btnCancelar = dialogView.findViewById(R.id.btnCancelar);

        btnAsignar.setOnClickListener(v -> {
            String voluntarioNombre = spinnerVoluntarios.getSelectedItem().toString();
            String voluntarioId = voluntariosMap.get(voluntarioNombre);

            if (voluntarioId != null) {
                asignarVoluntario(cita, voluntarioId, voluntarioNombre);
                dialog.dismiss();
            }
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * Asigna el voluntario y cambia el estado de la cita
     */
    private void asignarVoluntario(Cita cita, String voluntarioId, String voluntarioNombre) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("voluntarioAsignado", voluntarioId);
        updates.put("voluntarioNombre", voluntarioNombre);
        updates.put("estado", "asignada");

        db.collection("citas")
                .document(cita.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "✅ Voluntario asignado: " + voluntarioNombre, Toast.LENGTH_SHORT).show();
                    loadCitas("pendiente_asignacion"); // Recargar lista
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Ver el reporte completo de una cita completada
     */
    private void verReporte(Cita cita) {
        if (cita.getReporteId() == null || cita.getReporteId().isEmpty()) {
            Toast.makeText(this, "Reporte no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, AdminReportDetailActivity.class);
        intent.putExtra("CITA_ID", cita.getId());
        intent.putExtra("REPORTE_ID", cita.getReporteId());
        startActivity(intent);
    }
}