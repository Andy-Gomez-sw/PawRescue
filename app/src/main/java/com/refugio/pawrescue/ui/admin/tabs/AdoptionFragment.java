package com.refugio.pawrescue.ui.admin.tabs;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.Locale;
import java.util.Map;

/**
 * Fragmento que gestiona las Solicitudes de Adopción (RF-14, RF-15, RF-16).
 * Se muestra dentro de DetalleAnimalActivity.
 */
public class AdoptionFragment extends Fragment implements SolicitudAdopcionAdapter.SolicitudInteractionListener {

    private static final String TAG = "AdoptionFragment";
    private static final String ARG_ANIMAL_ID = "animal_id";
    private String animalId;

    private RecyclerView recyclerView;
    private SolicitudAdopcionAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty, tvAnimalIdHint;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
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
        mAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_adoption, container, false);

        recyclerView = view.findViewById(R.id.recycler_solicitudes);
        progressBar = view.findViewById(R.id.progress_bar_adoption);
        tvEmpty = view.findViewById(R.id.tv_empty_requests);
        tvAnimalIdHint = view.findViewById(R.id.tv_animal_id_hint);

        if (animalId != null) {
            tvAnimalIdHint.setText("Gestionando Solicitudes");
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SolicitudAdopcionAdapter(getContext(), this);
        recyclerView.setAdapter(adapter);

        cargarSolicitudes();

        return view;
    }

    /**
     * Carga las solicitudes de adopción para este animal en tiempo real (RF-14).
     */
    private void cargarSolicitudes() {
        if (animalId == null) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Error: ID de animal no disponible.");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        // Consulta filtrada por el idAnimal actual
        Query query = db.collection("solicitudes_adopcion")
                .whereEqualTo("idAnimal", animalId)
                .orderBy("fechaSolicitud", Query.Direction.DESCENDING);

        listenerRegistration = query.addSnapshotListener((snapshots, e) -> {
            progressBar.setVisibility(View.GONE);
            if (e != null) {
                Log.w(TAG, "Error al escuchar solicitudes:", e);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Error al cargar solicitudes: " + e.getMessage());
                return;
            }

            if (snapshots != null) {
                List<SolicitudAdopcion> solicitudes = new ArrayList<>();
                for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                    SolicitudAdopcion solicitud = doc.toObject(SolicitudAdopcion.class);
                    if (solicitud != null) {
                        solicitud.setIdSolicitud(doc.getId());
                        solicitudes.add(solicitud);
                    }
                }

                adapter.setSolicitudList(solicitudes);
                tvEmpty.setVisibility(solicitudes.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(solicitudes.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });
    }

    // --- Implementación de RF-15: Agendar Cita ---
    @Override
    public void onAgendarCitaClick(SolicitudAdopcion solicitud) {
        if (getContext() == null) return;

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Mostrar selector de fecha
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Cita agendada, ahora actualizar el estado de la solicitud
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay, 0, 0, 0);
                    selectedDate.set(Calendar.MILLISECOND, 0);

                    // Aquí solo guardamos la fecha por simplicidad de UI, en real se usaría un TimePicker también.
                    actualizarEstadoSolicitud(solicitud, "Cita Agendada", new Timestamp(selectedDate.getTime()));
                }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000); // Evitar fechas pasadas
        datePickerDialog.setTitle("Seleccione Fecha de Cita (RF-15)");
        datePickerDialog.show();
    }


    // --- Implementación de RF-16: Registrar Resultado ---
    @Override
    public void onRegistrarResultadoClick(SolicitudAdopcion solicitud) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Registrar Resultado de Adopción (RF-16)");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_adoption_result, null);
        builder.setView(dialogView);

        Spinner spinnerResultado = dialogView.findViewById(R.id.spinner_resultado);
        EditText etComentarios = dialogView.findViewById(R.id.et_comentarios_resultado);

        // Configurar el Spinner de Resultados
        ArrayAdapter<CharSequence> adapterSpinner = ArrayAdapter.createFromResource(
                getContext(), R.array.adoption_results, android.R.layout.simple_spinner_dropdown_item);
        spinnerResultado.setAdapter(adapterSpinner);

        builder.setPositiveButton("Confirmar", (dialog, which) -> {
            String resultado = spinnerResultado.getSelectedItem().toString();
            String comentarios = etComentarios.getText().toString().trim();

            if ("Seleccionar".equals(resultado)) {
                Toast.makeText(getContext(), "Debe seleccionar un resultado válido.", Toast.LENGTH_SHORT).show();
                return;
            }

            actualizarResultadoAdopcion(solicitud, resultado, comentarios);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * Actualiza el estado de la solicitud en Firestore (RF-15, RF-16).
     */
    private void actualizarEstadoSolicitud(SolicitudAdopcion solicitud, String nuevoEstado, @Nullable Timestamp fechaCita) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("estadoSolicitud", nuevoEstado);

        if (fechaCita != null) {
            updates.put("fechaCita", fechaCita);
        }

        db.collection("solicitudes_adopcion").document(solicitud.getIdSolicitud())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "✅ Solicitud actualizada a: " + nuevoEstado, Toast.LENGTH_SHORT).show();
                    // Si se agendó, no hay más acción inmediata, el coordinador debe monitorear la cita.
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "❌ Error al actualizar estado: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error actualizando estado de solicitud: ", e);
                });
    }

    /**
     * Procesa el resultado de la adopción y realiza el cambio de estado en el animal si es aprobado (RF-16).
     */
    private void actualizarResultadoAdopcion(SolicitudAdopcion solicitud, String resultado, String comentarios) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("estadoSolicitud", resultado);
        updates.put("comentariosResultado", comentarios);
        updates.put("fechaResultado", new Timestamp(new Date()));

        // 1. Actualizar la solicitud
        db.collection("solicitudes_adopcion").document(solicitud.getIdSolicitud())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "✅ Resultado registrado: " + resultado, Toast.LENGTH_SHORT).show();

                    // 2. Si se APROBÓ, cambiar el estado del ANIMAL a "Adoptado" (RF-16)
                    if ("Aprobada".equals(resultado)) {
                        cambiarEstadoAnimalAAdoptado(solicitud.getIdAnimal());
                    }
                    // Si fue Rechazada, el animal sigue disponible para otras solicitudes
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "❌ Error al registrar resultado: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error registrando resultado de adopción: ", e);
                });
    }

    /**
     * Cambia el estado del animal en la colección principal (RF-16).
     */
    private void cambiarEstadoAnimalAAdoptado(String animalId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("estadoRefugio", "Adoptado");
        updates.put("fechaAdopcion", new Timestamp(new Date()));

        db.collection("animales").document(animalId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Animal actualizado a estado: Adoptado.", Toast.LENGTH_LONG).show();
                    // Opcional: Iniciar el flujo de Seguimiento Post-Adopción (RF-17)
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error crítico al actualizar estado del animal a Adoptado.", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error cambiando estado animal: ", e);
                });
    }


    // --- RF-17: Placeholder para Seguimiento Post-Adopción (Se desarrollaría en una Activity separada) ---
    private void iniciarSeguimientoPostAdopcion(SolicitudAdopcion solicitud) {
        Toast.makeText(getContext(), "Iniciando Seguimiento Post-Adopción (RF-17) para el adoptante: " + solicitud.getNombreAdoptante(), Toast.LENGTH_LONG).show();
        // Implementación futura: Navegar a una nueva Activity para registrar comentarios y fotos.
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}