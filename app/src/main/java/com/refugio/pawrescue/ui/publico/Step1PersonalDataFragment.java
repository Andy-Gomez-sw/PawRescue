package com.refugio.pawrescue.ui.publico;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.refugio.pawrescue.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Step1PersonalDataFragment extends Fragment {

    private TextInputEditText etNombreCompleto, etFechaNacimiento, etTelefono, etEmail, etDireccion;
    private AutoCompleteTextView actvTipoVivienda;
    private RadioGroup rgPropiedadVivienda;
    private Calendar calendar = Calendar.getInstance();

    // Vistas de archivos
    private ImageView ivIneFrente, ivIneReverso;
    private MaterialButton btnSubirPdf;
    private TextView tvNombrePdf;

    // URIs para guardar la selección
    private Uri uriIneFrente, uriIneReverso, uriComprobantePdf;
    private Uri tempCameraUri; // URI temporal para la foto de la cámara
    private boolean isFrenteSelection = true; // Bandera para saber si estamos subiendo frente o reverso

    // Launchers
    private ActivityResultLauncher<Intent> launcherGallery;
    private ActivityResultLauncher<Intent> launcherCamera;
    private ActivityResultLauncher<Intent> launcherPdf;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_step1_personal_data, container, false);

        initViews(view);
        initFirebase();
        setupDatePicker();
        setupTipoViviendaDropdown();
        setupResultLaunchers(); // Configurar los manejadores de resultados
        setupFileListeners();   // Configurar los clics

        // 🟢 Cargar datos del usuario (incluyendo teléfono editado en perfil)
        loadUserData();

        return view;
    }

    private void initViews(View view) {
        etNombreCompleto = view.findViewById(R.id.etNombreCompleto);
        etFechaNacimiento = view.findViewById(R.id.etFechaNacimiento);
        etTelefono = view.findViewById(R.id.etTelefono);
        etEmail = view.findViewById(R.id.etEmail);
        etDireccion = view.findViewById(R.id.etDireccion);
        actvTipoVivienda = view.findViewById(R.id.actvTipoVivienda);
        rgPropiedadVivienda = view.findViewById(R.id.rgPropiedadVivienda);

        ivIneFrente = view.findViewById(R.id.ivIneFrente);
        ivIneReverso = view.findViewById(R.id.ivIneReverso);
        btnSubirPdf = view.findViewById(R.id.btnSubirPdf);
        tvNombrePdf = view.findViewById(R.id.tvNombrePdf);
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Configuración de los Launchers (Galería, Cámara, PDF, Permisos)
     */
    private void setupResultLaunchers() {
        // 1. Resultado de Galería
        launcherGallery = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedUri = result.getData().getData();
                        handleImageSelection(selectedUri);
                    }
                });

        // 2. Resultado de Cámara
        launcherCamera = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // La URI ya está en tempCameraUri
                        handleImageSelection(tempCameraUri);
                    }
                });

        // 3. Resultado de PDF
        launcherPdf = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        uriComprobantePdf = result.getData().getData();
                        tvNombrePdf.setText("Archivo PDF cargado correctamente");
                        // Asegúrate de tener el color negro o usa android.R.color.black
                        tvNombrePdf.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
                    }
                });

        // 4. Permisos
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(getContext(), "Se requiere permiso de cámara", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Procesa la imagen seleccionada (Cámara o Galería) y la muestra con Glide
     */
    private void handleImageSelection(Uri uri) {
        if (uri == null) return;

        if (isFrenteSelection) {
            uriIneFrente = uri;
            Glide.with(this)
                    .load(uriIneFrente)
                    .centerCrop()
                    .into(ivIneFrente);
        } else {
            uriIneReverso = uri;
            Glide.with(this)
                    .load(uriIneReverso)
                    .centerCrop()
                    .into(ivIneReverso);
        }
    }

    private void setupFileListeners() {
        ivIneFrente.setOnClickListener(v -> showImageSourceDialog(true));
        ivIneReverso.setOnClickListener(v -> showImageSourceDialog(false));

        btnSubirPdf.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            launcherPdf.launch(intent);
        });
    }

    /**
     * Muestra diálogo para elegir entre Cámara o Galería
     */
    private void showImageSourceDialog(boolean isFrente) {
        this.isFrenteSelection = isFrente;
        String[] options = {"Tomar Foto", "Elegir de Galería"};

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Seleccionar Imagen");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                checkCameraPermissionAndOpen();
            } else {
                openGallery();
            }
        });
        builder.show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        launcherGallery.launch(intent);
    }

    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        // Crear una URI temporal en el MediaStore
        tempCameraUri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempCameraUri);
        launcherCamera.launch(cameraIntent);
    }

    /**
     * 🟢 CARGA DE DATOS: Aquí es donde recuperamos el teléfono guardado en Perfil
     */
    private void loadUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            etEmail.setText(user.getEmail());
            String userId = user.getUid();

            db.collection("usuarios").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Recuperar Nombre
                            String nombre = documentSnapshot.getString("nombre");
                            if (nombre != null) etNombreCompleto.setText(nombre);

                            // Recuperar Teléfono (Este es el que editaste en ProfileActivity)
                            String telefono = documentSnapshot.getString("telefono");
                            if (telefono != null) etTelefono.setText(telefono);

                            // Recuperar Dirección
                            String direccion = documentSnapshot.getString("direccion");
                            if (direccion != null) etDireccion.setText(direccion);
                        }
                    });
        }
    }

    private void setupDatePicker() {
        etFechaNacimiento.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateField();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
    }

    private void updateDateField() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etFechaNacimiento.setText(sdf.format(calendar.getTime()));
    }

    private void setupTipoViviendaDropdown() {
        String[] tiposVivienda = {"Casa", "Departamento", "Otro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                tiposVivienda
        );
        actvTipoVivienda.setAdapter(adapter);
    }

    // 🟢 VALIDACIONES (Edad, campos vacíos y documentos)
    public boolean isValidStep() {
        String nombre = etNombreCompleto.getText().toString().trim();
        String fecha = etFechaNacimiento.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();
        String tipoVivienda = actvTipoVivienda.getText().toString().trim();

        if (nombre.isEmpty() || fecha.isEmpty() || telefono.isEmpty() ||
                email.isEmpty() || direccion.isEmpty() || tipoVivienda.isEmpty()) {
            Toast.makeText(getContext(), "Por favor, llena todos los campos.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (uriIneFrente == null || uriIneReverso == null) {
            Toast.makeText(getContext(), "Debes subir ambas fotos del INE.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (uriComprobantePdf == null) {
            Toast.makeText(getContext(), "Debes subir el Comprobante de Domicilio.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!isMayorDeEdad(21)) {
            Toast.makeText(getContext(), "Debes tener al menos 21 años.", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private boolean isMayorDeEdad(int edadMinima) {
        Calendar hoy = Calendar.getInstance();
        int edad = hoy.get(Calendar.YEAR) - calendar.get(Calendar.YEAR);
        if (hoy.get(Calendar.DAY_OF_YEAR) < calendar.get(Calendar.DAY_OF_YEAR)) {
            edad--;
        }
        return edad >= edadMinima;
    }

    public Map<String, Uri> getFileUris() {
        Map<String, Uri> files = new HashMap<>();
        files.put("ineFrente", uriIneFrente);
        files.put("ineReverso", uriIneReverso);
        files.put("comprobante", uriComprobantePdf);
        return files;
    }

    public Map<String, Object> getData() {
        if (!isValidStep()) return null;

        Map<String, Object> data = new HashMap<>();
        data.put("nombreCompleto", etNombreCompleto.getText().toString().trim());
        data.put("fechaNacimiento", etFechaNacimiento.getText().toString().trim());
        data.put("telefono", etTelefono.getText().toString().trim());
        data.put("email", etEmail.getText().toString().trim());
        data.put("direccion", etDireccion.getText().toString().trim());
        data.put("tipoVivienda", actvTipoVivienda.getText().toString().trim());

        int propiedadId = rgPropiedadVivienda.getCheckedRadioButtonId();
        if (propiedadId == R.id.rbPropietario) {
            data.put("propiedadVivienda", "Propietario");
        } else {
            data.put("propiedadVivienda", "Renta");
        }
        return data;
    }
}