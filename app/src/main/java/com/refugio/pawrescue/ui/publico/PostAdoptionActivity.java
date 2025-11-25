package com.refugio.pawrescue.ui.publico;

import android.content.Intent;
import android.net.Uri;
import com.refugio.pawrescue.model.Animal;
import com.refugio.pawrescue.R;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PostAdoptionActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ImageView ivAnimalPhoto;
    private TextView tvTitle;
    private TextView tvAdoptionDate;
    private MaterialCardView cardExcellent;
    private MaterialCardView cardGood;
    private MaterialCardView cardRegular;
    private MaterialCardView cardAttention;
    private MaterialCheckBox cbEatWell;
    private MaterialCheckBox cbActive;
    private MaterialCheckBox cbAdapted;
    private MaterialCheckBox cbVet;
    private EditText etComments;
    private GridLayout photoGrid;
    private MaterialCardView btnCamera;
    private MaterialCardView btnGallery;
    private MaterialButton btnSubmit;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;

    private String animalId;
    private String animalName;
    private String selectedMood = "";
    private List<Uri> photoUris = new ArrayList<>();
    private Uri currentPhotoUri;

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_adoption);

        initViews();
        initFirebase();
        getIntentData();
        setupActivityLaunchers();
        setupMoodSelector();
        setupButtons();
        loadAnimalData();
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

    private void getIntentData() {
        animalId = getIntent().getStringExtra("ANIMAL_ID");
        animalName = getIntent().getStringExtra("ANIMAL_NAME");
    }

    private void setupActivityLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && currentPhotoUri != null) {
                        photoUris.add(currentPhotoUri);
                        updatePhotoGrid();
                    }
                }
        );

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

    private void setupMoodSelector() {
        View.OnClickListener moodListener = v -> {
            // Resetear todos
            resetMoodCards();

            // Seleccionar el clickeado
            MaterialCardView selected = (MaterialCardView) v;
            selected.setStrokeColor(getColor(R.color.primary_orange));
            selected.setStrokeWidth(4);

            if (v.getId() == R.id.cardExcellent) {
                selectedMood = "excellent";
            } else if (v.getId() == R.id.cardGood) {
                selectedMood = "good";
            } else if (v.getId() == R.id.cardRegular) {
                selectedMood = "regular";
            } else if (v.getId() == R.id.cardAttention) {
                selectedMood = "attention";
            }
        };

        cardExcellent.setOnClickListener(moodListener);
        cardGood.setOnClickListener(moodListener);
        cardRegular.setOnClickListener(moodListener);
        cardAttention.setOnClickListener(moodListener);
    }

    private void resetMoodCards() {
        cardExcellent.setStrokeColor(getColor(R.color.gray_light));
        cardExcellent.setStrokeWidth(2);
        cardGood.setStrokeColor(getColor(R.color.gray_light));
        cardGood.setStrokeWidth(2);
        cardRegular.setStrokeColor(getColor(R.color.gray_light));
        cardRegular.setStrokeWidth(2);
        cardAttention.setStrokeColor(getColor(R.color.gray_light));
        cardAttention.setStrokeWidth(2);
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnCamera.setOnClickListener(v -> {
            if (photoUris.size() < 5) {
                openCamera();
            } else {
                Toast.makeText(this, "Máximo 5 fotos", Toast.LENGTH_SHORT).show();
            }
        });

        btnGallery.setOnClickListener(v -> {
            if (photoUris.size() < 5) {
                openGallery();
            } else {
                Toast.makeText(this, "Máximo 5 fotos", Toast.LENGTH_SHORT).show();
            }
        });

        btnSubmit.setOnClickListener(v -> {
            submitReport();
        });
    }

    private void loadAnimalData() {
        db.collection("animales").document(animalId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Animal animal = documentSnapshot.toObject(Animal.class);
                        if (animal != null) {
                            tvTitle.setText("¿Cómo está " + animal.getNombre() + "?");

                            if (animal.getFotoUrl() != null) {
                                Glide.with(this)
                                        .load(animal.getFotoUrl())
                                        .into(ivAnimalPhoto);
                            }
                        }
                    }
                });
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Error al crear archivo", Toast.LENGTH_SHORT).show();
            }

            if (photoFile != null) {
                currentPhotoUri = FileProvider.getUriForFile(this,
                        "com.pawrescue.app.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
                cameraLauncher.launch(takePictureIntent);
            }
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
        // Limpiar grid excepto los botones de cámara y galería
        int childCount = photoGrid.getChildCount();
        for (int i = childCount - 1; i >= 2; i--) {
            photoGrid.removeViewAt(i);
        }

        // Agregar fotos
        for (int i = 0; i < photoUris.size(); i++) {
            final int index = i;
            View photoView = getLayoutInflater().inflate(R.layout.item_photo_preview, photoGrid, false);
            ImageView ivPhoto = photoView.findViewById(R.id.ivPhoto);
            ImageButton btnRemove = photoView.findViewById(R.id.btnRemove);

            Glide.with(this).load(photoUris.get(i)).into(ivPhoto);

            btnRemove.setOnClickListener(v -> {
                photoUris.remove(index);
                updatePhotoGrid();
            });

            photoGrid.addView(photoView);
        }
    }

    private void submitReport() {
        if (selectedMood.isEmpty()) {
            Toast.makeText(this, "Por favor selecciona el estado general", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) return;

        // Preparar datos del reporte
        Map<String, Object> reporte = new HashMap<>();
        reporte.put("animalId", animalId);
        reporte.put("animalNombre", animalName);
        reporte.put("usuarioId", userId);
        reporte.put("estadoGeneral", selectedMood);
        reporte.put("comeB bien", cbEatWell.isChecked());
        reporte.put("activo", cbActive.isChecked());
        reporte.put("adaptado", cbAdapted.isChecked());
        reporte.put("visitaVet", cbVet.isChecked());
        reporte.put("comentarios", etComments.getText().toString().trim());
        reporte.put("fecha", new Date());

        // Subir fotos primero
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
            String fileName = "seguimiento_" + System.currentTimeMillis() + ".jpg";
            StorageReference photoRef = storage.getReference()
                    .child("seguimientos")
                    .child(animalId)
                    .child(fileName);

            photoRef.putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> {
                        photoRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            photoUrls.add(downloadUri.toString());
                            uploadedCount[0]++;

                            if (uploadedCount[0] == photoUris.size()) {
                                reporte.put("fotos", photoUrls);
                                saveReport(reporte);
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al subir foto", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void saveReport(Map<String, Object> reporte) {
        db.collection("seguimientos_post_adopcion")
                .add(reporte)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "¡Reporte enviado! Gracias por compartir",
                            Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar reporte: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}