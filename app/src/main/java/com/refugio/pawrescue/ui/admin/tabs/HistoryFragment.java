package com.refugio.pawrescue.ui.admin.tabs;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.HistorialMedico;
import com.refugio.pawrescue.ui.adapter.HistoryAdapter;
import com.refugio.pawrescue.ui.admin.RegistroEventoMedicoActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Fragmento que muestra el Historial Médico y de Cuidados (RF-09, RF-10).
 */
public class HistoryFragment extends Fragment
        implements HistoryAdapter.HistoryActionListener {

    private static final String TAG = "HistoryFragment";
    private static final String ARG_ANIMAL_ID = "animal_id";
    private String animalId;

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
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
        FloatingActionButton fabAddEvento = view.findViewById(R.id.fab_add_evento);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HistoryAdapter(getContext(), this);
        recyclerView.setAdapter(adapter);

        fabAddEvento.setOnClickListener(v -> abrirRegistroEvento());

        cargarHistorial();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
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

        // Se usa addSnapshotListener para actualizar la lista en tiempo real
        db.collection("animales").document(animalId)
                .collection("historialMedico")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    progressBar.setVisibility(View.GONE);
                    if (e != null) {
                        Log.w(TAG, "Error al escuchar historial:", e);
                        tvEmpty.setText("Error al cargar historial: " + e.getMessage());
                        tvEmpty.setVisibility(View.VISIBLE);
                        return;
                    }

                    List<HistorialMedico> historial = new ArrayList<>();
                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            HistorialMedico evento = doc.toObject(HistorialMedico.class);
                            if (evento != null) {
                                evento.setIdRegistro(doc.getId()); // <<-- CRUCIAL: Capturar el ID del documento
                                historial.add(evento);
                            }
                        }
                    }

                    if (historial.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText("No hay eventos médicos registrados.\n\nUsa el botón '+' para agregar uno.");
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        adapter.setHistorial(historial);
                        recyclerView.setVisibility(View.VISIBLE);
                        tvEmpty.setVisibility(View.GONE);
                    }
                });
    }

    // =========================================================================
    // IMPLEMENTACIÓN DE HistoryActionListener
    // =========================================================================

    @Override
    public void onItemClick(HistorialMedico evento) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Detalles: " + evento.getTipoEvento());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_historial_detalle, null);
        builder.setView(dialogView);

        // Formateador de fecha
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy HH:mm", Locale.getDefault());

        // Referencias a los TextViews del diálogo
        TextView tvTipo = dialogView.findViewById(R.id.tv_dialog_tipo);
        TextView tvFecha = dialogView.findViewById(R.id.tv_dialog_fecha);
        TextView tvVeterinario = dialogView.findViewById(R.id.tv_dialog_veterinario);
        TextView tvDiagnostico = dialogView.findViewById(R.id.tv_dialog_diagnostico);
        TextView tvTratamiento = dialogView.findViewById(R.id.tv_dialog_tratamiento);
        TextView tvNotas = dialogView.findViewById(R.id.tv_dialog_notas);

        // Llenado de datos (uso de ternario para manejar posibles nulos o textos vacíos)
        tvTipo.setText(evento.getTipoEvento());

        if (evento.getFecha() != null) {
            tvFecha.setText("Fecha: " + sdf.format(evento.getFecha().toDate()));
        } else {
            tvFecha.setText("Fecha: N/A");
        }

        String veterinarioText = evento.getVeterinario() != null ? evento.getVeterinario() : "N/A";
        String diagnosticoText = evento.getDiagnostico() != null ? evento.getDiagnostico() : "No registrado";
        String tratamientoText = evento.getTratamiento() != null ? evento.getTratamiento() : "No registrado";
        String notasText = evento.getNotas() != null ? evento.getNotas() : "Sin notas adicionales";

        tvVeterinario.setText("Dr(a).: " + veterinarioText);
        tvDiagnostico.setText(diagnosticoText);
        tvTratamiento.setText(tratamientoText);
        tvNotas.setText(notasText);

        builder.setPositiveButton("Cerrar", null);
        builder.show();
    }

    @Override
    public void onDeleteClick(HistorialMedico evento) {
        if (getContext() == null) return;

        // Pedir confirmación antes de eliminar permanentemente
        new AlertDialog.Builder(getContext())
                .setTitle("Confirmar Eliminación")
                .setMessage("¿Estás seguro de que deseas eliminar permanentemente el registro médico: " + evento.getTipoEvento() + " (" + evento.getVeterinario() + ")?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarEventoMedico(evento))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarEventoMedico(HistorialMedico evento) {
        if (animalId == null || evento.getIdRegistro() == null) {
            Toast.makeText(getContext(), "Error: No se pudo obtener la referencia del evento para eliminar.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("animales")
                .document(animalId)
                .collection("historialMedico")
                .document(evento.getIdRegistro()) // USAR idRegistro, el ID del documento
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "✅ Evento eliminado correctamente.", Toast.LENGTH_SHORT).show();
                    // La recarga es automática gracias al SnapshotListener en cargarHistorial()
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "❌ Error al eliminar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error eliminando evento:", e);
                });
    }
}