package com.refugio.pawrescue.ui.admin;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.data.helper.FirebaseHelper;
import com.refugio.pawrescue.model.Animal;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Activity para el registro inicial de un Animal Rescatado (RF-05) y su EDICIÓN (RF-07).
 */
public class RegistroAnimalActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final int REQUEST_TAKE_PHOTO = 101;
    private static final String TAG = "RegistroAnimalActivity";
    private static final int MAX_IMAGE_DIMENSION = 1920;

    // Componentes UI
    private TextView tvTitle;
    private TextInputEditText etNombre, etRaza, etUbicacionManual;
    private Spinner spinnerEspecie;
    private RadioGroup rgSexo;
    private CheckBox cbHeridas, cbDesnutrido, cbPrenada;
    private Button btnTomarFoto, btnObtenerGps, btnGuardarAnimal;
    private ImageView ivFotoPreview;
    private TextView tvGpsStatus;
    private ProgressBar progressBar;

    // Modo y Datos del Animal
    private boolean isEditMode = false;
    private String animalId = null;
    private Animal originalAnimal = null;
    private Bitmap fotoBitmap;
    private GeoPoint ubicacionGPS;
    private String currentPhotoPath;

    // Servicios
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_animal);

        // Inicializar Servicios
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        firebaseHelper = new FirebaseHelper();

        // Enlazar componentes
        tvTitle = findViewById(R.id.tv_registro_title);
        etNombre = findViewById(R.id.et_nombre);
        etRaza = findViewById(R.id.et_raza);
        etUbicacionManual = findViewById(R.id.et_ubicacion_manual);
        spinnerEspecie = findViewById(R.id.spinner_especie);
        rgSexo = findViewById(R.id.rg_sexo);
        cbHeridas = findViewById(R.id.cb_heridas);
        cbDesnutrido = findViewById(R.id.cb_desnutrido);
        cbPrenada = findViewById(R.id.cb_prenada);
        btnTomarFoto = findViewById(R.id.btn_tomar_foto);
        btnObtenerGps = findViewById(R.id.btn_obtener_gps);
        btnGuardarAnimal = findViewById(R.id.btn_guardar_animal);
        ivFotoPreview = findViewById(R.id.iv_foto_preview);
        tvGpsStatus = findViewById(R.id.tv_gps_status);
        progressBar = findViewById(R.id.progress_bar_registro);

        progressBar.setVisibility(View.GONE);

        // Verificar Modo (Registro vs. Edición)
        isEditMode = getIntent().getBooleanExtra("isEditMode", false);
        animalId = getIntent().getStringExtra("animalId");

        if (isEditMode && animalId != null) {
            setupEditMode();
            cargarDatosParaEdicion(animalId);
        } else {
            setupRegistroMode();
        }

        // Listeners
        btnTomarFoto.setOnClickListener(v -> solicitarPermisosYCapturarFoto());
        btnObtenerGps.setOnClickListener(v -> solicitarPermisosYObtenerGPS());
        btnGuardarAnimal.setOnClickListener(v -> {
            if (isEditMode) {
                intentarActualizarAnimal(); // Lógica de edición
            } else {
                intentarGuardarAnimal(); // Lógica de registro nuevo
            }
        });

        // Habilitar flecha de regreso en el ActionBar si es necesario
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // --- CONFIGURACIÓN DE MODOS ---

    private void setupRegistroMode() {
        tvTitle.setText("Registro de Animal Rescatado (RF-05)");
        btnGuardarAnimal.setText("Guardar Animal Rescatado");
        btnTomarFoto.setVisibility(View.VISIBLE);
        btnObtenerGps.setVisibility(View.VISIBLE);
    }

    private void setupEditMode() {
        tvTitle.setText("Editar Expediente (RF-07)");
        btnGuardarAnimal.setText("GUARDAR CAMBIOS");
        // Ocultar botones de foto y GPS para edición (se asume que la foto/ubicación inicial ya están)
        btnTomarFoto.setVisibility(View.GONE);
        btnObtenerGps.setVisibility(View.GONE);
        tvGpsStatus.setText("Modo Edición. Ubicación Rescate: ");
        // Bloquear ubicación manual para no alterar la inicial a menos que se use un campo específico de lat/lon.
        etUbicacionManual.setEnabled(false);
    }

    // --- LÓGICA DE EDICIÓN (RF-07) ---

    private void cargarDatosParaEdicion(String id) {
        firebaseHelper.getDb().collection("animales").document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        originalAnimal = documentSnapshot.toObject(Animal.class);
                        if (originalAnimal != null) {
                            popularFormulario(originalAnimal);
                            Log.d(TAG, "Datos cargados para edición: " + originalAnimal.getNombre());
                        } else {
                            Toast.makeText(this, "Error al mapear datos para edición.", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Animal no encontrado para edición.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error cargando datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void popularFormulario(Animal animal) {
        // Datos Básicos
        etNombre.setText(animal.getNombre());
        etRaza.setText(animal.getRaza());

        // Especie
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.animal_species_array, android.R.layout.simple_spinner_dropdown_item);
        if (animal.getEspecie() != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).toString().equalsIgnoreCase(animal.getEspecie())) {
                    spinnerEspecie.setSelection(i);
                    break;
                }
            }
        }

        // Sexo
        if (animal.getSexo() != null) {
            if (animal.getSexo().equalsIgnoreCase("Macho")) {
                rgSexo.check(R.id.rb_macho);
            } else if (animal.getSexo().equalsIgnoreCase("Hembra")) {
                rgSexo.check(R.id.rb_hembra);
            } else {
                rgSexo.check(R.id.rb_desconocido);
            }
        }

        // Condiciones
        if (animal.getCondicionesEspeciales() != null) {
            cbHeridas.setChecked(animal.getCondicionesEspeciales().contains(cbHeridas.getText().toString()));
            cbDesnutrido.setChecked(animal.getCondicionesEspeciales().contains(cbDesnutrido.getText().toString()));
            cbPrenada.setChecked(animal.getCondicionesEspeciales().contains(cbPrenada.getText().toString()));
        }

        // Ubicación
        tvGpsStatus.setText("Ubicación Rescate: " + animal.getUbicacionRescate());
        etUbicacionManual.setText(animal.getUbicacionRescate()); // Mostrar valor en campo

        // No precargamos la foto en la preview de esta activity en modo edición, ya que se ve en DetalleAnimalActivity.
    }

    private void intentarActualizarAnimal() {
        if (animalId == null || !validarCampos()) return;

        // Recolectar datos del formulario
        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", etNombre.getText().toString().trim());
        updates.put("especie", spinnerEspecie.getSelectedItem().toString());
        updates.put("raza", etRaza.getText().toString().trim());
        updates.put("sexo", obtenerSexoSeleccionado());
        updates.put("condicionesEspeciales", obtenerCondicionesEspeciales());
        updates.put("ubicacionRescate", etUbicacionManual.getText().toString().trim());
        updates.put("fechaUltimaActualizacion", new Timestamp(new Date()));

        // Ocultar botones y mostrar progreso
        btnGuardarAnimal.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Actualizando expediente...", Toast.LENGTH_SHORT).show();

        firebaseHelper.actualizarAnimal(animalId, updates, new FirebaseHelper.GuardadoAnimalCallback() {
            @Override
            public void onSuccess(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(RegistroAnimalActivity.this, message, Toast.LENGTH_LONG).show();
                Log.d(TAG, "✅ Expediente actualizado exitosamente.");
                finish(); // Regresar a DetalleAnimalActivity
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                btnGuardarAnimal.setEnabled(true);
                Toast.makeText(RegistroAnimalActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "❌ Error al actualizar animal: " + error);
            }
        });
    }

    // --- MANEJO DE PERMISOS, CÁMARA, GPS y GUARDADO (sin cambios en su lógica) ---

    private void solicitarPermisosYCapturarFoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
        } else {
            abrirCamara();
        }
    }

    private void solicitarPermisosYObtenerGPS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_CODE);
        } else {
            obtenerUbicacionGPS();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (permissions[0].equals(Manifest.permission.CAMERA)) {
                    abrirCamara();
                } else if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    obtenerUbicacionGPS();
                }
            } else {
                Toast.makeText(this, "Permiso denegado. Algunas funciones no estarán disponibles.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "ANIMAL_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs();
        }

        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();

        Log.d(TAG, "Archivo creado: " + currentPhotoPath);
        return image;
    }

    private void abrirCamara() {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                    Log.d(TAG, "Archivo creado: " + (photoFile != null ? photoFile.getAbsolutePath() : "null"));
                } catch (IOException ex) {
                    Toast.makeText(this, "Error al crear archivo temporal", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al crear archivo: " + ex.getMessage());
                    return;
                }

                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            getApplicationContext().getPackageName() + ".fileprovider",
                            photoFile);

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                    Log.d(TAG, "Iniciando actividad de cámara con URI (Método 1): " + photoURI);
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            } else {
                Log.d(TAG, "No se encontró app de cámara compatible con FileProvider (Método 1)");
                abrirCualquierCamara();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al abrir cámara (Método 1): " + e.getMessage());
            Toast.makeText(this, "Error al abrir cámara. Intentando método alternativo...", Toast.LENGTH_SHORT).show();
            abrirCualquierCamara();
        }
    }

    private void abrirCualquierCamara() {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);

        try {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, "Error al crear archivo (Método 2): " + ex.getMessage());
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        getApplicationContext().getPackageName() + ".fileprovider",
                        photoFile);

                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }

            Log.d(TAG, "Iniciando actividad de cámara (Método 2)");
            startActivityForResult(intent, REQUEST_TAKE_PHOTO);
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo encontrar app de cámara", Toast.LENGTH_LONG).show();
            Log.e(TAG, "No hay app de cámara (Método 2): " + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult - requestCode: " + requestCode + ", resultCode: " + resultCode);

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            if (currentPhotoPath != null && new File(currentPhotoPath).exists()) {
                try {
                    // OPTIMIZACIÓN: Cargar y redimensionar la imagen
                    fotoBitmap = cargarYRedimensionarImagen(currentPhotoPath);

                    if (fotoBitmap != null) {
                        ivFotoPreview.setImageBitmap(fotoBitmap);
                        ivFotoPreview.setVisibility(View.VISIBLE);

                        int width = fotoBitmap.getWidth();
                        int height = fotoBitmap.getHeight();

                        Toast.makeText(this,
                                String.format("Foto capturada: %dx%d px", width, height),
                                Toast.LENGTH_SHORT).show();

                        Log.d(TAG, "✅ Foto procesada exitosamente: " + width + "x" + height);
                    } else {
                        Toast.makeText(this, "Error al procesar la imagen.", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "❌ Bitmap es null después de procesar");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error al cargar imagen: " + e.getMessage());
                    Toast.makeText(this, "Error al cargar imagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Error: Ruta de archivo no encontrada.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "❌ Archivo no existe: " + currentPhotoPath);
            }
        } else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Captura de foto cancelada.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Usuario canceló la captura");
        }
    }

    private Bitmap cargarYRedimensionarImagen(String photoPath) {
        // Primero, obtener las dimensiones sin cargar toda la imagen
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, options);

        int originalWidth = options.outWidth;
        int originalHeight = options.outHeight;

        Log.d(TAG, "📐 Dimensiones originales: " + originalWidth + "x" + originalHeight);

        // Calcular factor de escala
        int scaleFactor = 1;
        if (originalWidth > MAX_IMAGE_DIMENSION || originalHeight > MAX_IMAGE_DIMENSION) {
            scaleFactor = Math.max(
                    originalWidth / MAX_IMAGE_DIMENSION,
                    originalHeight / MAX_IMAGE_DIMENSION
            );
        }

        Log.d(TAG, "🔍 Factor de escala: " + scaleFactor);

        // Cargar la imagen con el factor de escala
        options.inJustDecodeBounds = false;
        options.inSampleSize = scaleFactor;
        options.inPreferredConfig = Bitmap.Config.RGB_565; // Usa menos memoria

        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, options);

        if (bitmap != null) {
            Log.d(TAG, "✅ Imagen redimensionada a: " + bitmap.getWidth() + "x" + bitmap.getHeight());
        } else {
            Log.e(TAG, "❌ Error: Bitmap es null después de decodificar");
        }

        return bitmap;
    }

    private void obtenerUbicacionGPS() {
        tvGpsStatus.setText("Estado: Obteniendo coordenadas...");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            tvGpsStatus.setText("Estado: Sin permisos de ubicación");
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            ubicacionGPS = new GeoPoint(location.getLatitude(), location.getLongitude());
                            tvGpsStatus.setText(String.format("GPS Registrado\nLat: %.6f, Lon: %.6f",
                                    location.getLatitude(), location.getLongitude()));
                            etUbicacionManual.setText(String.format(Locale.US, "%.6f,%.6f", location.getLatitude(), location.getLongitude()));
                            etUbicacionManual.setEnabled(false);
                            Toast.makeText(RegistroAnimalActivity.this, "Ubicación registrada con éxito.", Toast.LENGTH_SHORT).show();
                        } else {
                            tvGpsStatus.setText("Estado: GPS Fallido. Ingrese manualmente (RF-11).");
                            etUbicacionManual.setEnabled(true);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    tvGpsStatus.setText("Estado: Error de GPS. Ingrese manualmente (RF-11).");
                    etUbicacionManual.setEnabled(true);
                    Log.e(TAG, "Error al obtener ubicación: ", e);
                });
    }

    // --- LÓGICA DE GUARDADO NUEVO (RF-05) ---

    private void intentarGuardarAnimal() {
        if (!validarCampos()) {
            return;
        }

        if (fotoBitmap == null) {
            Toast.makeText(this, "La fotografía del animal es obligatoria.", Toast.LENGTH_LONG).show();
            return;
        }

        // Crear el objeto Animal
        Animal nuevoAnimal = new Animal();
        nuevoAnimal.setNombre(etNombre.getText().toString().trim());
        nuevoAnimal.setEspecie(spinnerEspecie.getSelectedItem().toString());
        nuevoAnimal.setRaza(etRaza.getText().toString().trim());
        nuevoAnimal.setSexo(obtenerSexoSeleccionado());
        nuevoAnimal.setEstadoSalud("Estable");
        nuevoAnimal.setEstadoRefugio("Rescatado");
        nuevoAnimal.setFechaRegistro(new Date());
        nuevoAnimal.setCondicionesEspeciales(obtenerCondicionesEspeciales());

        // Manejar Ubicación
        if (ubicacionGPS != null) {
            // Guardamos la ubicación de la caja de texto, que ya fue poblada con lat,lon
            nuevoAnimal.setUbicacionRescate(etUbicacionManual.getText().toString().trim());
        } else {
            nuevoAnimal.setUbicacionRescate(etUbicacionManual.getText().toString().trim());
        }

        // Mostrar progreso
        btnGuardarAnimal.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Guardando animal y subiendo foto...", Toast.LENGTH_SHORT).show();

        Log.d(TAG, "🚀 Iniciando subida de animal: " + nuevoAnimal.getNombre());

        // Utilizar el Bitmap redimensionado (más eficiente)
        firebaseHelper.registrarAnimalConFoto(nuevoAnimal, fotoBitmap, new FirebaseHelper.GuardadoAnimalCallback() {
            @Override
            public void onSuccess(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(RegistroAnimalActivity.this, message, Toast.LENGTH_LONG).show();

                // Limpiar archivo temporal
                if (currentPhotoPath != null) {
                    File file = new File(currentPhotoPath);
                    if (file.exists()) {
                        boolean deleted = file.delete();
                        Log.d(TAG, "🗑️ Archivo temporal " + (deleted ? "eliminado" : "no se pudo eliminar"));
                    }
                }

                Log.d(TAG, "✅ Animal registrado exitosamente");
                finish(); // Cerrar Activity y regresar al listado
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                btnGuardarAnimal.setEnabled(true);
                Toast.makeText(RegistroAnimalActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "❌ Error al guardar animal: " + error);
            }
        });
    }

    private boolean validarCampos() {
        String nombre = etNombre.getText().toString().trim();
        String raza = etRaza.getText().toString().trim();

        if (nombre.isEmpty()) {
            Toast.makeText(this, "El nombre del animal es obligatorio (RF-05).", Toast.LENGTH_LONG).show();
            etNombre.requestFocus();
            return false;
        }

        if (raza.isEmpty()) {
            Toast.makeText(this, "La raza es obligatoria (RF-05).", Toast.LENGTH_LONG).show();
            etRaza.requestFocus();
            return false;
        }

        return true;
    }

    private String obtenerSexoSeleccionado() {
        int selectedId = rgSexo.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton selectedRadioButton = findViewById(selectedId);
            return selectedRadioButton.getText().toString();
        }
        return "Desconocido";
    }

    private List<String> obtenerCondicionesEspeciales() {
        List<String> condiciones = new ArrayList<>();
        if (cbHeridas.isChecked()) condiciones.add(cbHeridas.getText().toString());
        if (cbDesnutrido.isChecked()) condiciones.add(cbDesnutrido.getText().toString());
        if (cbPrenada.isChecked()) condiciones.add(cbPrenada.getText().toString());
        return condiciones;
    }
}