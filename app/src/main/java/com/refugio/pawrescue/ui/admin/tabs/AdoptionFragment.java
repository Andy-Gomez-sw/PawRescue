package com.refugio.pawrescue.ui.admin.tabs;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.data.helper.FirebaseHelper;
import com.refugio.pawrescue.model.ReporteCita;
import com.refugio.pawrescue.model.SolicitudAdopcion;
import com.refugio.pawrescue.model.Usuario;
import com.refugio.pawrescue.ui.adapter.SolicitudAdopcionAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

    private List<Usuario> voluntariosDisponibles = new ArrayList<>();
    private Map<String, String> voluntariosMap = new HashMap<>();


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

        cargarVoluntariosDisponibles();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_adoption, container, false);

        recyclerView = view.findViewById(R.id.recycler_solicitudes);
        progressBar = view.findViewById(R.id.progress_bar_adoption);
        tvEmpty = view.findViewById(R.id.tv_empty_requests);
        tvAnimalIdHint = view.findViewById(R.id.tv_animal_id_hint);

        if (animalId != null && tvAnimalIdHint != null) {
            tvAnimalIdHint.setText("Solicitudes de adopción:");
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SolicitudAdopcionAdapter(getContext(), new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        cargarSolicitudes();

        return view;
    }

    private void cargarVoluntariosDisponibles() {
        if (db == null) return;
        db.collection("usuarios")
                .whereEqualTo("rol", "Voluntario")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    voluntariosDisponibles.clear();
                    voluntariosMap.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        try {
                            Usuario usuario = doc.toObject(Usuario.class);
                            if (usuario != null) {
                                usuario.setUid(doc.getId());
                                voluntariosDisponibles.add(usuario);
                                voluntariosMap.put(usuario.getUid(), usuario.getNombre());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error al parsear documento de usuario: " + doc.getId(), e);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error cargando voluntarios: ", e));
    }


    // 🚨 IMPLEMENTACIÓN MODIFICADA: Ahora espera por la sincronización de reporteId
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

        Query query = db.collection("solicitudes_adopcion")
                .whereEqualTo("animalId", animalId)
                .orderBy("fechaSolicitud", Query.Direction.DESCENDING);

        listenerRegistration = query.addSnapshotListener((snapshots, e) -> {
            if (progressBar != null) progressBar.setVisibility(View.GONE);

            if (e != null) {
                Log.e(TAG, "Error cargando solicitudes", e);
                // ... (Manejo de error) ...
                return;
            }

            if (snapshots != null) {
                List<SolicitudAdopcion> lista = new ArrayList<>();
                List<DocumentSnapshot> documentos = snapshots.getDocuments();
                final int totalDocs = documentos.size();

                if (totalDocs == 0) {
                    adapter.setListaSolicitudes(lista);
                    if(tvEmpty != null) {
                        tvEmpty.setText("No hay solicitudes para este animal.");
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                    return;
                }

                final int[] contadorCompletado = {0};

                for (DocumentSnapshot doc : documentos) {
                    try {
                        SolicitudAdopcion sol = doc.toObject(SolicitudAdopcion.class);
                        if (sol != null) {
                            sol.setIdSolicitud(doc.getId());

                            // Carga síncrona de campos (Misma lógica que proporcionaste)
                            sol.setCitaId(doc.contains("citaId") ? doc.getString("citaId") : null);
                            sol.setReporteId(doc.contains("reporteId") ? doc.getString("reporteId") : null);
                            sol.setVoluntarioId(doc.contains("voluntarioId") ? doc.getString("voluntarioId") : null);
                            sol.setVoluntarioNombre(doc.contains("voluntarioNombre") ? doc.getString("voluntarioNombre") : null);
                            if (doc.contains("datosPersonales")) sol.setDatosPersonales((Map<String, Object>) doc.get("datosPersonales"));
                            if (doc.contains("datosFamilia")) sol.setDatosFamilia((Map<String, Object>) doc.get("datosFamilia"));
                            if (doc.contains("datosExperiencia")) sol.setDatosExperiencia((Map<String, Object>) doc.get("datosExperiencia"));
                            if (doc.contains("datosCompromiso")) sol.setDatosCompromiso((Map<String, Object>) doc.get("datosCompromiso"));
                            if (sol.getNombreCompleto() == null && doc.contains("nombreCompleto")) {
                                sol.setNombreCompleto(doc.getString("nombreCompleto"));
                            }
                            if (sol.getTelefono() == null && doc.contains("telefono")) {
                                sol.setTelefono(doc.getString("telefono"));
                            }
                            if (sol.getEstado() == null && doc.contains("estado")) {
                                sol.setEstado(doc.getString("estado"));
                            }

                            lista.add(sol);

                            // 🚨 LÓGICA DE SINCRONIZACIÓN ASÍNCRONA: Si no tiene reporteId pero sí tiene citaId
                            if (sol.getReporteId() == null && sol.getCitaId() != null) {
                                sincronizarReporteId(sol, () -> {
                                    contadorCompletado[0]++;
                                    if (contadorCompletado[0] == totalDocs) {
                                        adapter.setListaSolicitudes(lista);
                                        if(tvEmpty != null) tvEmpty.setVisibility(View.GONE);
                                    }
                                });
                            } else {
                                // Si tiene reporteId O no necesita sincronización, avanza el contador de inmediato
                                contadorCompletado[0]++;
                                if (contadorCompletado[0] == totalDocs) {
                                    adapter.setListaSolicitudes(lista);
                                    if(tvEmpty != null) tvEmpty.setVisibility(View.GONE);
                                }
                            }

                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "Error parseando doc: " + doc.getId(), ex);
                        contadorCompletado[0]++;
                        if (contadorCompletado[0] == totalDocs) {
                            adapter.setListaSolicitudes(lista);
                        }
                    }
                }
            }
        });
    }

    /**
     * 🚨 NUEVA FUNCIÓN: Busca el reporteId en la colección 'citas' y lo asigna localmente a la solicitud.
     */
    private void sincronizarReporteId(SolicitudAdopcion solicitud, Runnable callback) {
        String citaId = solicitud.getCitaId();

        db.collection("citas").document(citaId).get()
                .addOnSuccessListener(citaDoc -> {
                    String reporteIdEnCita = citaDoc.getString("reporteId");

                    if (reporteIdEnCita != null) {
                        // Si la cita tiene el reporteId, lo asignamos localmente.
                        solicitud.setReporteId(reporteIdEnCita);
                        Log.d(TAG, "Sincronizado reporteId: " + reporteIdEnCita + " en solicitud: " + solicitud.getIdSolicitud());
                    }
                    callback.run(); // Ejecuta el siguiente paso (avanza el contador)
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error sincronizando reporteId desde cita: " + e.getMessage());
                    callback.run(); // Ejecuta el callback en caso de error para no bloquear
                });
    }

    // ========================================================================
    // --- LÓGICA CENTRAL DE GESTIÓN EN DIÁLOGO ---
    // (MANTENIDA LA CORRECCIÓN DE FLUJO Y ANIMAL ID)
    // ========================================================================

    @Override
    public void onSolicitudClick(SolicitudAdopcion solicitud) {
        mostrarDialogoGestionCompleta(solicitud);
    }

    private void mostrarDialogoGestionCompleta(SolicitudAdopcion solicitud) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Gestión de Solicitud: " + solicitud.getNombreAnimal());

        LinearLayout layout = buildDialogLayout(getContext(), solicitud);
        builder.setView(layout);

        // --- FASE 3: REPORTE ENVIADO (Decisión Final) ---
        if (solicitud.getReporteId() != null) {
            // El reporteId existe (gracias a la sincronización local o porque estaba en Firestore)
            cargarReporteDesdeId(solicitud, builder, layout);
            return;
        }

        // --- FASE 1 y 2: ASIGNACIÓN / ESPERA (Si llega aquí, el reporteId es null,
        // pero solo debería ser en las fases 1 y 2, no en la 3)
        // ... (Lógica de FASE 1 y 2, asumimos que filtradoYMostrarDialogo se encargará) ...

        if (solicitud.getFechaCita() != null && solicitud.getVoluntarioId() == null) {
            // FASE 1: ASIGNACIÓN DE VOLUNTARIO
            // ... (Lógica asíncrona para filtrar y asignar voluntario) ...
            return;
        }

        // Caso default (Síncrono): FASE 2 o inicial
        if (solicitud.getVoluntarioId() != null) {
            builder.setNeutralButton("Cerrar", null); // FASE 2: Voluntario asignado, esperando que el Voluntario lo sincronice en Firestore
        } else {
            builder.setNeutralButton("Cerrar", null);
        }

        builder.show();
    }

    /**
     * Busca el reporte en 'reportes_citas' usando el reporteId.
     */
    private void cargarReporteDesdeId(SolicitudAdopcion solicitud, AlertDialog.Builder builder, LinearLayout layout) {
        db.collection("reportes_citas").document(solicitud.getReporteId()).get()
                .addOnSuccessListener(reporteDoc -> {
                    if (reporteDoc.exists()) {
                        _mostrarContenidoReporte(reporteDoc, solicitud, builder, layout);
                    } else {
                        addTextViewToLayout(layout, "Error al cargar reporte:", "Documento de reporte no encontrado.");
                        builder.setNeutralButton("Cerrar", null).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al cargar reporte: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    builder.setNeutralButton("Cerrar", null).show();
                });
    }

    /**
     * Lógica central para parsear y mostrar el contenido del reporte.
     */
    private void _mostrarContenidoReporte(DocumentSnapshot reporteDoc, SolicitudAdopcion solicitud, AlertDialog.Builder builder, LinearLayout layout) {
        ReporteCita reporte = reporteDoc.toObject(ReporteCita.class);

        if (reporte != null) {
            TextView tvReporte = new TextView(getContext());
            tvReporte.setText("\n--- REPORTE DEL VOLUNTARIO ---");
            tvReporte.setTextSize(16);
            tvReporte.setTypeface(null, android.graphics.Typeface.BOLD);
            layout.addView(tvReporte);

            String recomendacion = reporte.isRecomiendaAprobacion() ? "✅ SÍ recomienda APROBACIÓN" : "❌ NO recomienda APROBACIÓN";

            addTextViewToLayout(layout, "Recomendación Voluntario:", recomendacion);
            addTextViewToLayout(layout, "Voluntario:", solicitud.getVoluntarioNombre());
            addTextViewToLayout(layout, "INE Recibida:", String.valueOf(reporte.isIneRecibida()));
            addTextViewToLayout(layout, "Domicilio Adecuado:", String.valueOf(reporte.isDomicilioAdecuado()));
            addTextViewToLayout(layout, "Actitud Positiva:", String.valueOf(reporte.isActitudPositiva()));
            addTextViewToLayout(layout, "Observaciones:", reporte.getObservaciones());
            addTextViewToLayout(layout, "Recomendaciones:", reporte.getRecomendaciones());

            // Botones de Decisión Final
            builder.setPositiveButton("APROBAR ADOPCIÓN", (dialog, which) -> {
                mostrarDialogoAgendarEntrega(solicitud);
            });
            builder.setNegativeButton("RECHAZAR ADOPCIÓN", (dialog, which) -> {
                actualizarEstadoFinal(solicitud, "Rechazada");
            });

        } else {
            addTextViewToLayout(layout, "Error al cargar reporte:", "Reporte inválido.");
            builder.setNeutralButton("Cerrar", null);
        }
        builder.show();
    }


    /**
     * Muestra un diálogo para seleccionar la fecha y hora de la entrega del animal.
     */
    private void mostrarDialogoAgendarEntrega(SolicitudAdopcion solicitud) {
        if (getContext() == null) return;

        final Calendar calendar = Calendar.getInstance();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Agendar Entrega de " + solicitud.getNombreAnimal());

        final TextView tvFechaSeleccionada = new TextView(getContext());
        final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        tvFechaSeleccionada.setPadding(50, 30, 50, 20);
        tvFechaSeleccionada.setText("Toca para seleccionar fecha y hora de entrega");

        tvFechaSeleccionada.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                                (timeView, hourOfDay, minute) -> {
                                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    calendar.set(Calendar.MINUTE, minute);
                                    tvFechaSeleccionada.setText("Entrega agendada para: " + sdf.format(calendar.getTime()));
                                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
                        timePickerDialog.show();
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(tvFechaSeleccionada);
        builder.setView(layout);

        builder.setPositiveButton("CONFIRMAR ENTREGA", (dialog, which) -> {
            if (!tvFechaSeleccionada.getText().toString().contains("agendada para")) {
                Toast.makeText(getContext(), "Por favor, selecciona una fecha y hora de entrega.", Toast.LENGTH_LONG).show();
                return;
            }

            Date fechaEntrega = calendar.getTime();

            Map<String, Object> updates = new HashMap<>();
            updates.put("estado", "Aprobada");
            updates.put("estadoSolicitud", "Aprobada");
            updates.put("fechaEntrega", fechaEntrega);
            updates.put("fechaDictamen", new Date());

            db.collection("solicitudes_adopcion").document(solicitud.getIdSolicitud())
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        marcarAnimalAdoptado(this.animalId);
                        Toast.makeText(getContext(), "Adopción APROBADA. Entrega agendada.", Toast.LENGTH_LONG).show();
                        cargarSolicitudes();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al confirmar entrega: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("CANCELAR", null);
        builder.show();
    }


    /**
     * Filtra voluntarios ocupados en esa cita, actualiza el Spinner y muestra el diálogo final.
     */
    private void filtrarYMostrarDialogo(SolicitudAdopcion solicitud, AlertDialog.Builder builder, LinearLayout layout, String fecha, String hora) {

        db.collection("citas")
                .whereEqualTo("fecha", fecha)
                .whereEqualTo("hora", hora)
                .whereEqualTo("estado", "asignada")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> occupiedVolunteerIds = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String occupiedId = doc.getString("voluntarioAsignado");
                        if (occupiedId != null) {
                            occupiedVolunteerIds.add(occupiedId);
                        }
                    }

                    List<String> availableVolunteerNames = new ArrayList<>();

                    for (Usuario vol : voluntariosDisponibles) {
                        if (!occupiedVolunteerIds.contains(vol.getUid())) {
                            availableVolunteerNames.add(vol.getNombre());
                        }
                    }

                    final Spinner spVoluntario = layout.findViewWithTag("spVoluntario");

                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                            getContext(),
                            android.R.layout.simple_spinner_item,
                            availableVolunteerNames
                    );
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spVoluntario.setAdapter(spinnerAdapter);

                    if (availableVolunteerNames.isEmpty()) {
                        builder.setTitle("Solicitud: SIN VOLUNTARIOS DISPONIBLES");
                        Toast.makeText(getContext(), "No hay voluntarios libres.", Toast.LENGTH_LONG).show();
                        builder.setNeutralButton("Cerrar", null);
                    } else {
                        builder.setPositiveButton("ASIGNAR VOLUNTARIO", (dialog, which) -> {
                            String selectedName = (String) spVoluntario.getSelectedItem();
                            String selectedVolunteerId = null;

                            for (Usuario vol : voluntariosDisponibles) {
                                if (vol.getNombre().equals(selectedName)) {
                                    selectedVolunteerId = vol.getUid();
                                    break;
                                }
                            }

                            if (selectedVolunteerId != null) {
                                String voluntarioNombre = voluntariosMap.get(selectedVolunteerId);
                                asignarVoluntarioACitaAgendada(solicitud, selectedVolunteerId, voluntarioNombre);
                            } else {
                                Toast.makeText(getContext(), "Error al obtener ID del voluntario seleccionado.", Toast.LENGTH_SHORT).show();
                            }
                        });
                        builder.setNegativeButton("Cancelar", null);
                    }

                    builder.show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al verificar disponibilidad: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    builder.setNeutralButton("Cerrar", null).show();
                });
    }


    /**
     * Lógica (Paso 2 del flujo): Asigna el voluntario a una cita YA agendada.
     */
    private void asignarVoluntarioACitaAgendada(SolicitudAdopcion solicitud, String voluntarioId, String voluntarioNombre) {
        String citaId = solicitud.getCitaId();
        if (citaId == null) {
            Toast.makeText(getContext(), "Error: La solicitud no tiene cita ID para actualizar.", Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> citaUpdates = new HashMap<>();
        citaUpdates.put("voluntarioAsignado", voluntarioId);
        citaUpdates.put("voluntarioNombre", voluntarioNombre);
        citaUpdates.put("estado", "asignada");

        FirebaseHelper.getInstance().updateCita(citaId, citaUpdates, new FirebaseHelper.CitaUpdateListener() {
            @Override
            public void onCitaUpdated() {
                Map<String, Object> solicitudUpdates = new HashMap<>();
                solicitudUpdates.put("voluntarioId", voluntarioId);
                solicitudUpdates.put("voluntarioNombre", voluntarioNombre);
                solicitudUpdates.put("estado", "Voluntario Asignado");
                solicitudUpdates.put("estadoSolicitud", "Voluntario Asignado");

                db.collection("solicitudes_adopcion").document(solicitud.getIdSolicitud())
                        .update(solicitudUpdates)
                        .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Voluntario asignado: " + voluntarioNombre, Toast.LENGTH_LONG).show())
                        .addOnSuccessListener(aVoid -> cargarSolicitudes())
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al actualizar solicitud: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Error al asignar voluntario a la cita: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * Centraliza la actualización de estado final (Aprobada/Rechazada) en la base de datos.
     */
    private void actualizarEstadoFinal(SolicitudAdopcion solicitud, String nuevoEstado) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", nuevoEstado);
        updates.put("estadoSolicitud", nuevoEstado);
        updates.put("fechaDictamen", new Date());

        db.collection("solicitudes_adopcion").document(solicitud.getIdSolicitud())
                .update(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Dictamen final: " + nuevoEstado, Toast.LENGTH_LONG).show())
                .addOnSuccessListener(aVoid -> cargarSolicitudes())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private LinearLayout buildDialogLayout(Context context, SolicitudAdopcion solicitud) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        TextView tvTitulo = new TextView(context);
        tvTitulo.setText("DETALLES DE LA SOLICITUD (" + (solicitud.getFolio() != null ? solicitud.getFolio() : "Sin Folio") + ")");
        tvTitulo.setTextSize(16);
        tvTitulo.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(tvTitulo);

        // ... (Mostrar campos principales) ...
        addTextViewToLayout(layout, "Adoptante:", solicitud.getNombreCompleto());
        // ... (otros campos) ...

        return layout;
    }

    private void addTextViewToLayout(LinearLayout layout, String label, String value) {
        TextView tv = new TextView(getContext());
        if (value.isEmpty()) {
            tv.setText(label);
        } else {
            tv.setText(label + " " + (value != null ? value : "-"));
        }
        layout.addView(tv);
    }

    private void displayMapData(LinearLayout layout, String sectionTitle, Map<String, Object> data) {
        if (data != null && !data.isEmpty()) {
            TextView tvTitle = new TextView(getContext());
            tvTitle.setText(sectionTitle);
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            layout.addView(tvTitle);

            for (Map.Entry<String, Object> entry : data.entrySet()) {
                addTextViewToLayout(layout, "  - " + entry.getKey() + ":", String.valueOf(entry.getValue()));
            }
        }
    }

    private void marcarAnimalAdoptado(String animalId) {
        if (animalId != null) {
            db.collection("animales").document(animalId)
                    .update("estadoRefugio", "Adoptado")
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Animal marcado como ADOPTADO"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error al marcar animal como adoptado: ", e));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}