package com.refugio.pawrescue.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.refugio.pawrescue.R;

import java.util.ArrayList;
import java.util.List;
import com.google.firebase.firestore.FieldValue;

/**
 * Pantalla para asignar o quitar un voluntario a un animal.
 * Recibe por Intent:
 *  - "idAnimal" (String)
 *  - "nombreAnimal" (String)
 */
public class AssignVolunteerActivity extends AppCompatActivity {

    private TextView tvAssignTitleAnimal, tvAssignEmpty, tvCurrentVolunteer;
    private Spinner spinnerVoluntarios;
    private MaterialButton btnGuardar, btnQuitar;

    private FirebaseFirestore db;

    private String idAnimal;
    private String nombreAnimal;

    // Listas para mapear nombre mostrado -> UID voluntario
    private final List<String> nombresMostrados = new ArrayList<>();
    private final List<String> uidsVoluntarios = new ArrayList<>();

    // Para mostrar al inicio
    private String nombreVoluntarioActual;
    private String idVoluntarioActual;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_volunteer);

        db = FirebaseFirestore.getInstance();

        tvAssignTitleAnimal = findViewById(R.id.tvAssignAnimalName);
        tvAssignEmpty = findViewById(R.id.tvAssignEmpty);
        tvCurrentVolunteer = findViewById(R.id.tvCurrentVolunteer);
        spinnerVoluntarios = findViewById(R.id.spinnerVoluntarios);
        btnGuardar = findViewById(R.id.btnGuardarAsignacion);
        btnQuitar = findViewById(R.id.btnQuitarAsignacion);

        // Datos del animal desde el Intent
        idAnimal = getIntent().getStringExtra("idAnimal");
        nombreAnimal = getIntent().getStringExtra("nombreAnimal");

        if (nombreAnimal != null) {
            tvAssignTitleAnimal.setText("Animal: " + nombreAnimal);
        }

        // Cargar info actual del animal (si ya tiene voluntario)
        cargarInfoActualAnimal();

        // Cargar lista de voluntarios
        cargarVoluntariosActivos();

        btnGuardar.setOnClickListener(v -> guardarAsignacion());
        btnQuitar.setOnClickListener(v -> quitarAsignacion());
    }

    /**
     * Lee el documento del animal para saber si ya tiene voluntario asignado.
     */
    private void cargarInfoActualAnimal() {
        if (idAnimal == null || idAnimal.isEmpty()) {
            tvCurrentVolunteer.setText("Actualmente asignado a: Refugio (sin voluntario)");
            btnQuitar.setEnabled(false);
            return;
        }

        db.collection("animales")
                .document(idAnimal)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        nombreVoluntarioActual = doc.getString("nombreVoluntario");
                        Object idVol = doc.get("idVoluntario");
                        idVoluntarioActual = (idVol != null) ? idVol.toString() : null;

                        if (nombreVoluntarioActual != null && !nombreVoluntarioActual.isEmpty()) {
                            tvCurrentVolunteer.setText("Actualmente asignado a: " + nombreVoluntarioActual);
                            btnQuitar.setEnabled(true);
                        } else {
                            tvCurrentVolunteer.setText("Actualmente asignado a: Refugio (sin voluntario)");
                            btnQuitar.setEnabled(false);
                        }
                    } else {
                        tvCurrentVolunteer.setText("Actualmente asignado a: Refugio (sin voluntario)");
                        btnQuitar.setEnabled(false);
                    }
                })
                .addOnFailureListener(e -> {
                    tvCurrentVolunteer.setText("Actualmente asignado a: (error al cargar)");
                    btnQuitar.setEnabled(false);
                });
    }

    /**
     * Carga todos los voluntarios activos para el Spinner.
     */
    private void cargarVoluntariosActivos() {
        tvAssignEmpty.setVisibility(View.GONE);

        Query query = db.collection("usuarios")
                .whereEqualTo("rol", "Voluntario")
                .whereEqualTo("estadoActivo", true);

        query.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                tvAssignEmpty.setVisibility(View.VISIBLE);
                tvAssignEmpty.setText("Error al cargar voluntarios.");
                return;
            }

            nombresMostrados.clear();
            uidsVoluntarios.clear();

            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                String uid = doc.getId();
                String nombre = doc.getString("nombre");
                String correo = doc.getString("correo");

                if (nombre == null) nombre = "(Sin nombre)";

                String texto = nombre;
                if (correo != null && !correo.isEmpty()) {
                    texto = nombre + " - " + correo;
                }

                nombresMostrados.add(texto);
                uidsVoluntarios.add(uid);
            }

            if (nombresMostrados.isEmpty()) {
                tvAssignEmpty.setVisibility(View.VISIBLE);
                tvAssignEmpty.setText("No hay voluntarios activos para asignar.");
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    AssignVolunteerActivity.this,
                    android.R.layout.simple_spinner_item,
                    nombresMostrados
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerVoluntarios.setAdapter(adapter);
        });
    }

    /**
     * Asigna o cambia el voluntario del animal.
     */
    private void guardarAsignacion() {
        if (idAnimal == null || idAnimal.isEmpty()) {
            Toast.makeText(this, "Error: ID de animal no válido.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (uidsVoluntarios.isEmpty()) {
            Toast.makeText(this, "No hay voluntarios para asignar.", Toast.LENGTH_SHORT).show();
            return;
        }

        int pos = spinnerVoluntarios.getSelectedItemPosition();
        if (pos < 0 || pos >= uidsVoluntarios.size()) {
            Toast.makeText(this, "Selecciona un voluntario.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uidVoluntario = uidsVoluntarios.get(pos);
        String nombreMostrado = nombresMostrados.get(pos);

        db.collection("animales")
                .document(idAnimal)
                .update(
                        "idVoluntario", uidVoluntario,
                        "nombreVoluntario", nombreMostrado
                )
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Voluntario asignado correctamente.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(
                        this,
                        "Error al asignar voluntario: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
    }

    /**
     * Quita el voluntario actual del animal (lo deja en refugio sin voluntario).
     */
    private void quitarAsignacion() {
        if (idAnimal == null || idAnimal.isEmpty()) {
            Toast.makeText(this, "Error: ID de animal no válido.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Si ya no tiene voluntario, no hacemos nada
        if (idVoluntarioActual == null && nombreVoluntarioActual == null) {
            Toast.makeText(this, "Este animal ya no tiene voluntario asignado.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("animales")
                .document(idAnimal)
                .update(
                        "idVoluntario", FieldValue.delete(),
                        "nombreVoluntario", FieldValue.delete()
                )
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Voluntario retirado. El animal queda en el refugio principal.", Toast.LENGTH_SHORT).show();
                    // Actualizamos la UI
                    nombreVoluntarioActual = null;
                    idVoluntarioActual = null;
                    tvCurrentVolunteer.setText("Actualmente asignado a: Refugio (sin voluntario)");
                    btnQuitar.setEnabled(false);
                })
                .addOnFailureListener(e -> Toast.makeText(
                        this,
                        "Error al quitar voluntario: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
    }
}
