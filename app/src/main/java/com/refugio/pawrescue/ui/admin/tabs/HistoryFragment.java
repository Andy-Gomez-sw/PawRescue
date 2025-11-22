package com.refugio.pawrescue.ui.admin.tabs;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.HistorialMedico;
import com.refugio.pawrescue.ui.adapter.HistoryAdapter;
import com.refugio.pawrescue.ui.admin.RegistroEventoMedicoActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragmento que muestra el Historial Médico y de Cuidados (RF-09, RF-10).
 */
public class HistoryFragment extends Fragment {

    private static final String TAG = "HistoryFragment";
    private static final String ARG_ANIMAL_ID = "animal_id";
    private String animalId;

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private FloatingActionButton fabAddEvento;
    private FirebaseFirestore db;

    public static HistoryFragment newInstance(String animalId) {
        HistoryFragment fragment = new HistoryFragment();
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.recycler_historial);
        progressBar = view.findViewById(R.id.progress_bar_historial);
        tvEmpty = view.findViewById(R.id.tv_empty_historial);
        fabAddEvento = view.findViewById(R.id.fab_add_evento);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HistoryAdapter(getContext());
        recyclerView.setAdapter(adapter);

        // Listener para agregar nuevo evento médico (RF-09)
        fabAddEvento.setOnClickListener(v -> abrirRegistroEvento());

        cargarHistorial();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recargar al volver de registrar un evento
        cargarHistorial();
    }

    private void abrirRegistroEvento() {
        Intent intent = new Intent(getActivity(), RegistroEventoMedicoActivity.class);
        intent.putExtra("animalId", animalId);
        startActivity(intent);
    }

    private void cargarHistorial() {
        if (animalId == null) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Error: ID de animal no disponible.");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        db.collection("animales").document(animalId)
                .collection("historialMedico")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);

                    List<HistorialMedico> historial = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        HistorialMedico evento = doc.toObject(HistorialMedico.class);
                        historial.add(evento);
                    }

                    if (historial.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText("No hay eventos médicos registrados.\n\nUsa el botón '+' para agregar uno.");
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        adapter.setHistorial(historial);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Error al cargar historial: " + e.getMessage());
                    Log.e(TAG, "Error al cargar historial: ", e);
                });
    }
}