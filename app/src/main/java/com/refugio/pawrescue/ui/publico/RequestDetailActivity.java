package com.refugio.pawrescue.ui.publico;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
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
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.TimelineStep;
import com.refugio.pawrescue.model.DocumentItem;
import com.refugio.pawrescue.model.Message;
import com.refugio.pawrescue.ui.adapter.TimelineAdapter;
import com.refugio.pawrescue.ui.adapter.DocumentAdapter;
import com.refugio.pawrescue.ui.adapter.MessageAdapter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestDetailActivity extends AppCompatActivity {

    // Vistas
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

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration messageListener;

    // Datos
    private String solicitudId;
    private AdoptionRequest request; // Tu modelo completo
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

        // 1. Recuperar ID de forma segura
        if (!getSolicitudId()) {
            return; // Si falla, nos salimos
        }

        setupRecyclerViews();
        setupButtons();

        // 2. Cargar datos
        if (request != null) {
            // Si ya recibimos el objeto completo, lo usamos directo
            displayRequestData();
            loadTimeline();
            loadDocuments();
            listenToMessages(); // El chat siempre necesita Firebase
        } else {
            // Si solo tenemos ID, descargamos todo
            loadRequestData();
        }
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

    // --- MÉTODO CLAVE PARA EVITAR EL CRASH ---
    private boolean getSolicitudId() {
        // Intento 1: Ver si nos pasaron el Objeto completo (REQUEST_OBJ)
        if (getIntent().hasExtra("REQUEST_OBJ")) {
            request = (AdoptionRequest) getIntent().getSerializableExtra("REQUEST_OBJ");
            if (request != null) {
                solicitudId = request.getId();
            }
        }

        // Intento 2: Ver si nos pasaron el ID suelto (REQUEST_ID o SOLICITUD_ID)
        if (solicitudId == null) {
            if (getIntent().hasExtra("REQUEST_ID")) {
                solicitudId = getIntent().getStringExtra("REQUEST_ID");
            } else if (getIntent().hasExtra("SOLICITUD_ID")) { // Por compatibilidad
                solicitudId = getIntent().getStringExtra("SOLICITUD_ID");
            }
        }

        // Validación Final
        if (solicitudId == null || solicitudId.isEmpty()) {
            Toast.makeText(this, "Error: ID de solicitud no encontrado", Toast.LENGTH_LONG).show();
            finish(); // Cerramos suavemente
            return false;
        }
        return true;
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
        btnCopyFolio.setOnClickListener(v -> copyFolioToClipboard());
        btnSend.setOnClickListener(v -> sendMessage());
        btnAttach.setOnClickListener(v -> { /* Implementar adjuntar */ });
    }

    private void loadRequestData() {
        // Protección adicional
        if (solicitudId == null) return;

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
                    } else {
                        Toast.makeText(this, "La solicitud no existe", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar solicitud", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayRequestData() {
        if (request == null) return;

        // Foto
        if (request.getAnimalFotoUrl() != null) {
            Glide.with(this)
                    .load(request.getAnimalFotoUrl())
                    .placeholder(R.drawable.placeholder_animal)
                    .into(ivAnimalPhoto);
        }

        // Textos
        tvAnimalName.setText(request.getAnimalNombre());
        tvAnimalDetails.setText(request.getAnimalRaza() != null ? request.getAnimalRaza() : "Raza no especificada");

        String folioText = request.getFolio() != null ? request.getFolio() : request.getId();
        tvFolio.setText("#" + folioText);

        // Estado
        if (request.getEstado() != null) {
            tvCurrentStatus.setText(request.getEstadoTexto()); // Asegúrate que tu modelo tenga este método
            // Si tienes iconos en el modelo, úsalos:
            // ivStatusIcon.setImageResource(request.getEstadoIcon());
        }
    }

    private void loadTimeline() {
        timelineSteps.clear();
        if (request == null) return;

        String fecha = request.getFechaFormateada(); // Asegúrate que tu modelo tenga esto

        // Paso 1: Solicitud Recibida
        timelineSteps.add(new TimelineStep(
                "Solicitud Recibida",
                fecha,
                "completed",
                "Tu solicitud ha sido registrada exitosamente",
                null
        ));

        // Paso 2: En Revisión
        if (request.getEstado() != null && !request.getEstado().equals("pendiente")) {
            timelineSteps.add(new TimelineStep(
                    "En Revisión",
                    "En proceso",
                    "completed",
                    "El administrador está revisando tu perfil",
                    null
            ));
        }

        // ... (Puedes agregar más lógica de pasos aquí según el estado)

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
        if (solicitudId == null) return;

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
        if (messageText.isEmpty() || solicitudId == null) return;

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
                .addOnSuccessListener(doc -> etMessage.setText(""))
                .addOnFailureListener(e -> Toast.makeText(this, "Error al enviar", Toast.LENGTH_SHORT).show());
    }

    private void copyFolioToClipboard() {
        if (request == null) return;
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Folio", request.getFolio() != null ? request.getFolio() : request.getId());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Folio copiado", Toast.LENGTH_SHORT).show();
    }

    private void onDocumentUploadClick(DocumentItem document) {
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