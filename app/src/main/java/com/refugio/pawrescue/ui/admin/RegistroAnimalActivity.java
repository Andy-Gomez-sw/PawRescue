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
import com.google.firebase.firestore.GeoPoint;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.data.helper.FirebaseHelper;
import com.refugio.pawrescue.model.Animal;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activity para el registro inicial de un Animal Rescatado (RF-05, RF-11).
 * OPTIMIZADO: Incluye redimensionamiento de imágenes y mantiene ambos métodos de cámara.
 */
public class RegistroAnimalActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final int REQUEST_TAKE_PHOTO = 101;
    private static final String TAG = "RegistroAnimalActivity";

    // Dimensiones máximas para la imagen
    private static final int MAX_IMAGE_DIMENSION = 1920;

    // Componentes UI
    private TextInputEditText etNombre, etRaza, etUbicacionManual;
    private Spinner spinnerEspecie;
    private RadioGroup rgSexo;
    private CheckBox cbHeridas, cbDesnutrido, cbPrenada;
    private Button btnTomarFoto, btnObtenerGps, btnGuardarAnimal;
    private ImageView ivFotoPreview;
    private TextView tvGpsStatus;
    private ProgressBar progressBar;

    // Datos del Animal
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

        // ProgressBar (opcional)
        progressBar = findViewById(R.id.progress_bar_registro);
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        // Listeners
        btnTomarFoto.setOnClickListener(v -> solicitarPermisosYCapturarFoto());
        btnObtenerGps.setOnClickListener(v -> solicitarPermisosYObtenerGPS());
        btnGuardarAnimal.setOnClickListener(v -> intentarGuardarAnimal());
    }

    // --- MANEJO DE PERMISOS ---

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

    // --- MANEJO DE CÁMARA (AMBOS MÉTODOS) ---

    /**
     * Crea un archivo temporal para guardar la imagen de alta resolución.
     */
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

    /**
     * MÉTODO 1: Inicia la Intent de la cámara (funciona en algunos dispositivos).
     */
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
                abrirCualquierCamara(); // Usar el método alternativo
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al abrir cámara (Método 1): " + e.getMessage());
            Toast.makeText(this, "Error al abrir cámara. Intentando método alternativo...", Toast.LENGTH_SHORT).show();
            abrirCualquierCamara(); // Intentar con el método alternativo
        }
    }

    /**
     * MÉTODO 2: Alternativa de cámara (funciona en otros dispositivos).
     */
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

    /**
     * NUEVA FUNCIÓN: Carga y redimensiona una imagen de forma eficiente.
     * Esto reduce el tamaño del archivo antes de subir a Firebase.
     */
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

    // --- MANEJO DE GPS (RF-11) ---

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
                            etUbicacionManual.setText("Ubicación GPS automática");
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

    // --- LÓGICA DE GUARDADO (RF-05) ---

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
            String coordenadas = ubicacionGPS.getLatitude() + "," + ubicacionGPS.getLongitude();
            nuevoAnimal.setUbicacionRescate(coordenadas);
        } else {
            nuevoAnimal.setUbicacionRescate(null);
        }

        // Mostrar progreso
        btnGuardarAnimal.setEnabled(false);
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        Toast.makeText(this, "Guardando animal y subiendo foto...", Toast.LENGTH_SHORT).show();

        Log.d(TAG, "🚀 Iniciando subida de animal: " + nuevoAnimal.getNombre());

        // Utilizar el Bitmap redimensionado (más eficiente)
        firebaseHelper.registrarAnimalConFoto(nuevoAnimal, fotoBitmap, new FirebaseHelper.GuardadoAnimalCallback() {
            @Override
            public void onSuccess(String message) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
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
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
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