package com.refugio.pawrescue.ui.publico;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.Cita;
import com.refugio.pawrescue.model.TimelineStep;
import com.refugio.pawrescue.model.DocumentItem;
import com.refugio.pawrescue.model.Message;
import com.refugio.pawrescue.ui.adapter.TimelineAdapter;
import com.refugio.pawrescue.ui.adapter.DocumentAdapter;
import com.refugio.pawrescue.ui.adapter.MessageAdapter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public class RequestDetailActivity extends AppCompatActivity {

    private static final String TAG = "RequestDetailActivity";

    // Vistas
    private ImageButton btnBack;
    private ImageView ivAnimalPhoto;
    private TextView tvAnimalName, tvAnimalDetails, tvFolio, tvCurrentStatus;
    private ImageButton btnCopyFolio, btnAttach, btnSend;
    private MaterialCardView cardCurrentStatus;
    private ImageView ivStatusIcon;
    private EditText etMessage;
    private RecyclerView recyclerViewTimeline, recyclerViewDocuments, recyclerViewMessages;
    private MaterialButton btnSeguimiento;

    // Firebase y Datos
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration messageListener;
    private String solicitudId;
    private AdoptionRequest request;

    private TimelineAdapter timelineAdapter;
    private DocumentAdapter documentAdapter;
    private MessageAdapter messageAdapter;

    private List<TimelineStep> timelineSteps = new ArrayList<>();
    private List<DocumentItem> documents = new ArrayList<>();
    private List<Message> messages = new ArrayList<>();

    private TextView tvEstadoCita, tvFechaCita, tvVoluntarioAsignado;
    private MaterialButton btnAgendarCita;
    private View sectionCita;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);

        initViews();
        initFirebase();

        if (getIntentData()) {
            setupRecyclerViews();
            setupButtons();

            if (request != null) {
                // Si viene el objeto serializado, lo usamos pero refrescamos datos críticos
                displayRequestData();
                loadTimeline();
                loadDocuments();
                loadCitaInfo();
                listenToMessages();
                // 🟢 Verificar imagen del animal por si falta
                if (request.getAnimalFotoUrl() == null && request.getAnimalId() != null) {
                    fetchAnimalImage(request.getAnimalId());
                }
            } else {
                loadRequestData();
            }
        }
    }

    private boolean getIntentData() {
        if (getIntent().hasExtra("REQUEST_OBJ")) {
            request = (AdoptionRequest) getIntent().getSerializableExtra("REQUEST_OBJ");
            if (request != null) solicitudId = request.getId();
        }
        if (solicitudId == null) {
            solicitudId = getIntent().getStringExtra("REQUEST_ID");
            if (solicitudId == null) {
                solicitudId = getIntent().getStringExtra("SOLICITUD_ID");
            }
        }

        if (solicitudId == null || solicitudId.isEmpty()) {
            Toast.makeText(this, "Error: ID no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }
        return true;
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        ivAnimalPhoto = findViewById(R.id.ivAnimalPhoto);
        tvAnimalName = findViewById(R.id.tvAnimalName);
        tvAnimalDetails = findViewById(R.id.tvAnimalDetails);
        tvFolio = findViewById(R.id.tvFolio);
        btnCopyFolio = findViewById(R.id.btnCopyFolio);
        cardCurrentStatus = findViewById(R.id.cardCurrentStatus);
        ivStatusIcon = findViewById(R.id.ivStatusIcon);
        tvCurrentStatus = findViewById(R.id.tvCurrentStatus);
        recyclerViewTimeline = findViewById(R.id.recyclerViewTimeline);
        recyclerViewDocuments = findViewById(R.id.recyclerViewDocuments);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        btnAttach = findViewById(R.id.btnAttach);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnSeguimiento = findViewById(R.id.btnSeguimiento);
        tvEstadoCita = findViewById(R.id.tvEstadoCita);
        tvFechaCita = findViewById(R.id.tvFechaCita);
        tvVoluntarioAsignado = findViewById(R.id.tvVoluntarioAsignado);
        btnAgendarCita = findViewById(R.id.btnAgendarCita);
        sectionCita = findViewById(R.id.sectionCita);
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void setupRecyclerViews() {
        timelineAdapter = new TimelineAdapter(this, timelineSteps);
        recyclerViewTimeline.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTimeline.setAdapter(timelineAdapter);

        // 🟢 Cambiamos el listener para abrir documentos
        documentAdapter = new DocumentAdapter(this, documents, this::onDocumentClick);
        recyclerViewDocuments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDocuments.setAdapter(documentAdapter);

        messageAdapter = new MessageAdapter(this, messages);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMessages.setAdapter(messageAdapter);
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> onBackPressed());
        btnCopyFolio.setOnClickListener(v -> copyFolioToClipboard());
        btnSend.setOnClickListener(v -> sendMessage());
        btnAttach.setOnClickListener(v -> Toast.makeText(this, "Función en desarrollo", Toast.LENGTH_SHORT).show());
    }

    private void loadRequestData() {
        db.collection("solicitudes_adopcion").document(solicitudId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        request = documentSnapshot.toObject(AdoptionRequest.class);
                        if (request != null) {
                            request.setId(documentSnapshot.getId());

                            // Si la foto no vino en el objeto principal, intentamos buscarla en la BD de animales
                            if (request.getAnimalFotoUrl() == null && request.getAnimalId() != null) {
                                fetchAnimalImage(request.getAnimalId());
                            }

                            displayRequestData();
                            loadTimeline();
                            loadDocuments();
                            loadCitaInfo();
                            listenToMessages();
                        }
                    } else {
                        Toast.makeText(this, "Solicitud no encontrada", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    /**
     * 🟢 NUEVO: Busca la imagen del animal si no está en la solicitud
     */
    private void fetchAnimalImage(String animalId) {
        db.collection("animales").document(animalId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String fotoUrl = doc.getString("imageUrl"); // O "fotoUrl" según tu modelo Animal
                        if (fotoUrl == null) fotoUrl = doc.getString("fotoUrl");

                        if (fotoUrl != null && !isDestroyed()) {
                            request.setAnimalFotoUrl(fotoUrl); // Actualizar localmente
                            Glide.with(this).load(fotoUrl).into(ivAnimalPhoto);
                        }
                    }
                });
    }

    private void displayRequestData() {
        if (request == null) return;

        // Cargar imagen si ya la tenemos
        if (request.getAnimalFotoUrl() != null) {
            Glide.with(this).load(request.getAnimalFotoUrl()).placeholder(R.drawable.ic_pet_placeholder).into(ivAnimalPhoto);
        }

        tvAnimalName.setText(request.getAnimalNombre());
        tvAnimalDetails.setText(request.getAnimalRaza() != null ? request.getAnimalRaza() : "Raza desconocida");
        tvFolio.setText("#" + (request.getFolio() != null ? request.getFolio() : solicitudId.substring(0,8)));
        tvCurrentStatus.setText(request.getEstadoTexto());
        ivStatusIcon.setImageResource(request.getEstadoIcon());

        // Configuración para estado Aprobado
        String estado = request.getEstado();
        if (estado != null && (estado.equalsIgnoreCase("aprobada") || estado.equalsIgnoreCase("adoptado"))) {
            if (request.getFechaEntrega() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("es", "MX"));
                String fechaEntregaStr = sdf.format(request.getFechaEntrega());
                tvAnimalDetails.setText(request.getAnimalRaza() + " - Entrega: " + fechaEntregaStr);
            }
            btnSeguimiento.setVisibility(View.VISIBLE);
            btnSeguimiento.setOnClickListener(v -> {
                Intent intent = new Intent(RequestDetailActivity.this, PostAdoptionActivity.class);
                intent.putExtra("ANIMAL_ID", request.getAnimalId());
                intent.putExtra("ANIMAL_NAME", request.getAnimalNombre());
                intent.putExtra("SOLICITUD_ID", request.getId());
                startActivity(intent);
            });
        } else {
            btnSeguimiento.setVisibility(View.GONE);
        }
    }

    private void loadTimeline() {
        timelineSteps.clear();
        if (request == null) return;

        // Paso 1: Solicitud Recibida
        timelineSteps.add(new TimelineStep("Solicitud Recibida", request.getFechaFormateada(), "completed", "Solicitud registrada", null));

        String estado = request.getEstado();
        if (estado != null) {
            if (estado.equalsIgnoreCase("aprobada") || estado.equalsIgnoreCase("adoptado")) {
                timelineSteps.add(new TimelineStep("Revisión de Visita", "Finalizada", "completed", "Reporte de visita completado.", null));
                String dateText = request.getFechaEntrega() != null
                        ? new SimpleDateFormat("dd/MM/yyyy HH:mm").format(request.getFechaEntrega())
                        : "Fecha Pendiente";
                timelineSteps.add(new TimelineStep("Entrega Agendada", dateText, "completed", "¡Felicidades! Adopción aprobada.", null));
            } else if (estado.equalsIgnoreCase("rechazada")) {
                timelineSteps.add(new TimelineStep("Revisión de Visita", "Finalizada", "error", "Solicitud rechazada.", null));
            } else if (request.getVoluntarioId() != null) {
                timelineSteps.add(new TimelineStep("Revisión de Visita", "En proceso", "current", "El voluntario revisará tu perfil.", null));
            } else if (request.getCitaId() != null || "cita_agendada".equals(estado)) {
                timelineSteps.add(new TimelineStep("Revisión de Visita", "Cita Agendada", "current", "Esperando la asignación de voluntario.", null));
            } else if (!estado.equalsIgnoreCase("pendiente")) {
                timelineSteps.add(new TimelineStep("En Revisión", "En proceso", "current", "Revisando perfil inicial.", null));
            }
        }
        timelineAdapter.notifyDataSetChanged();
    }

    /**
     * 🟢 NUEVO: Carga los documentos reales usando las URLs guardadas
     */
    private void loadDocuments() {
        documents.clear();
        if (request == null) return;

        // Verificar INE Frente
        if (request.getUrlIneFrente() != null && !request.getUrlIneFrente().isEmpty()) {
            documents.add(new DocumentItem("INE (Frente)", "Subido", request.getUrlIneFrente()));
        } else {
            documents.add(new DocumentItem("INE (Frente)", true)); // Pendiente
        }

        // Verificar INE Reverso
        if (request.getUrlIneReverso() != null && !request.getUrlIneReverso().isEmpty()) {
            documents.add(new DocumentItem("INE (Reverso)", "Subido", request.getUrlIneReverso()));
        } else {
            documents.add(new DocumentItem("INE (Reverso)", true));
        }

        // Verificar Comprobante
        if (request.getUrlComprobante() != null && !request.getUrlComprobante().isEmpty()) {
            documents.add(new DocumentItem("Comprobante Domicilio", "Subido", request.getUrlComprobante()));
        } else {
            documents.add(new DocumentItem("Comprobante Domicilio", true));
        }

        documentAdapter.notifyDataSetChanged();
    }

    /**
     * 🟢 NUEVO: Maneja el clic en el documento para abrirlo
     */
    private void onDocumentClick(DocumentItem document) {
        if (document.getUrl() != null && !document.getUrl().isEmpty()) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(document.getUrl()));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "No se pudo abrir el documento", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Este documento aún no se ha subido", Toast.LENGTH_SHORT).show();
        }
    }

    // ... Resto de métodos (listenToMessages, sendMessage, copyFolioToClipboard, loadCitaInfo, etc.) se mantienen igual ...

    private void listenToMessages() {
        if (solicitudId == null) return;
        messageListener = db.collection("solicitudes_adopcion").document(solicitudId)
                .collection("mensajes").orderBy("fecha")
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        messages.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            messages.add(doc.toObject(Message.class));
                        }
                        messageAdapter.notifyDataSetChanged();
                        if (!messages.isEmpty()) recyclerViewMessages.scrollToPosition(messages.size() - 1);
                    }
                });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty() || auth.getCurrentUser() == null) return;

        Map<String, Object> msg = new HashMap<>();
        msg.put("texto", text);
        msg.put("userId", auth.getCurrentUser().getUid());
        msg.put("fecha", new Date());
        msg.put("tipo", "usuario");

        db.collection("solicitudes_adopcion").document(solicitudId).collection("mensajes").add(msg)
                .addOnSuccessListener(d -> etMessage.setText(""));
    }

    private void copyFolioToClipboard() {
        if (request == null) return;
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Folio", request.getFolio());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Folio copiado", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messageListener != null) messageListener.remove();
    }

    private void loadCitaInfo() {
        if (btnAgendarCita == null || sectionCita == null) return;
        if (request == null || request.getId() == null) return;

        String estado = request.getEstado();

        if ("pendiente_cita".equals(estado)) {
            btnAgendarCita.setVisibility(View.VISIBLE);
            sectionCita.setVisibility(View.GONE);
            btnAgendarCita.setOnClickListener(v -> {
                Intent intent = new Intent(RequestDetailActivity.this, AppointmentSelectionActivity.class);
                intent.putExtra("SOLICITUD_ID", request.getId());
                intent.putExtra("ANIMAL_ID", request.getAnimalId());
                intent.putExtra("ANIMAL_NAME", request.getAnimalNombre());
                startActivity(intent);
            });
        } else if ("cita_agendada".equals(estado) || "en_revision".equals(estado) ||
                "aprobada".equals(estado) || "rechazada".equals(estado)) {

            btnAgendarCita.setVisibility(View.GONE);
            sectionCita.setVisibility(View.VISIBLE);

            db.collection("citas")
                    .whereEqualTo("solicitudId", request.getId())
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            Cita cita = queryDocumentSnapshots.getDocuments().get(0).toObject(Cita.class);
                            if (cita != null) {
                                cita.setId(queryDocumentSnapshots.getDocuments().get(0).getId());
                                displayCitaInfo(cita);
                            }
                        }
                    });
        } else {
            btnAgendarCita.setVisibility(View.GONE);
            sectionCita.setVisibility(View.GONE);
        }
    }

    private void displayCitaInfo(Cita cita) {
        if (tvEstadoCita == null) return;

        String estadoCita = cita.getEstadoTexto();
        String fecha = cita.getFechaHoraCompleta();

        tvEstadoCita.setText(estadoCita);
        tvFechaCita.setText("📅 " + fecha);

        if (cita.getVoluntarioNombre() != null && !cita.getVoluntarioNombre().isEmpty()) {
            tvVoluntarioAsignado.setText("👤 Atendido por: " + cita.getVoluntarioNombre());
            tvVoluntarioAsignado.setVisibility(View.VISIBLE);
        } else {
            tvVoluntarioAsignado.setVisibility(View.GONE);
        }

        switch (cita.getEstado()) {
            case "pendiente_asignacion":
                tvEstadoCita.setBackgroundResource(R.drawable.bg_badge_warning);
                break;
            case "asignada":
                tvEstadoCita.setBackgroundResource(R.drawable.bg_badge_info);
                break;
            case "completada":
                tvEstadoCita.setBackgroundResource(R.drawable.bg_badge_success);
                break;
        }
    }
}