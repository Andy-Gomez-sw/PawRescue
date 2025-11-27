package com.refugio.pawrescue.ui.volunteer;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.Animal;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;



public class VolunteerSeguimientoDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ANIMAL = "extra_animal";

    private ImageView ivAnimalPhoto;
    private TextView tvAnimalName, tvFecha, tvComments, tvEmptyReport;

    private MaterialCardView cardExcellent, cardGood, cardRegular, cardAttention;
    private MaterialCheckBox cbEatWell, cbActive, cbAdapted, cbVet;

    private FirebaseFirestore db;
    private String animalId;

    private static final String TAG = "VolunteerSegDetalle";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_seguimiento_detail);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarSeguimiento);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Seguimiento Post-Adopción");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // Título del toolbar en negro
        toolbar.setTitleTextColor(
                androidx.core.content.ContextCompat.getColor(this, android.R.color.white)
        );

        // Recibir animal
        Animal animal = (Animal) getIntent().getSerializableExtra(EXTRA_ANIMAL);
        if (animal == null) {
            Toast.makeText(this, "Animal no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        animalId = animal.getIdAnimal();

        db = FirebaseFirestore.getInstance();

        // Referencias UI
        ivAnimalPhoto = findViewById(R.id.ivAnimalPhoto);
        tvAnimalName = findViewById(R.id.tvAnimalName);
        tvFecha = findViewById(R.id.tvFecha);
        tvComments = findViewById(R.id.tvComments);
        tvEmptyReport = findViewById(R.id.tvEmptyReport);

        cardExcellent = findViewById(R.id.cardExcellent);
        cardGood = findViewById(R.id.cardGood);
        cardRegular = findViewById(R.id.cardRegular);
        cardAttention = findViewById(R.id.cardAttention);

        cbEatWell = findViewById(R.id.cbEatWell);
        cbActive = findViewById(R.id.cbActive);
        cbAdapted = findViewById(R.id.cbAdapted);
        cbVet = findViewById(R.id.cbVet);

        // Datos del animal
        tvAnimalName.setText(animal.getNombre());
        if (animal.getFotoUrl() != null && !animal.getFotoUrl().isEmpty()) {
            Glide.with(this)
                    .load(animal.getFotoUrl())
                    .placeholder(R.drawable.ic_pet_placeholder)
                    .error(R.drawable.ic_pet_placeholder)
                    .into(ivAnimalPhoto);
        } else {
            ivAnimalPhoto.setImageResource(R.drawable.ic_pet_placeholder);
        }

        loadLastReport();
    }

    private void loadLastReport() {
        db.collection("seguimientos_post_adopcion")
                .whereEqualTo("animalId", animalId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        tvEmptyReport.setVisibility(View.VISIBLE);
                        tvComments.setText("Sin comentarios.");
                        return;
                    }

                    // Buscar el documento con la fecha más reciente
                    DocumentSnapshot lastDoc = null;
                    Date lastDate = null;
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Date d = doc.getDate("fecha");
                        if (lastDoc == null || (d != null && (lastDate == null || d.after(lastDate)))) {
                            lastDoc = doc;
                            lastDate = d;
                        }
                    }

                    if (lastDoc == null) {
                        tvEmptyReport.setVisibility(View.VISIBLE);
                        tvComments.setText("Sin comentarios.");
                        return;
                    }

                    String estadoGeneral = lastDoc.getString("estadoGeneral");
                    Boolean comeBien = lastDoc.getBoolean("comeBien");
                    Boolean activo = lastDoc.getBoolean("activo");
                    Boolean adaptado = lastDoc.getBoolean("adaptado");
                    Boolean visitaVet = lastDoc.getBoolean("visitaVet");
                    String comentarios = lastDoc.getString("comentarios");
                    Date fecha = lastDoc.getDate("fecha");

                    // Fecha
                    if (fecha != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                        tvFecha.setText("Último reporte: " + sdf.format(fecha));
                    } else {
                        tvFecha.setText("Último reporte: --/--/----");
                    }

                    // Comentarios
                    if (comentarios != null && !comentarios.trim().isEmpty()) {
                        tvComments.setText(comentarios);
                    } else {
                        tvComments.setText("Sin comentarios.");
                    }

                    // Checkboxes
                    cbEatWell.setChecked(Boolean.TRUE.equals(comeBien));
                    cbActive.setChecked(Boolean.TRUE.equals(activo));
                    cbAdapted.setChecked(Boolean.TRUE.equals(adaptado));
                    cbVet.setChecked(Boolean.TRUE.equals(visitaVet));

                    // Ocultar mensaje vacío
                    tvEmptyReport.setVisibility(View.GONE);

                    // Estado general
                    highlightMoodCard(estadoGeneral);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar el seguimiento", e);
                    Toast.makeText(this, "Error al cargar el seguimiento", Toast.LENGTH_SHORT).show();
                });
    }

    // --- Solo UNA vez estos métodos ---

    private void highlightMoodCard(String estadoGeneral) {
        // Limpiar todos
        resetCard(cardExcellent);
        resetCard(cardGood);
        resetCard(cardRegular);
        resetCard(cardAttention);

        if (estadoGeneral == null) return;

        MaterialCardView selected = null;
        switch (estadoGeneral) {
            case "Excelente":
                selected = cardExcellent;
                break;
            case "Bien":
                selected = cardGood;
                break;
            case "Regular":
                selected = cardRegular;
                break;
            case "Atención":
                selected = cardAttention;
                break;
        }

        if (selected != null) {
            selected.setStrokeWidth(4);
            selected.setStrokeColor(
                    androidx.core.content.ContextCompat.getColor(this, R.color.primary_orange)
            );
        }
    }

    private void resetCard(MaterialCardView card) {
        card.setStrokeWidth(0);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
