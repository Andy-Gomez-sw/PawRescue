package com.refugio.pawrescue.ui.publico;

import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.TimeSlot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.refugio.pawrescue.ui.adapter.TimeSlotAdapter;

/**
 * Actividad donde el USUARIO selecciona fecha y hora para su cita de adopción
 * Restricciones: No domingos, horario 9AM-5PM, slots de 1 hora
 */
public class AppointmentSelectionActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private RecyclerView rvTimeSlots;
    private TextView tvSelectedDate, tvAnimalName;
    private MaterialButton btnConfirmarCita;
    private ImageButton btnBack;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String solicitudId;
    private String animalId;
    private String animalName;
    private Date selectedDate;
    private TimeSlot selectedTimeSlot;

    private TimeSlotAdapter timeSlotAdapter;
    private List<TimeSlot> availableSlots = new ArrayList<>();

    // Horarios disponibles (9 AM a 5 PM)
    private static final String[] TIME_SLOTS = {
            "09:00", "10:00", "11:00", "12:00",
            "13:00", "14:00", "15:00", "16:00", "17:00"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_selection);

        initViews();
        initFirebase();
        getIntentData();
        setupCalendar();
        setupTimeSlots();
        setupButtons();
    }

    private void initViews() {
        calendarView = findViewById(R.id.calendarView);
        rvTimeSlots = findViewById(R.id.rvTimeSlots);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvAnimalName = findViewById(R.id.tvAnimalName);
        btnConfirmarCita = findViewById(R.id.btnConfirmarCita);
        btnBack = findViewById(R.id.btnBack);
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void getIntentData() {
        solicitudId = getIntent().getStringExtra("SOLICITUD_ID");
        animalId = getIntent().getStringExtra("ANIMAL_ID");
        animalName = getIntent().getStringExtra("ANIMAL_NAME");

        if (solicitudId == null || animalId == null) {
            Toast.makeText(this, "Error: Datos incompletos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvAnimalName.setText("Cita para adoptar a " + animalName);
    }

    private void setupCalendar() {
        // Establecer fecha mínima (hoy)
        calendarView.setMinDate(System.currentTimeMillis());

        // Establecer fecha máxima (3 meses adelante)
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.MONTH, 3);
        calendarView.setMaxDate(maxDate.getTimeInMillis());

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth);

            // Verificar que NO sea domingo
            if (selected.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                Toast.makeText(this, "⚠️ No hay citas los domingos", Toast.LENGTH_SHORT).show();
                return;
            }

            selectedDate = selected.getTime();
            updateSelectedDateText();
            loadAvailableTimeSlots();
        });
    }

    private void setupTimeSlots() {
        timeSlotAdapter = new TimeSlotAdapter(availableSlots, slot -> {
            selectedTimeSlot = slot;
            timeSlotAdapter.setSelectedSlot(slot);
            btnConfirmarCita.setEnabled(true);
        });

        rvTimeSlots.setLayoutManager(new GridLayoutManager(this, 3));
        rvTimeSlots.setAdapter(timeSlotAdapter);
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnConfirmarCita.setEnabled(false);
        btnConfirmarCita.setOnClickListener(v -> confirmarCita());
    }

    private void updateSelectedDateText() {
        if (selectedDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", new Locale("es", "MX"));
            tvSelectedDate.setText(sdf.format(selectedDate));
            tvSelectedDate.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Carga los horarios disponibles para la fecha seleccionada
     * Verifica cuáles ya están ocupados en Firestore
     */
    private void loadAvailableTimeSlots() {
        if (selectedDate == null) return;

        availableSlots.clear();
        btnConfirmarCita.setEnabled(false);

        // Formato de fecha para comparar en BD
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateString = dateFormat.format(selectedDate);

        // Consultar citas YA agendadas para este día
        db.collection("citas")
                .whereEqualTo("fecha", dateString)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> horariosOcupados = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String hora = doc.getString("hora");
                        if (hora != null) horariosOcupados.add(hora);
                    }

                    // Generar slots disponibles
                    for (String hora : TIME_SLOTS) {
                        boolean disponible = !horariosOcupados.contains(hora);
                        availableSlots.add(new TimeSlot(hora, disponible));
                    }

                    timeSlotAdapter.notifyDataSetChanged();

                    if (availableSlots.stream().noneMatch(TimeSlot::isAvailable)) {
                        Toast.makeText(this, "No hay horarios disponibles para este día", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar horarios", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Guarda la cita en Firestore
     * Estado inicial: "pendiente_asignacion" (Admin debe asignar voluntario)
     */
    private void confirmarCita() {
        if (selectedDate == null || selectedTimeSlot == null) {
            Toast.makeText(this, "Selecciona fecha y hora", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!selectedTimeSlot.isAvailable()) {
            Toast.makeText(this, "Este horario ya no está disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "Error de sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirmarCita.setEnabled(false);
        btnConfirmarCita.setText("Agendando...");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String fechaString = dateFormat.format(selectedDate);

        Map<String, Object> cita = new HashMap<>();
        cita.put("solicitudId", solicitudId);
        cita.put("animalId", animalId);
        cita.put("animalNombre", animalName);
        cita.put("usuarioId", userId);
        cita.put("usuarioEmail", auth.getCurrentUser().getEmail());
        cita.put("fecha", fechaString);
        cita.put("hora", selectedTimeSlot.getTime());
        cita.put("estado", "pendiente_asignacion"); // Admin debe asignar voluntario
        cita.put("fechaCreacion", new Date());
        cita.put("voluntarioAsignado", null);
        cita.put("reporteCompleto", false);

        // Guardar cita
        db.collection("citas")
                .add(cita)
                .addOnSuccessListener(documentReference -> {
                    String citaId = documentReference.getId();

                    // Actualizar la solicitud con el ID de la cita
                    db.collection("solicitudes_adopcion")
                            .document(solicitudId)
                            .update(
                                    "estado", "cita_agendada",
                                    "citaId", citaId,
                                    "fechaCita", fechaString,
                                    "horaCita", selectedTimeSlot.getTime()
                            )
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "✅ ¡Cita agendada con éxito!", Toast.LENGTH_LONG).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error al actualizar solicitud", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btnConfirmarCita.setEnabled(true);
                    btnConfirmarCita.setText("Confirmar Cita");
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}