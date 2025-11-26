package com.refugio.pawrescue.ui.admin.tabs;

import android.app.AlertDialog;
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
import com.refugio.pawrescue.model.Cita;
import com.refugio.pawrescue.model.SolicitudAdopcion;
import com.refugio.pawrescue.model.Usuario;
import com.refugio.pawrescue.ui.adapter.SolicitudAdopcionAdapter;

import java.util.ArrayList;
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

    // Listas para almacenar Voluntarios reales
    private List<Usuario> voluntariosDisponibles = new ArrayList<>();
    private Map<String, String> voluntariosMap = new HashMap<>(); // UID -> Nombre Completo


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

        // Llamada para cargar los voluntarios al iniciar
        cargarVoluntariosDisponibles();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Asegúrate de usar el layout correcto que proporcionaste: fragment_adoption
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

    /**
     * Carga los usuarios con rol "voluntario" en segundo plano.
     */
    private void cargarVoluntariosDisponibles() {
        if (db == null) return;

        // Utilizamos "Voluntario" tal como lo tienes en el código, asumiendo que coincide con la BDD.
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
                                // Usamos el nombre y el ID
                                voluntariosMap.put(usuario.getUid(), usuario.getNombre());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error al parsear documento de usuario: " + doc.getId(), e);
                        }
                    }
                    Log.d(TAG, "Voluntarios cargados: " + voluntariosDisponibles.size());
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error cargando voluntarios: ", e));
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

                            // Lectura de campos de referencia
                            if (doc.contains("citaId")) sol.setCitaId(doc.getString("citaId"));
                            if (doc.contains("reporteId")) sol.setReporteId(doc.getString("reporteId"));

                            // Lectura de los campos de voluntario (NUEVOS CAMPOS)
                            if (doc.contains("voluntarioId")) sol.setVoluntarioId(doc.getString("voluntarioId"));
                            if (doc.contains("voluntarioNombre")) sol.setVoluntarioNombre(doc.getString("voluntarioNombre"));

                            // Lectura de los campos Map<String, Object> para evitar que salgan null en el diálogo
                            if (doc.contains("datosPersonales")) sol.setDatosPersonales((Map<String, Object>) doc.get("datosPersonales"));
                            if (doc.contains("datosFamilia")) sol.setDatosFamilia((Map<String, Object>) doc.get("datosFamilia"));
                            if (doc.contains("datosExperiencia")) sol.setDatosExperiencia((Map<String, Object>) doc.get("datosExperiencia"));
                            if (doc.contains("datosCompromiso")) sol.setDatosCompromiso((Map<String, Object>) doc.get("datosCompromiso"));

                            // Lectura de los campos simples (parches existentes)
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

    // ========================================================================
    // --- LÓGICA CENTRAL DE GESTIÓN EN DIÁLOGO ---
    // ========================================================================

    @Override
    public void onSolicitudClick(SolicitudAdopcion solicitud) {
        mostrarDialogoGestionCompleta(solicitud);
    }

    /**
     * Muestra un diálogo unificado para ver detalles completos, asignar voluntario
     * y aprobar/rechazar la solicitud, dependiendo del estado actual.
     */
    private void mostrarDialogoGestionCompleta(SolicitudAdopcion solicitud) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Gestión de Solicitud: " + solicitud.getNombreAnimal());

        // Usamos un layout dinámico para mostrar todos los datos del formulario
        LinearLayout layout = buildDialogLayout(getContext(), solicitud);
        builder.setView(layout);

        // CONDICIONAL PRINCIPAL: EL USUARIO YA AGENDÓ LA CITA (fechaCita != null)
        if (solicitud.getFechaCita() != null && solicitud.getVoluntarioId() == null) {
            // FASE 1: ASIGNACIÓN DE VOLUNTARIO (Activamos la lógica asíncrona de disponibilidad)

            // 1. Cargamos la Cita para obtener fecha y hora exacta (string)
            db.collection("citas").document(solicitud.getCitaId()).get()
                    .addOnSuccessListener(citaSnapshot -> {
                        if (citaSnapshot.exists()) {
                            String fechaAgendada = citaSnapshot.getString("fecha");
                            String horaAgendada = citaSnapshot.getString("hora");

                            if (fechaAgendada != null && horaAgendada != null) {
                                // 2. Filtramos la lista global de voluntarios disponibles
                                filtrarYMostrarDialogo(solicitud, builder, layout, fechaAgendada, horaAgendada);
                            } else {
                                // Cita incompleta (debería ser raro si el usuario validó)
                                Toast.makeText(getContext(), "Error: Cita agendada incompleta.", Toast.LENGTH_LONG).show();
                                builder.setNeutralButton("Cerrar", null).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "Error: No se encontró el documento de Cita asociado.", Toast.LENGTH_LONG).show();
                            builder.setNeutralButton("Cerrar", null).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error al cargar la Cita: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        builder.setNeutralButton("Cerrar", null).show();
                    });

            return; // Salimos del flujo principal, el diálogo se mostrará en el callback
        }

        // --- FASES 2, 3, y Default (Síncrono) ---
        if (solicitud.getVoluntarioId() != null && solicitud.getReporteId() == null) {
            // FASE 2: EN ESPERA DE REPORTE
            builder.setNeutralButton("Cerrar", null);

        } else if (solicitud.getReporteId() != null) {
            // FASE 3: DECISIÓN FINAL DEL ADMIN (Existe Reporte)

            TextView tvReporte = new TextView(getContext());
            tvReporte.setText("\n--- REPORTE DEL VOLUNTARIO (" + solicitud.getReporteId() + ") ---");
            tvReporte.setTypeface(null, android.graphics.Typeface.BOLD);
            layout.addView(tvReporte);

            addTextViewToLayout(layout, "Comentario:", "Reporte simulado: El adoptante parece responsable y apto.");

            builder.setPositiveButton("APROBAR ADOPCIÓN", (dialog, which) -> {
                actualizarEstadoFinal(solicitud, "Aprobada");
                marcarAnimalAdoptado();
            });
            builder.setNegativeButton("RECHAZAR ADOPCIÓN", (dialog, which) -> {
                actualizarEstadoFinal(solicitud, "Rechazada");
            });

        } else {
            // Caso por defecto (Ej: No hay fecha de cita agendada por el usuario aún)
            builder.setNeutralButton("Cerrar", null);
        }

        // Solo se muestra el diálogo aquí si no se entró en el flujo asíncrono
        if (solicitud.getFechaCita() == null || solicitud.getVoluntarioId() != null) {
            builder.show();
        }
    }


    /**
     * 🟢 NUEVO MÉTODO: Filtra voluntarios ocupados en esa cita, actualiza el Spinner y muestra el diálogo final.
     */
    private void filtrarYMostrarDialogo(SolicitudAdopcion solicitud, AlertDialog.Builder builder, LinearLayout layout, String fecha, String hora) {

        // Consultamos citas que coincidan con la fecha Y hora y ya estén asignadas
        db.collection("citas")
                .whereEqualTo("fecha", fecha)
                .whereEqualTo("hora", hora)
                .whereEqualTo("estado", "asignada") // Citas que ya tienen voluntario asignado
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // 1. IDs de voluntarios ocupados
                    List<String> occupiedVolunteerIds = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String occupiedId = doc.getString("voluntarioAsignado");
                        if (occupiedId != null) {
                            occupiedVolunteerIds.add(occupiedId);
                        }
                    }

                    // 2. Lista de nombres de voluntarios DISPONIBLES para el Spinner
                    List<String> availableVolunteerNames = new ArrayList<>();

                    for (Usuario vol : voluntariosDisponibles) {
                        // Si el voluntario NO está en la lista de ocupados, está disponible.
                        if (!occupiedVolunteerIds.contains(vol.getUid())) {
                            availableVolunteerNames.add(vol.getNombre());
                        }
                    }

                    final Spinner spVoluntario = layout.findViewWithTag("spVoluntario");

                    // Actualizar Spinner con la lista filtrada
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                            getContext(),
                            android.R.layout.simple_spinner_item,
                            availableVolunteerNames
                    );
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spVoluntario.setAdapter(spinnerAdapter);

                    if (availableVolunteerNames.isEmpty()) {
                        builder.setTitle("Solicitud: SIN VOLUNTARIOS DISPONIBLES");
                        Toast.makeText(getContext(), "No hay voluntarios libres para el " + fecha + " a las " + hora + ". Se recomienda contactar al adoptante para re-agendar.", Toast.LENGTH_LONG).show();
                        // Deshabilitar botón de asignación
                        builder.setNeutralButton("Cerrar", null);

                    } else {
                        // Habilitar botón de asignación con la lógica original
                        builder.setPositiveButton("ASIGNAR VOLUNTARIO", (dialog, which) -> {
                            String selectedName = (String) spVoluntario.getSelectedItem();
                            String selectedVolunteerId = null;

                            // Se busca el ID del voluntario seleccionado en la lista original (donde tenemos el UID)
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

                    builder.show(); // Mostramos el diálogo final
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al verificar disponibilidad: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    builder.setNeutralButton("Cerrar", null).show();
                });
    }


    /**
     * Lógica (Paso 2 del flujo): Asigna el voluntario a una cita YA agendada.
     * Actualiza el documento de Cita con el ID del voluntario y el estado.
     */
    private void asignarVoluntarioACitaAgendada(SolicitudAdopcion solicitud, String voluntarioId, String voluntarioNombre) {
        String citaId = solicitud.getCitaId();
        if (citaId == null) {
            Toast.makeText(getContext(), "Error: La solicitud no tiene cita ID para actualizar.", Toast.LENGTH_LONG).show();
            return;
        }

        // 1. ACTUALIZAR EL DOCUMENTO DE CITA (Colección 'citas')
        Map<String, Object> citaUpdates = new HashMap<>();
        citaUpdates.put("voluntarioAsignado", voluntarioId);
        citaUpdates.put("voluntarioNombre", voluntarioNombre);
        citaUpdates.put("estado", "asignada"); // Estado de la Cita: Agendada -> Asignada a Voluntario

        FirebaseHelper.getInstance().updateCita(citaId, citaUpdates, new FirebaseHelper.CitaUpdateListener() {
            @Override
            public void onCitaUpdated() {
                // 2. ACTUALIZAR LA SOLICITUD con el ID de la Cita y el Voluntario
                Map<String, Object> solicitudUpdates = new HashMap<>();
                solicitudUpdates.put("voluntarioId", voluntarioId);
                solicitudUpdates.put("voluntarioNombre", voluntarioNombre);
                solicitudUpdates.put("estado", "Voluntario Asignado");
                solicitudUpdates.put("estadoSolicitud", "Voluntario Asignado");

                db.collection("solicitudes_adopcion").document(solicitud.getIdSolicitud())
                        .update(solicitudUpdates)
                        .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Voluntario asignado: " + voluntarioNombre, Toast.LENGTH_LONG).show())
                        // Aseguramos que la lista se refresque para ver el cambio de estado
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

        db.collection("solicitudes_adopcion").document(solicitud.getIdSolicitud())
                .update(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Dictamen final: " + nuevoEstado, Toast.LENGTH_LONG).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Construye el Layout del diálogo con todos los detalles de la solicitud y el selector de voluntario (si aplica).
     */
    private LinearLayout buildDialogLayout(Context context, SolicitudAdopcion solicitud) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        // --- 1. DETALLES ESENCIALES ---
        TextView tvTitulo = new TextView(context);
        tvTitulo.setText("DETALLES DE LA SOLICITUD (" + (solicitud.getFolio() != null ? solicitud.getFolio() : "Sin Folio") + ")");
        tvTitulo.setTextSize(16);
        tvTitulo.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(tvTitulo);

        // ... (Mostrar campos principales) ...
        addTextViewToLayout(layout, "Adoptante:", solicitud.getNombreCompleto());
        addTextViewToLayout(layout, "Teléfono:", solicitud.getTelefono());
        addTextViewToLayout(layout, "Email:", solicitud.getEmail());
        addTextViewToLayout(layout, "Dirección:", solicitud.getDireccion());
        addTextViewToLayout(layout, "Tipo Vivienda:", solicitud.getTipoVivienda());

        String fechaCitaStr = solicitud.getFechaCita() != null ?
                new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(solicitud.getFechaCita()) :
                "PENDIENTE DE AGENDAR";
        addTextViewToLayout(layout, "Fecha Cita Agendada:", fechaCitaStr);
        addTextViewToLayout(layout, "Estado:", solicitud.getEstado());

        String citaInfo = solicitud.getCitaId() != null ? "ID Cita: " + solicitud.getCitaId() : "Ninguna";
        addTextViewToLayout(layout, "Cita Asignada:", citaInfo);

        // Mostrar Voluntario asignado
        String voluntarioInfo = solicitud.getVoluntarioNombre() != null ? solicitud.getVoluntarioNombre() : "Pendiente de Asignación";
        addTextViewToLayout(layout, "Voluntario Asignado:", voluntarioInfo);

        String reporteInfo = solicitud.getReporteId() != null ? "ID Reporte: " + solicitud.getReporteId() : "Ninguno";
        addTextViewToLayout(layout, "Reporte Final:", reporteInfo);


        // --- 2. DETALLES DE MAPS (Mapear claves/valores) ---
        addTextViewToLayout(layout, "\n--- DATOS DEL FORMULARIO DETALLADOS ---", "");
        displayMapData(layout, "Datos Familiares:", solicitud.getDatosFamilia());
        displayMapData(layout, "Experiencia con Mascotas:", solicitud.getDatosExperiencia());
        displayMapData(layout, "Compromiso:", solicitud.getDatosCompromiso());

        // --- 3. SELECTOR DE VOLUNTARIO Y MENSAJES DE FASE (Condicional en el nuevo flujo) ---
        if (solicitud.getFechaCita() != null && solicitud.getVoluntarioId() == null) {
            // FASE 1: Asignar Voluntario
            TextView tvAsignar = new TextView(context);
            tvAsignar.setText("\nCita Agendada por el Adoptante. Asigne un Voluntario:");
            tvAsignar.setTypeface(null, android.graphics.Typeface.BOLD);
            layout.addView(tvAsignar);

            Spinner spVoluntario = new Spinner(context);
            spVoluntario.setTag("spVoluntario");

            // La lista de voluntarios se llenará en el callback asíncrono filtrarYMostrarDialogo

            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                    context,
                    android.R.layout.simple_spinner_item,
                    new ArrayList<>() // Inicialmente vacío o con "Cargando..."
            );
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spVoluntario.setAdapter(spinnerAdapter);

            layout.addView(spVoluntario);

        } else if (solicitud.getVoluntarioId() != null && solicitud.getReporteId() == null) {
            // FASE 2: Esperando Reporte
            TextView tvEspera = new TextView(context);
            String mensaje = "\nESTADO: CITA ASIGNADA A VOLUNTARIO. Esperando reporte de visita: " + voluntarioInfo;

            tvEspera.setText(mensaje);
            tvEspera.setTypeface(null, android.graphics.Typeface.BOLD);
            tvEspera.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
            layout.addView(tvEspera);
        } else if (solicitud.getFechaCita() == null) {
            // Solicitud en la cola, pero el usuario aún no ha agendado la cita
            TextView tvPendiente = new TextView(context);
            tvPendiente.setText("\nESTADO: Pendiente de que el adoptante agende fecha y hora de la visita.");
            tvPendiente.setTypeface(null, android.graphics.Typeface.BOLD);
            tvPendiente.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
            layout.addView(tvPendiente);
        }


        return layout;
    }

    /**
     * Helper para mostrar los datos de los Map<String, Object> de forma legible.
     */
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

    private void addTextViewToLayout(LinearLayout layout, String label, String value) {
        TextView tv = new TextView(getContext());
        if (value.isEmpty()) {
            tv.setText(label);
        } else {
            tv.setText(label + " " + (value != null ? value : "-"));
        }
        layout.addView(tv);
    }

    private void marcarAnimalAdoptado() {
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