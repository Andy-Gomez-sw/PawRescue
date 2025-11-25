package com.refugio.pawrescue.ui.volunteer;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;


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
     * Usamos solo los campos reales de Firestore y evitamos índices compuestos.
     */
    private void cargarCitasProgramadas() {
        if (animalId == null || animalId.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("No se encontró el ID del animal.");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        // 🔹 Filtramos por animal y por solicitudes "Aprobadas".
        // (la cita está en el campo fechaCita)
        Query query = db.collection("solicitudes_adopcion")
                .whereEqualTo("idAnimal", animalId)
                .whereEqualTo("estadoSolicitud", "Aprobada");

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
                SolicitudAdopcion cita = doc.toObject(SolicitudAdopcion.class);
                if (cita != null) {
                    cita.setIdSolicitud(doc.getId()); // Ya lo usas en otros lados
                    citas.add(cita);
                }
            });

            // 🔹 Ordenamos en memoria por fechaCita (más próxima primero)
            Collections.sort(citas, new Comparator<SolicitudAdopcion>() {
                @Override
                public int compare(SolicitudAdopcion c1, SolicitudAdopcion c2) {
                    Timestamp t1 = c1.getFechaCita();
                    Timestamp t2 = c2.getFechaCita();
                    if (t1 == null && t2 == null) return 0;
                    if (t1 == null) return 1;
                    if (t2 == null) return -1;
                    return t1.compareTo(t2);
                }
            });

            adapter.setCitasList(citas);
            tvEmpty.setVisibility(View.GONE);
        });
    }

    /**
     * Cuando el voluntario toca una cita:
     * - Ve fecha y hora
     * - Ve datos básicos del interesado
     * - Puede marcar al animal como "Listo para adoptar"
     *   o simplemente cerrar.
     */
    @Override
    public void onCitaClick(SolicitudAdopcion cita) {
        if (getContext() == null || cita == null) return;

        Intent intent = new Intent(getContext(), VolunteerCitaDetailActivity.class);
        intent.putExtra("idSolicitud", cita.getIdSolicitud());
        intent.putExtra("idAnimal", animalId);
        startActivity(intent);

    }






    private String valueOrDash(String v) {
        return v == null || v.trim().isEmpty() ? "-" : v;
    }

    /**
     * Marca el animal como "Listo para adoptar" usando
     * el campo estadoRefugio y fechaUltimaActualizacion.
     */
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
