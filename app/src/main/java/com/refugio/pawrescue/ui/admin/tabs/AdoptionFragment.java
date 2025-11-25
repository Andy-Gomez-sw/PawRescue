package com.refugio.pawrescue.ui.admin.tabs;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.SolicitudAdopcion;
import com.refugio.pawrescue.ui.adapter.SolicitudAdopcionAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdoptionFragment extends Fragment implements SolicitudAdopcionAdapter.SolicitudInteractionListener {

    private static final String TAG = "AdoptionFragment";
    private static final String ARG_ANIMAL_ID = "animal_id";
    private String animalId;

    private RecyclerView recyclerView;
    private SolicitudAdopcionAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty, tvAnimalIdHint;

    private FirebaseFirestore db;
    private ListenerRegistration listenerRegistration;

    public static AdoptionFragment newInstance(String animalId) {
        AdoptionFragment fragment = new AdoptionFragment();
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Usamos el layout que ya tienes (fragment_adoption)
        View view = inflater.inflate(R.layout.fragment_adoption, container, false);

        recyclerView = view.findViewById(R.id.recycler_solicitudes);
        progressBar = view.findViewById(R.id.progress_bar_adoption);
        tvEmpty = view.findViewById(R.id.tv_empty_requests);
        tvAnimalIdHint = view.findViewById(R.id.tv_animal_id_hint);

        if (animalId != null && tvAnimalIdHint != null) {
            tvAnimalIdHint.setText("Solicitudes de adopción:");
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Inicializamos el adaptador pasando 'this' como listener
        adapter = new SolicitudAdopcionAdapter(getContext(), new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        cargarSolicitudes();

        return view;
    }

    private void cargarSolicitudes() {
        if (animalId == null) {
            if(tvEmpty != null) {
                tvEmpty.setText("Error: No se recibió ID del animal.");
                tvEmpty.setVisibility(View.VISIBLE);
            }
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        if(tvEmpty != null) tvEmpty.setVisibility(View.GONE);

        // Consulta a la colección correcta "solicitudes_adopcion"
        Query query = db.collection("solicitudes_adopcion")
                .whereEqualTo("animalId", animalId)
                .orderBy("fechaSolicitud", Query.Direction.DESCENDING);

        listenerRegistration = query.addSnapshotListener((snapshots, e) -> {
            if (progressBar != null) progressBar.setVisibility(View.GONE);

            if (e != null) {
                Log.e(TAG, "Error cargando solicitudes", e);
                if(tvEmpty != null) {
                    tvEmpty.setText("Error de carga: " + e.getMessage());
                    tvEmpty.setVisibility(View.VISIBLE);
                }
                return;
            }

            if (snapshots != null) {
                List<SolicitudAdopcion> lista = new ArrayList<>();
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    try {
                        SolicitudAdopcion sol = doc.toObject(SolicitudAdopcion.class);
                        if (sol != null) {
                            sol.setIdSolicitud(doc.getId());

                            // --- PARCHE DE COMPATIBILIDAD (Lectura segura) ---
                            // Si falta el nombre, lo buscamos manualmente
                            if ((sol.getNombreAdoptante() == null || sol.getNombreAdoptante().equals("Usuario Desconocido")) && doc.contains("nombreCompleto")) {
                                sol.setNombreCompleto(doc.getString("nombreCompleto"));
                            }
                            // Si falta el teléfono
                            if ((sol.getTelefonoAdoptante() == null || sol.getTelefonoAdoptante().equals("Sin teléfono")) && doc.contains("telefono")) {
                                sol.setTelefono(doc.getString("telefono"));
                            }
                            // Si falta el estado
                            if (sol.getEstado() == null && doc.contains("estado")) {
                                sol.setEstado(doc.getString("estado"));
                            }

                            lista.add(sol);
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "Error parseando doc: " + doc.getId(), ex);
                    }
                }

                adapter.setListaSolicitudes(lista);

                if(tvEmpty != null) {
                    if (lista.isEmpty()) {
                        tvEmpty.setText("No hay solicitudes para este animal.");
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    // --- INTERFAZ: Cuando tocas una tarjeta ---
    @Override
    public void onSolicitudClick(SolicitudAdopcion solicitud) {
        // Menú simple
        String[] opciones = {"📅 Agendar Cita", "✅ Aprobar / ❌ Rechazar"};

        new AlertDialog.Builder(getContext())
                .setTitle("Gestionar Solicitud")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        mostrarDialogoCita(solicitud);
                    } else {
                        mostrarDialogoResultadoSinXML(solicitud);
                    }
                })
                .show();
    }

    // --- Lógica de Cita (DatePicker simple) ---
    private void mostrarDialogoCita(SolicitudAdopcion solicitud) {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    c.set(year, month, dayOfMonth);
                    // Actualizamos estado a "cita_agendada"
                    actualizarEstadoSolicitud(solicitud, "cita_agendada", new Timestamp(c.getTime()));
                },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        datePicker.setTitle("Fecha de la Cita");
        datePicker.show();
    }

    // --- Lógica de Resultado (SIN XML EXTRA) ---
    private void mostrarDialogoResultadoSinXML(SolicitudAdopcion solicitud) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Dictamen Final");
        builder.setMessage("¿Deseas aprobar o rechazar esta solicitud?");

        // Creamos un campo de texto programáticamente
        final EditText input = new EditText(getContext());
        input.setHint("Comentarios opcionales...");

        // Le damos un poco de margen al EditText
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        input.setLayoutParams(lp);

        // Contenedor para margenes
        LinearLayout container = new LinearLayout(getContext());
        container.setPadding(50, 20, 50, 20);
        container.addView(input);

        builder.setView(container);

        // Botón APROBAR (Positivo)
        builder.setPositiveButton("APROBAR", (dialog, which) -> {
            actualizarEstadoSolicitud(solicitud, "aprobada", null);
            marcarAnimalAdoptado(); // Cambia el estado del animal
        });

        // Botón RECHAZAR (Negativo)
        builder.setNegativeButton("RECHAZAR", (dialog, which) -> {
            actualizarEstadoSolicitud(solicitud, "rechazada", null);
        });

        // Botón CANCELAR (Neutral)
        builder.setNeutralButton("Cancelar", null);

        builder.show();
    }

    private void actualizarEstadoSolicitud(SolicitudAdopcion solicitud, String nuevoEstado, @Nullable Timestamp fechaCita) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", nuevoEstado); // Para la app pública
        updates.put("estadoSolicitud", nuevoEstado); // Para compatibilidad admin

        if (fechaCita != null) {
            updates.put("fechaCita", fechaCita);
        }

        db.collection("solicitudes_adopcion").document(solicitud.getIdSolicitud())
                .update(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Estado actualizado a: " + nuevoEstado, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al actualizar", Toast.LENGTH_SHORT).show());
    }

    private void marcarAnimalAdoptado() {
        db.collection("animales").document(animalId)
                .update("estadoRefugio", "Adoptado")
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "¡El animal ha sido marcado como ADOPTADO!", Toast.LENGTH_LONG).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}