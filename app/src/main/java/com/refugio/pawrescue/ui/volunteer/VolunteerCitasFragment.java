package com.refugio.pawrescue.ui.volunteer;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.SolicitudAdopcion;
import com.refugio.pawrescue.ui.adapter.CitasAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class VolunteerCitasFragment extends Fragment implements CitasAdapter.OnCitaClickListener {

    private static final String ARG_ANIMAL_ID = "animal_id";

    private String animalId;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private FirebaseFirestore db;
    private ListenerRegistration listenerRegistration;
    private CitasAdapter adapter;

    // Formatos (opcionales si los usas para logs o UI extra)
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("EEE dd MMM yyyy", new Locale("es", "MX"));
    private final SimpleDateFormat timeFormat =
            new SimpleDateFormat("HH:mm", new Locale("es", "MX"));

    public static VolunteerCitasFragment newInstance(String animalId) {
        VolunteerCitasFragment fragment = new VolunteerCitasFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ANIMAL_ID, animalId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            animalId = getArguments().getString(ARG_ANIMAL_ID);
        }
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_volunteer_citas, container, false);

        recyclerView = view.findViewById(R.id.recycler_citas);
        progressBar = view.findViewById(R.id.progress_citas);
        tvEmpty = view.findViewById(R.id.tv_empty_citas);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CitasAdapter(requireContext(), this);
        recyclerView.setAdapter(adapter);

        cargarCitasProgramadas();

        return view;
    }

    /**
     * Carga las citas para este animal.
     */
    private void cargarCitasProgramadas() {
        if (animalId == null || animalId.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("No se encontró el ID del animal.");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        // 🔹 Filtramos por animal.
        // OJO: Aquí filtrabas por "Aprobada", pero usualmente las citas son "cita_agendada".
        // Si no te salen datos, intenta cambiar "Aprobada" por "cita_agendada".
        Query query = db.collection("solicitudes_adopcion")
                .whereEqualTo("animalId", animalId);
        // .whereEqualTo("estadoSolicitud", "cita_agendada"); // Descomenta si quieres filtrar estado

        listenerRegistration = query.addSnapshotListener((snapshots, e) -> {
            progressBar.setVisibility(View.GONE);

            if (e != null) {
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Error al cargar citas: " + e.getMessage());
                return;
            }

            if (snapshots == null || snapshots.isEmpty()) {
                adapter.setCitasList(new ArrayList<>());
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("No hay citas programadas para este animal.");
                return;
            }

            List<SolicitudAdopcion> citas = new ArrayList<>();
            snapshots.getDocuments().forEach(doc -> {
                try {
                    SolicitudAdopcion cita = doc.toObject(SolicitudAdopcion.class);
                    if (cita != null) {
                        cita.setIdSolicitud(doc.getId());

                        // Parche de compatibilidad (como hicimos en Admin)
                        if(cita.getIdAnimal() == null) cita.setIdAnimal(doc.getString("animalId"));
                        if(cita.getNombreAnimal() == null) cita.setNombreAnimal(doc.getString("animalNombre"));

                        citas.add(cita);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            // 🔹 CORRECCIÓN AQUI: Usamos Date en lugar de Timestamp
            Collections.sort(citas, new Comparator<SolicitudAdopcion>() {
                @Override
                public int compare(SolicitudAdopcion c1, SolicitudAdopcion c2) {
                    Date d1 = c1.getFechaCita(); // Ahora devuelve Date
                    Date d2 = c2.getFechaCita(); // Ahora devuelve Date

                    if (d1 == null && d2 == null) return 0;
                    if (d1 == null) return 1;  // Nulos al final
                    if (d2 == null) return -1;

                    return d1.compareTo(d2); // Date tiene su propio compareTo
                }
            });

            adapter.setCitasList(citas);
            tvEmpty.setVisibility(View.GONE);
        });
    }

    @Override
    public void onCitaClick(SolicitudAdopcion cita) {
        if (getContext() == null || cita == null) return;

        Intent intent = new Intent(getContext(), VolunteerCitaDetailActivity.class);
        intent.putExtra("idSolicitud", cita.getIdSolicitud());
        intent.putExtra("idAnimal", animalId);
        // Pasamos el objeto completo para evitar problemas de carga
        intent.putExtra("SOLICITUD_OBJ", cita);
        startActivity(intent);
    }

    private String valueOrDash(String v) {
        return v == null || v.trim().isEmpty() ? "-" : v;
    }

    private void marcarAnimalListoParaAdoptar() {
        if (animalId == null || animalId.isEmpty() || getContext() == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("estadoRefugio", "Listo para adoptar");
        updates.put("fechaUltimaActualizacion", new Timestamp(new Date()));

        db.collection("animales").document(animalId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(),
                            "Estado actualizado a \"Listo para adoptar\"",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),
                            "Error al actualizar estado: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}