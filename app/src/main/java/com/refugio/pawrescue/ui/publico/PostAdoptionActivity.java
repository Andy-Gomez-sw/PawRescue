package com.refugio.pawrescue.ui.publico;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.Animal;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostAdoptionActivity extends AppCompatActivity {

    private static final String TAG = "PostAdoptionActivity";
    private static final int REQUEST_TAKE_PHOTO = 1;

    private ImageButton btnBack;
    private ImageView ivAnimalPhoto;
    private TextView tvTitle, tvAdoptionDate;

    // Opciones de Ánimo
    private MaterialCardView cardExcellent, cardGood, cardRegular, cardAttention;

    // Checkboxes
    private MaterialCheckBox cbEatWell, cbActive, cbAdapted, cbVet;

    private EditText etComments;
    private GridLayout photoGrid;
    private MaterialCardView btnCamera, btnGallery;
    private MaterialButton btnSubmit;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;

    private String animalId;
    private String solicitudId;
    private String selectedMood = "";
    private List<Uri> photoUris = new ArrayList<>();
    private Uri currentPhotoUri;

    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_adoption);

        initViews();
        initFirebase();

        if (!getIntentData()) {
            return;
        }

        setupActivityLaunchers();
        setupMoodSelector();
        setupButtons();
        loadAnimalData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK && currentPhotoUri != null) {
            photoUris.add(currentPhotoUri);
            updatePhotoGrid();
            currentPhotoUri = null;
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        ivAnimalPhoto = findViewById(R.id.ivAnimalPhoto);
        tvTitle = findViewById(R.id.tvTitle);
        tvAdoptionDate = findViewById(R.id.tvAdoptionDate);

        cardExcellent = findViewById(R.id.cardExcellent);
        cardGood = findViewById(R.id.cardGood);
        cardRegular = findViewById(R.id.cardRegular);
        cardAttention = findViewById(R.id.cardAttention);

        cbEatWell = findViewById(R.id.cbEatWell);
        cbActive = findViewById(R.id.cbActive);
        cbAdapted = findViewById(R.id.cbAdapted);
        cbVet = findViewById(R.id.cbVet);

        etComments = findViewById(R.id.etComments);
        photoGrid = findViewById(R.id.photoGrid);
        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);
        btnSubmit = findViewById(R.id.btnSubmit);
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    private boolean getIntentData() {
        animalId = getIntent().getStringExtra("ANIMAL_ID");
        solicitudId = getIntent().getStringExtra("SOLICITUD_ID");

        if (animalId == null) {
            Toast.makeText(this, "Error: No se encontró el animal", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }
        return true;
    }

    private void setupActivityLaunchers() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        if (selectedImage != null) {
                            photoUris.add(selectedImage);
                            updatePhotoGrid();
                        }
                    }
                }
        );
    }

    // 🟢 LÓGICA DE COLORES DE ESTADO
    private void setupMoodSelector() {
        View.OnClickListener moodListener = v -> {
            resetMoodCards(); // 1. Resetear todos a blanco

            MaterialCardView selected = (MaterialCardView) v;

            // 2. Configurar el borde de selección (Naranja)
            int orangeColor = ContextCompat.getColor(this, R.color.primary_orange);
            selected.setStrokeColor(orangeColor);
            selected.setStrokeWidth(6);

            int id = v.getId();
            // 3. Cambiar el FONDO según la emoción (Colores pastel fuertes)
            if (id == R.id.cardExcellent) {
                selectedMood = "Excelente";
                selected.setCardBackgroundColor(Color.parseColor("#C8E6C9")); // Verde
            } else if (id == R.id.cardGood) {
                selectedMood = "Bien";
                selected.setCardBackgroundColor(Color.parseColor("#BBDEFB")); // Azul
            } else if (id == R.id.cardRegular) {
                selectedMood = "Regular";
                selected.setCardBackgroundColor(Color.parseColor("#FFF9C4")); // Amarillo
            } else if (id == R.id.cardAttention) {
                selectedMood = "Atención";
                selected.setCardBackgroundColor(Color.parseColor("#FFCDD2")); // Rojo
            }
        };

        cardExcellent.setOnClickListener(moodListener);
        cardGood.setOnClickListener(moodListener);
        cardRegular.setOnClickListener(moodListener);
        cardAttention.setOnClickListener(moodListener);
    }

    private void resetMoodCards() {
        int grayColor = ContextCompat.getColor(this, android.R.color.darker_gray);
        int whiteColor = Color.WHITE;

        // Restaurar todos a fondo BLANCO
        cardExcellent.setCardBackgroundColor(whiteColor);
        cardGood.setCardBackgroundColor(whiteColor);
        cardRegular.setCardBackgroundColor(whiteColor);
        cardAttention.setCardBackgroundColor(whiteColor);

        // Restaurar bordes GRISES
        cardExcellent.setStrokeColor(grayColor); cardExcellent.setStrokeWidth(2);
        cardGood.setStrokeColor(grayColor); cardGood.setStrokeWidth(2);
        cardRegular.setStrokeColor(grayColor); cardRegular.setStrokeWidth(2);
        cardAttention.setStrokeColor(grayColor); cardAttention.setStrokeWidth(2);
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnCamera.setOnClickListener(v -> {
            if (photoUris.size() < 5) openCamera();
            else Toast.makeText(this, "Máximo 5 fotos", Toast.LENGTH_SHORT).show();
        });

        btnGallery.setOnClickListener(v -> {
            if (photoUris.size() < 5) openGallery();
            else Toast.makeText(this, "Máximo 5 fotos", Toast.LENGTH_SHORT).show();
        });

        btnSubmit.setOnClickListener(v -> submitReport());
    }

    private void loadAnimalData() {
        db.collection("animales").document(animalId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Animal animal = documentSnapshot.toObject(Animal.class);
                        if (animal != null) {
                            tvTitle.setText("¿Cómo está " + animal.getNombre() + "?");
                            if (animal.getFotoUrl() != null) {
                                Glide.with(this).load(animal.getFotoUrl()).into(ivAnimalPhoto);
                            }
                        }
                    }
                });
    }

    private void openCamera() {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = createImageFile();
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            getApplicationContext().getPackageName() + ".fileprovider",
                            photoFile);
                    currentPhotoUri = photoURI;
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error abriendo cámara", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void updatePhotoGrid() {
        Toast.makeText(this, "Foto agregada (" + photoUris.size() + "/5)", Toast.LENGTH_SHORT).show();
    }

    private void submitReport() {
        if (selectedMood.isEmpty()) {
            Toast.makeText(this, "Selecciona el estado general", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "Sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Enviando...");

        Map<String, Object> reporte = new HashMap<>();
        reporte.put("animalId", animalId);
        reporte.put("solicitudId", solicitudId);
        reporte.put("usuarioId", userId);
        reporte.put("estadoGeneral", selectedMood);
        reporte.put("comeBien", cbEatWell.isChecked());
        reporte.put("activo", cbActive.isChecked());
        reporte.put("adaptado", cbAdapted.isChecked());
        reporte.put("visitaVet", cbVet.isChecked());
        reporte.put("comentarios", etComments.getText().toString().trim());
        reporte.put("fecha", new Date());

        if (!photoUris.isEmpty()) {
            uploadPhotos(reporte);
        } else {
            saveReport(reporte);
        }
    }

    private void uploadPhotos(Map<String, Object> reporte) {
        List<String> photoUrls = new ArrayList<>();
        final int[] uploadedCount = {0};

        for (Uri uri : photoUris) {
            String fileName = "seg_" + System.currentTimeMillis() + ".jpg";
            StorageReference ref = storage.getReference().child("seguimientos").child(animalId).child(fileName);

            ref.putFile(uri).addOnSuccessListener(taskSnapshot -> {
                ref.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    photoUrls.add(downloadUri.toString());
                    uploadedCount[0]++;
                    if (uploadedCount[0] == photoUris.size()) {
                        reporte.put("fotos", photoUrls);
                        saveReport(reporte);
                    }
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error al subir foto", Toast.LENGTH_SHORT).show();
                btnSubmit.setEnabled(true);
            });
        }
    }

    private void saveReport(Map<String, Object> reporte) {
        db.collection("seguimientos_post_adopcion")
                .add(reporte)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "¡Reporte enviado con éxito!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Enviar Reporte");
                });
    }
}