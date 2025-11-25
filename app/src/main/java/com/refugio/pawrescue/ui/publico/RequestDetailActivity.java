package com.refugio.pawrescue.ui.publico;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import com.refugio.pawrescue.R;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.refugio.pawrescue.ui.adapter.TimelineAdapter;
import com.refugio.pawrescue.model.TimelineStep;
import com.refugio.pawrescue.ui.adapter.DocumentAdapter;
import com.refugio.pawrescue.model.DocumentItem;
import com.refugio.pawrescue.ui.adapter.MessageAdapter;
import com.refugio.pawrescue.model.Message;

public class RequestDetailActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ImageView ivAnimalPhoto;
    private TextView tvAnimalName;
    private TextView tvAnimalDetails;
    private TextView tvFolio;
    private ImageButton btnCopyFolio;
    private MaterialCardView cardCurrentStatus;
    private ImageView ivStatusIcon;
    private TextView tvCurrentStatus;
    private RecyclerView recyclerViewTimeline;
    private RecyclerView recyclerViewDocuments;
    private RecyclerView recyclerViewMessages;
    private ImageButton btnAttach;
    private EditText etMessage;
    private ImageButton btnSend;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration messageListener;

    private String solicitudId;
    private AdoptionRequest request;
    private TimelineAdapter timelineAdapter;
    private DocumentAdapter documentAdapter;
    private MessageAdapter messageAdapter;

    private List<TimelineStep> timelineSteps;
    private List<DocumentItem> documents;
    private List<Message> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);

        initViews();
        initFirebase();
        getSolicitudId();
        setupRecyclerViews();
        setupButtons();
        loadRequestData();
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

        timelineSteps = new ArrayList<>();
        documents = new ArrayList<>();
        messages = new ArrayList<>();
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void getSolicitudId() {
        solicitudId = getIntent().getStringExtra("SOLICITUD_ID");
        if (solicitudId == null) {
            finish();
        }
    }

    private void setupRecyclerViews() {
        // Timeline
        timelineAdapter = new TimelineAdapter(this, timelineSteps);
        recyclerViewTimeline.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTimeline.setAdapter(timelineAdapter);

        // Documentos
        documentAdapter = new DocumentAdapter(this, documents, this::onDocumentUploadClick);
        recyclerViewDocuments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDocuments.setAdapter(documentAdapter);

        // Mensajes
        messageAdapter = new MessageAdapter(this, messages);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMessages.setAdapter(messageAdapter);
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnCopyFolio.setOnClickListener(v -> {
            copyFolioToClipboard();
        });

        btnSend.setOnClickListener(v -> {
            sendMessage();
        });

        btnAttach.setOnClickListener(v -> {
            // Implementar adjuntar archivo
        });
    }

    private void loadRequestData() {
        db.collection("solicitudes_adopcion").document(solicitudId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        request = documentSnapshot.toObject(AdoptionRequest.class);
                        if (request != null) {
                            request.setId(documentSnapshot.getId());
                            displayRequestData();
                            loadTimeline();
                            loadDocuments();
                            listenToMessages();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar solicitud", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayRequestData() {
        // Foto del animal
        if (request.getAnimalFotoUrl() != null) {
            Glide.with(this)
                    .load(request.getAnimalFotoUrl())
                    .placeholder(R.drawable.placeholder_animal)
                    .into(ivAnimalPhoto);
        }

        // Nombre y detalles
        tvAnimalName.setText(request.getAnimalNombre());
        tvAnimalDetails.setText(request.getAnimalRaza());

        // Folio
        tvFolio.setText("#" + request.getFolio());

        // Estado actual
        tvCurrentStatus.setText(request.getEstadoTexto());
        ivStatusIcon.setImageResource(request.getEstadoIcon());
    }

    private void loadTimeline() {
        timelineSteps.clear();

        // Paso 1: Solicitud Recibida
        timelineSteps.add(new TimelineStep(
                "Solicitud Recibida",
                request.getFechaFormateada() + ", 10:30 AM",
                "completed",
                "Tu solicitud ha sido registrada exitosamente",
                null
        ));

        // Paso 2: En Revisión
        if (!request.getEstado().equals("pendiente")) {
            timelineSteps.add(new TimelineStep(
                    "En Revisión",
                    request.getFechaFormateada() + ", 9:15 AM",
                    "completed",
                    "El administrador está revisando tu perfil",
                    null
            ));
        }

        // Paso 3: Cita Agendada
        if (request.getEstado().equals("cita_agendada") || request.getEstado().equals("aprobada")) {
            String status = request.getEstado().equals("cita_agendada") ? "current" : "completed";
            timelineSteps.add(new TimelineStep(
                    "Cita Agendada",
                    request.getCitaAgendada() != null ?
                            request.getCitaAgendada().getFechaHoraFormateada() : "Pendiente",
                    status,
                    "Visita programada al refugio",
                    request.getCitaAgendada() != null ? request.getCitaAgendada().getLugar() : null
            ));
        } else {
            timelineSteps.add(new TimelineStep(
                    "Cita Agendada",
                    "Pendiente",
                    "pending",
                    "Se agendará una visita al refugio",
                    null
            ));
        }

        // Paso 4: Visita Domiciliaria
        timelineSteps.add(new TimelineStep(
                "Visita Domiciliaria",
                "Pendiente",
                "pending",
                "Se realizará después de la primera cita",
                null
        ));

        // Paso 5: Decisión Final
        if (request.getEstado().equals("aprobada")) {
            timelineSteps.add(new TimelineStep(
                    "Decisión Final",
                    request.getFechaFormateada(),
                    "completed",
                    "¡Felicidades! Tu solicitud ha sido aprobada",
                    null
            ));
        } else {
            timelineSteps.add(new TimelineStep(
                    "Decisión Final",
                    "Pendiente",
                    "pending",
                    "Resultado del proceso de adopción",
                    null
            ));
        }

        timelineAdapter.notifyDataSetChanged();
    }

    private void loadDocuments() {
        documents.clear();
        documents.add(new DocumentItem("INE / Identificación", true));
        documents.add(new DocumentItem("Comprobante de domicilio", true));
        documents.add(new DocumentItem("Fotos de la vivienda", false));
        documentAdapter.notifyDataSetChanged();
    }

    private void listenToMessages() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) return;

        messageListener = db.collection("solicitudes_adopcion")
                .document(solicitudId)
                .collection("mensajes")
                .orderBy("fecha")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        messages.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Message message = doc.toObject(Message.class);
                            messages.add(message);
                        }
                        messageAdapter.notifyDataSetChanged();
                        if (!messages.isEmpty()) {
                            recyclerViewMessages.scrollToPosition(messages.size() - 1);
                        }
                    }
                });
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (messageText.isEmpty()) return;

        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) return;

        Map<String, Object> mensaje = new HashMap<>();
        mensaje.put("texto", messageText);
        mensaje.put("userId", userId);
        mensaje.put("fecha", new Date());
        mensaje.put("tipo", "usuario");

        db.collection("solicitudes_adopcion")
                .document(solicitudId)
                .collection("mensajes")
                .add(mensaje)
                .addOnSuccessListener(documentReference -> {
                    etMessage.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al enviar mensaje", Toast.LENGTH_SHORT).show();
                });
    }

    private void copyFolioToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Folio", request.getFolio());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Folio copiado", Toast.LENGTH_SHORT).show();
    }

    private void onDocumentUploadClick(DocumentItem document) {
        // Implementar carga de documento
        Toast.makeText(this, "Subir: " + document.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messageListener != null) {
            messageListener.remove();
        }
    }
}
