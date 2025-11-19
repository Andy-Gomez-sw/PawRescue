package com.refugio.pawrescue.ui.admin;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.data.helper.FirebaseHelper;
import com.refugio.pawrescue.model.Animal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Activity para el registro inicial de un Animal Rescatado (RF-05, RF-11).
 */
public class RegistroAnimalActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final String TAG = "RegistroAnimalActivity";

    // Componentes UI
    private TextInputEditText etNombre, etRaza, etUbicacionManual;
    private Spinner spinnerEspecie;
    private RadioGroup rgSexo;
    private CheckBox cbHeridas, cbDesnutrido, cbPrenada;
    private Button btnTomarFoto, btnObtenerGps, btnGuardarAnimal;
    private ImageView ivFotoPreview;
    private TextView tvGpsStatus;

    // Datos del Animal
    private Bitmap fotoBitmap;
    private GeoPoint ubicacionGPS;

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

        // Listeners
        btnTomarFoto.setOnClickListener(v -> solicitarPermisosYCapturarFoto());
        btnObtenerGps.setOnClickListener(v -> solicitarPermisosYObtenerGPS());
        btnGuardarAnimal.setOnClickListener(v -> intentarGuardarAnimal());
    }

    // --- MANEJO DE PERMISOS ---

    private void solicitarPermisosYCapturarFoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CODE);
        } else {
            capturarFoto();
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
                // Permiso concedido
                if (permissions[0].equals(Manifest.permission.CAMERA)) {
                    capturarFoto();
                } else if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    obtenerUbicacionGPS();
                }
            } else {
                Toast.makeText(this, "Permiso denegado. Algunas funciones no estarán disponibles.", Toast.LENGTH_SHORT).show();
                // (RF-11 Excepción 1.1) Permite ingreso manual de dirección como alternativa.
            }
        }
    }

    // --- MANEJO DE CÁMARA (RF-05) ---

    private void capturarFoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                fotoBitmap = (Bitmap) data.getExtras().get("data");
                ivFotoPreview.setImageBitmap(fotoBitmap);
                ivFotoPreview.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Foto capturada. Lista para subir.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // --- MANEJO DE GPS (RF-11) ---

    private void obtenerUbicacionGPS() {
        tvGpsStatus.setText("Estado: Obteniendo coordenadas...");

        // Usar FusedLocationProviderClient para obtener la mejor ubicación
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            ubicacionGPS = new GeoPoint(location.getLatitude(), location.getLongitude());
                            tvGpsStatus.setText("Estado: GPS Registrado. Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude());
                            etUbicacionManual.setText("Ubicación GPS automática");
                            etUbicacionManual.setEnabled(false); // Bloquear edición manual si se usó GPS
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

        // Crear el objeto Animal (los setters se usan para poblar el POJO)
        Animal nuevoAnimal = new Animal();
        nuevoAnimal.setNombre(etNombre.getText().toString().trim());
        nuevoAnimal.setEspecie(spinnerEspecie.getSelectedItem().toString());
        nuevoAnimal.setRaza(etRaza.getText().toString().trim());
        nuevoAnimal.setSexo(obtenerSexoSeleccionado());
        nuevoAnimal.setEstadoSalud("Estable"); // Valor inicial por defecto
        nuevoAnimal.setEstadoRefugio("Rescatado"); // Primer estado por defecto (RF-08)
        nuevoAnimal.setFechaRegistro(new Timestamp(new Date()));
        nuevoAnimal.setCondicionesEspeciales(obtenerCondicionesEspeciales());

        // Manejar Ubicación
        if (ubicacionGPS != null) {
            nuevoAnimal.setUbicacionRescate(ubicacionGPS);
        } else {
            // Si el GPS falla, se usa la ubicación manual para fines de trazabilidad.
            // Nota: En Firestore GeoPoint es preferible, pero aquí usamos un placeholder.
            // Para GeoPoint obligatorio, se podría crear un GeoPoint(0,0) con nota en ubicacionManual.
            Log.w(TAG, "Ubicación GPS no disponible. Usando Ubicación Manual.");
            // En este ejemplo simple, si el GPS falló, se asume que la ubicación manual se registra en el campo notas, o simplemente se deja el campo GeoPoint como null.
        }

        // Iniciar el proceso asíncrono de subida de foto y guardado de datos
        btnGuardarAnimal.setEnabled(false); // Deshabilitar para evitar duplicados
        Toast.makeText(this, "Guardando animal y subiendo foto...", Toast.LENGTH_SHORT).show();

        firebaseHelper.registrarAnimalConFoto(nuevoAnimal, fotoBitmap, new FirebaseHelper.GuardadoAnimalCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(RegistroAnimalActivity.this, message, Toast.LENGTH_LONG).show();
                btnGuardarAnimal.setEnabled(true);
                finish(); // Cerrar Activity y regresar al listado
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(RegistroAnimalActivity.this, error, Toast.LENGTH_LONG).show();
                btnGuardarAnimal.setEnabled(true);
            }
        });
    }

    private boolean validarCampos() {
        if (etNombre.getText().toString().trim().isEmpty() || etRaza.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "El nombre y la raza son obligatorios (RF-05).", Toast.LENGTH_LONG).show();
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