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
import com.refugio.pawrescue.model.SolicitudAdopcion;
import com.refugio.pawrescue.model.Usuario; // <-- Importación necesaria del modelo Usuario
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

    // 🔴 REEMPLAZO DE MOCK: Listas para almacenar Voluntarios reales
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

        // 🚨 Llamada para cargar los voluntarios al iniciar
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

    /**
     * 🔴 NUEVO MÉTODO: Carga los usuarios con rol "voluntario" en segundo plano.
     */
    private void cargarVoluntariosDisponibles() {
        if (db == null) return;

        db.collection("usuarios")
                .whereEqualTo("rol", "voluntario")
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

                    // Opcional: Si el diálogo estuviera abierto, se podría intentar actualizarlo aquí.
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

                            // 🔴 CORRECCIÓN CRÍTICA 1: Lectura explícita de campos Map<> y referencias
                            if (doc.contains("citaId")) sol.setCitaId(doc.getString("citaId"));
                            if (doc.contains("reporteId")) sol.setReporteId(doc.getString("reporteId"));

                            // Lectura de los campos Map<String, Object> para evitar que salgan null en el diálogo
                            if (doc.contains("datosPersonales")) sol.setDatosPersonales((Map<String, Object>) doc.get("datosPersonales"));
                            if (doc.contains("datosFamilia")) sol.setDatosFamilia((Map<String, Object>) doc.get("datosFamilia"));
                            if (doc.contains("datosExperiencia")) sol.setDatosExperiencia((Map<String, Object>) doc.get("datosExperiencia"));
                            if (doc.contains("datosCompromiso")) sol.setDatosCompromiso((Map<String, Object>) doc.get("datosCompromiso"));

                            // ... (parches existentes) ...
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

        if (solicitud.getCitaId() == null) {
            // FASE 1: ASIGNACIÓN DE VOLUNTARIO (Crea la Cita)

            final Spinner spVoluntario = layout.findViewWithTag("spVoluntario");

            // 🚨 Validar que la lista de voluntarios no esté vacía
            if (voluntariosDisponibles.isEmpty()) {
                Toast.makeText(getContext(), "Error: No hay voluntarios disponibles cargados.", Toast.LENGTH_LONG).show();
                builder.setNeutralButton("Cerrar", null);
                builder.show();
                return;
            }

            builder.setPositiveButton("ASIGNAR VOLUNTARIO", (dialog, which) -> {
                String selectedName = (String) spVoluntario.getSelectedItem();
                String selectedVolunteerId = null;

                // 🔴 Búsqueda del ID real usando el mapa (eficiente)
                for (Map.Entry<String, String> entry : voluntariosMap.entrySet()) {
                    if (entry.getValue().equals(selectedName)) {
                        selectedVolunteerId = entry.getKey();
                        break;
                    }
                }

                if (selectedVolunteerId != null) {
                    crearCitaYAsignar(solicitud, selectedVolunteerId);
                } else {
                    Toast.makeText(getContext(), "Error al obtener ID del voluntario seleccionado.", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Cancelar", null);

        } else if (solicitud.getCitaId() != null && solicitud.getReporteId() == null) {
            // FASE 2: EN ESPERA DE REPORTE
            builder.setNeutralButton("Cerrar", null);

        } else if (solicitud.getReporteId() != null) {
            // FASE 3: DECISIÓN FINAL DEL ADMIN (Existe Reporte)

            // Simulación de carga de reporte (en app real, usar reporteId para cargar Seguimiento.java)
            TextView tvReporte = new TextView(getContext());
            tvReporte.setText("\n--- REPORTE DEL VOLUNTARIO (" + solicitud.getReporteId() + ") ---");
            tvReporte.setTypeface(null, android.graphics.Typeface.BOLD);
            layout.addView(tvReporte);

            // 🚨 Aquí deberías obtener y mostrar el comentario del reporte real
            addTextViewToLayout(layout, "Comentario:", "Reporte simulado: El adoptante parece responsable y apto.");

            builder.setPositiveButton("APROBAR ADOPCIÓN", (dialog, which) -> {
                actualizarEstadoFinal(solicitud, "Aprobada");
                marcarAnimalAdoptado();
            });
            builder.setNegativeButton("RECHAZAR ADOPCIÓN", (dialog, which) -> {
                actualizarEstadoFinal(solicitud, "Rechazada");
            });

        } else {
            // Estado por defecto (e.g., Voluntario asignado sin reporte aún)
            builder.setNeutralButton("Cerrar", null);
        }

        builder.show();
    }

    /**
     * Lógica para simular la creación de la Cita en Firestore
     * y actualizar la Solicitud con el citaId generado.
     */
    private void crearCitaYAsignar(SolicitudAdopcion solicitud, String voluntarioId) {
        if (solicitud.getFechaCita() == null) {
            Toast.makeText(getContext(), "Error: La solicitud no tiene fecha de cita agendada.", Toast.LENGTH_LONG).show();
            return;
        }

        // 1. CREAR EL DOCUMENTO DE CITA (Colección 'citas')
        Map<String, Object> citaData = new HashMap<>();
        citaData.put("solicitudId", solicitud.getIdSolicitud());
        citaData.put("animalId", solicitud.getIdAnimal());
        citaData.put("voluntarioId", voluntarioId); // ID del voluntario real
        citaData.put("fechaCita", solicitud.getFechaCita());

        db.collection("citas")
                .add(citaData)
                .addOnSuccessListener(documentReference -> {
                    String newCitaId = documentReference.getId();

                    // 2. ACTUALIZAR LA SOLICITUD con el ID de la Cita
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("citaId", newCitaId);
                    updates.put("estado", "Voluntario asignado");
                    updates.put("estadoSolicitud", "Voluntario asignado");

                    // Usar el nombre del voluntario para el Toast de confirmación
                    String nombreVoluntario = voluntariosMap.getOrDefault(voluntarioId, "Voluntario Desconocido");

                    db.collection("solicitudes_adopcion").document(solicitud.getIdSolicitud())
                            .update(updates)
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Cita asignada a: " + nombreVoluntario, Toast.LENGTH_LONG).show())
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al actualizar solicitud: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al crear la cita: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
     * Construye el Layout del diálogo con todos los detalles de la solicitud.
     * 🔴 MODIFICADO para usar la lista REAL de voluntarios en el Spinner.
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

        String reporteInfo = solicitud.getReporteId() != null ? "ID Reporte: " + solicitud.getReporteId() : "Ninguno";
        addTextViewToLayout(layout, "Reporte Final:", reporteInfo);


        // --- 2. DETALLES DE MAPS (Mapear claves/valores) ---
        addTextViewToLayout(layout, "\n--- DATOS DEL FORMULARIO DETALLADOS ---", "");
        // 🚨 Ahora estos Map<> DEBEN contener datos gracias a la corrección en cargarSolicitudes
        displayMapData(layout, "Datos Familiares:", solicitud.getDatosFamilia());
        displayMapData(layout, "Experiencia con Mascotas:", solicitud.getDatosExperiencia());
        displayMapData(layout, "Compromiso:", solicitud.getDatosCompromiso());

        // --- 3. SELECTOR DE VOLUNTARIO (Condicional si no hay cita) ---
        if (solicitud.getCitaId() == null) {
            TextView tvAsignar = new TextView(context);
            tvAsignar.setText("\nSeleccione el Voluntario para esta Cita:");
            tvAsignar.setTypeface(null, android.graphics.Typeface.BOLD);
            layout.addView(tvAsignar);

            Spinner spVoluntario = new Spinner(context);
            spVoluntario.setTag("spVoluntario");

            // 🔴 Llenar el Spinner con NOMBRES REALES
            List<String> volunteerNames = new ArrayList<>();
            for (Usuario vol : voluntariosDisponibles) {
                volunteerNames.add(vol.getNombre());
            }

            // 🚨 Añadir un item por defecto si la lista está vacía
            if (volunteerNames.isEmpty()) {
                volunteerNames.add("No hay voluntarios cargados");
                spVoluntario.setEnabled(false);
            }

            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                    context,
                    android.R.layout.simple_spinner_item,
                    volunteerNames
            );
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spVoluntario.setAdapter(spinnerAdapter);

            layout.addView(spVoluntario);
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