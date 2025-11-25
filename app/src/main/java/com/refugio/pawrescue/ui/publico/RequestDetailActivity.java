package com.refugio.pawrescue.ui.publico;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
                displayRequestData();
                loadTimeline();
                loadDocuments();
                listenToMessages();
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
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void setupRecyclerViews() {
        timelineAdapter = new TimelineAdapter(this, timelineSteps);
        recyclerViewTimeline.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTimeline.setAdapter(timelineAdapter);

        documentAdapter = new DocumentAdapter(this, documents, this::onDocumentUploadClick);
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
                            displayRequestData();
                            loadTimeline();
                            loadDocuments();
                            listenToMessages();
                        }
                    } else {
                        Toast.makeText(this, "Solicitud no encontrada", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void displayRequestData() {
        if (request == null) return;

        if (request.getAnimalFotoUrl() != null) {
            Glide.with(this).load(request.getAnimalFotoUrl()).into(ivAnimalPhoto);
        }
        tvAnimalName.setText(request.getAnimalNombre());
        tvAnimalDetails.setText(request.getAnimalRaza());
        tvFolio.setText("#" + (request.getFolio() != null ? request.getFolio() : solicitudId.substring(0,8)));
        tvCurrentStatus.setText(request.getEstadoTexto());
        ivStatusIcon.setImageResource(request.getEstadoIcon());

        // 🟢 LÓGICA DEL BOTÓN DE SEGUIMIENTO (CORREGIDA)
        String estado = request.getEstado();
        if (estado != null && (estado.equalsIgnoreCase("aprobada") || estado.equalsIgnoreCase("adoptado"))) {

            btnSeguimiento.setVisibility(View.VISIBLE);

            btnSeguimiento.setOnClickListener(v -> {
                Intent intent = new Intent(RequestDetailActivity.this, PostAdoptionActivity.class);
                // CORRECCIÓN: Usamos solo los getters que SÍ existen en tu clase AdoptionRequest
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

        timelineSteps.add(new TimelineStep("Solicitud Recibida", request.getFechaFormateada(), "completed", "Solicitud registrada", null));

        if (request.getEstado().equals("aprobada")) {
            timelineSteps.add(new TimelineStep("Aprobada", "Reciente", "completed", "¡Felicidades!", null));
        } else if (!request.getEstado().equals("pendiente")) {
            timelineSteps.add(new TimelineStep("En Revisión", "En proceso", "current", "Revisando perfil", null));
        }
        timelineAdapter.notifyDataSetChanged();
    }

    private void loadDocuments() {
        documents.clear();
        documents.add(new DocumentItem("INE / Identificación", true));
        documents.add(new DocumentItem("Comprobante de domicilio", true));
        documentAdapter.notifyDataSetChanged();
    }

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

    private void onDocumentUploadClick(DocumentItem document) {
        Toast.makeText(this, "Subir documento...", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messageListener != null) messageListener.remove();
    }
}