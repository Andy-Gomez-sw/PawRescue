package com.refugio.pawrescue.ui.publico;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.Animal;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AnimalDetailsPublicActivity extends AppCompatActivity {

    private ImageView ivPhoto;
    private TextView tvName, tvBreed, tvSex, tvStatus, tvHealth;
    private Button btnSolicitar;
    private Animal currentAnimal;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animal_details_public);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Enlazar vistas
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        ivPhoto = findViewById(R.id.ivDetailPhoto);
        tvName = findViewById(R.id.tvDetailName);
        tvBreed = findViewById(R.id.tvDetailBreed);
        tvSex = findViewById(R.id.tvDetailSex);
        tvStatus = findViewById(R.id.tvDetailStatus);
        tvHealth = findViewById(R.id.tvDetailHealth);
        btnSolicitar = findViewById(R.id.btnSolicitarAdopcion);

        // Obtener el animal del Intent
        if (getIntent().hasExtra("animal")) {
            currentAnimal = (Animal) getIntent().getSerializableExtra("animal");
            cargarDatos();
        } else {
            Toast.makeText(this, "Error al cargar detalles", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Listener del botón
        btnSolicitar.setOnClickListener(v -> solicitarAdopcion());
    }

    private void cargarDatos() {
        if (currentAnimal == null) return;

        // Cargar imagen
        if (currentAnimal.getFotoUrl() != null && !currentAnimal.getFotoUrl().isEmpty()) {
            Glide.with(this).load(currentAnimal.getFotoUrl()).into(ivPhoto);
        }

        tvName.setText(currentAnimal.getNombre());
        tvBreed.setText(currentAnimal.getEspecie() + " • " + currentAnimal.getRaza());
        tvSex.setText(currentAnimal.getSexo());
        tvStatus.setText(currentAnimal.getEstadoRefugio());

        String saludInfo = "Estado General: " + currentAnimal.getEstadoSalud();
        if (currentAnimal.getCondicionesEspeciales() != null && !currentAnimal.getCondicionesEspeciales().isEmpty()) {
            saludInfo += "\n\nCondiciones: " + currentAnimal.getCondicionesEspeciales().toString();
        }
        tvHealth.setText(saludInfo);
    }

    private void solicitarAdopcion() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Debes iniciar sesión para adoptar", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSolicitar.setEnabled(false);
        btnSolicitar.setText("Enviando solicitud...");

        // Crear objeto de solicitud (Map simple por ahora)
        Map<String, Object> solicitud = new HashMap<>();
        solicitud.put("idUsuario", mAuth.getCurrentUser().getUid());
        solicitud.put("emailUsuario", mAuth.getCurrentUser().getEmail());
        solicitud.put("idAnimal", currentAnimal.getIdAnimal());
        solicitud.put("nombreAnimal", currentAnimal.getNombre());
        solicitud.put("fechaSolicitud", new Date());
        solicitud.put("estadoSolicitud", "Pendiente"); // Pendiente, Aprobada, Rechazada

        // Guardar en Firestore
        db.collection("solicitudes_adopcion")
                .add(solicitud)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "✅ ¡Solicitud enviada con éxito! Te contactaremos pronto.", Toast.LENGTH_LONG).show();
                    finish(); // Cierra la pantalla y vuelve a la lista
                })
                .addOnFailureListener(e -> {
                    btnSolicitar.setEnabled(true);
                    btnSolicitar.setText("SOLICITAR ADOPCIÓN ❤️");
                    Toast.makeText(this, "Error al enviar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}