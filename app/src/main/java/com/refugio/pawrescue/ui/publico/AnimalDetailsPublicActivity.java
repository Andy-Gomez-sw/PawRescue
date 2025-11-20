package com.refugio.pawrescue.ui.publico;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.Animal;

public class AnimalDetailsPublicActivity extends AppCompatActivity {

    private Animal animal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animal_details_public);

        animal = (Animal) getIntent().getSerializableExtra("animal");

        if (animal == null) {
            Toast.makeText(this, "Error al cargar información del animal", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupUI();
    }

    private void setupUI() {
        ImageView ivPhoto = findViewById(R.id.ivAnimalDetailPhoto);
        TextView tvNombre = findViewById(R.id.tvDetailNombre);
        TextView tvRaza = findViewById(R.id.tvDetailRaza);
        TextView tvEdad = findViewById(R.id.tvDetailEdad);
        TextView tvSexo = findViewById(R.id.tvDetailSexo);
        TextView tvDescripcion = findViewById(R.id.tvDetailDescripcion);
        Button btnAdoptar = findViewById(R.id.btnSolicitarAdopcion);

        tvNombre.setText(animal.getNombre());
        tvRaza.setText(animal.getRaza());
        tvEdad.setText(animal.getEdadAprox());
        tvSexo.setText(animal.getSexo());
        tvDescripcion.setText("Especie: " + animal.getEspecie() + "\n" +
                "Estado: " + animal.getEstadoRefugio());

        Glide.with(this)
                .load(animal.getFotoUrl())
                .placeholder(R.drawable.ic_pet_placeholder)
                .error(R.drawable.ic_pet_error)
                .into(ivPhoto);

        btnAdoptar.setOnClickListener(v -> {
            Toast.makeText(this,
                    "Funcionalidad de solicitud de adopción en desarrollo",
                    Toast.LENGTH_SHORT).show();
        });
    }
}