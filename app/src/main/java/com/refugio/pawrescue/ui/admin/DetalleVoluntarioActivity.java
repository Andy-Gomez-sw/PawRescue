package com.refugio.pawrescue.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.data.helper.FirebaseHelper;
import com.refugio.pawrescue.model.Usuario;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Activity para ver, editar y desactivar un voluntario/personal (RF-18, RF-04).
 */
public class DetalleVoluntarioActivity extends AppCompatActivity {

    private static final String TAG = "DetalleVoluntarioAct";

    private String userId;
    private Usuario currentUsuario;

    // UI Components
    private TextView tvName, tvEmail, tvRole, tvStatus;
    private SwitchMaterial switchStatus;
    private MaterialButton btnEditUser;
    private ProgressBar progressBar;

    // Se inicializan los Helpers aquí
    private final FirebaseHelper firebaseHelper = new FirebaseHelper();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_voluntario);

        userId = getIntent().getStringExtra("userId");

        // Enlazar componentes
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalle del Personal");
        }

        tvName = findViewById(R.id.tv_detail_name);
        tvEmail = findViewById(R.id.tv_detail_email);
        tvRole = findViewById(R.id.tv_detail_role);
        tvStatus = findViewById(R.id.tv_detail_status);
        switchStatus = findViewById(R.id.switch_status);
        btnEditUser = findViewById(R.id.btn_edit_user);
        progressBar = findViewById(R.id.progress_bar_detail);

        // Listeners
        btnEditUser.setOnClickListener(v -> mostrarDialogoEdicion());
        switchStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Solo actuar si fue un clic del usuario (evita bucles al llamar a setChecked)
            if (currentUsuario != null && buttonView.isPressed()) {
                actualizarEstado(isChecked);
            }
        });

        if (userId != null) {
            cargarDetalles(userId);
        } else {
            Toast.makeText(this, "Error: ID de usuario no proporcionado.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnEditUser.setEnabled(!show);
        switchStatus.setEnabled(!show);
    }

    private void cargarDetalles(String uid) {
        showLoading(true);
        db.collection("usuarios").document(uid).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        showLoading(false);
                        if (task.isSuccessful() && task.getResult().exists()) {
                            currentUsuario = task.getResult().toObject(Usuario.class);
                            if (currentUsuario != null) {
                                // Asegurar el UID por si acaso
                                currentUsuario.setUid(task.getResult().getId());
                                actualizarUI(currentUsuario);
                            } else {
                                Toast.makeText(DetalleVoluntarioActivity.this, "Error al mapear datos.", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        } else {
                            Toast.makeText(DetalleVoluntarioActivity.this, "Usuario no encontrado.", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                });
    }

    private void actualizarUI(Usuario usuario) {
        String idDisplay = String.format(Locale.US, "#%04d", usuario.getIdNumerico());

        tvName.setText(usuario.getNombre());
        tvEmail.setText(usuario.getCorreo());
        tvRole.setText(String.format("%s (%s)", usuario.getRol(), idDisplay));

        switchStatus.setChecked(usuario.isEstadoActivo());

        // Actualizar el color y texto del estado
        if (usuario.isEstadoActivo()) {
            tvStatus.setText("Estado: Activo");
            tvStatus.setTextColor(getColor(R.color.status_success));
            switchStatus.setTrackTintList(getColorStateList(R.color.primary_green_light));
        } else {
            tvStatus.setText("Estado: Inactivo");
            tvStatus.setTextColor(getColor(R.color.status_error));
            switchStatus.setTrackTintList(getColorStateList(R.color.text_hint));
        }
    }

    // --- LÓGICA DE EDICIÓN DE ROL/NOMBRE (RF-18) ---
    private void mostrarDialogoEdicion() {
        if (currentUsuario == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Personal y Rol");

        // Reutilizar dialog_registro_voluntario.xml
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_registro_voluntario, null);
        builder.setView(dialogView);

        TextInputEditText etNombre = dialogView.findViewById(R.id.et_nombre_voluntario);
        TextInputEditText etCorreo = dialogView.findViewById(R.id.et_correo_voluntario);
        TextInputEditText etPassword = dialogView.findViewById(R.id.et_password_voluntario);
        Spinner spinnerRol = dialogView.findViewById(R.id.spinner_rol_voluntario);
        TextView tvHint = dialogView.findViewById(R.id.tv_password_hint); // Asumimos un TextView de hint en el layout

        // Configuración para Edición:
        etCorreo.setEnabled(false); // Correo no editable
        etPassword.setVisibility(View.GONE); // Contraseña no se cambia aquí
        if(tvHint != null) tvHint.setVisibility(View.GONE);

        etNombre.setText(currentUsuario.getNombre());
        etCorreo.setText(currentUsuario.getCorreo());

        // Configurar el Spinner de Roles
        ArrayAdapter<CharSequence> adapterSpinner = ArrayAdapter.createFromResource(
                this, R.array.roles_voluntario, android.R.layout.simple_spinner_dropdown_item);
        spinnerRol.setAdapter(adapterSpinner);

        // Seleccionar el rol actual
        for (int i = 0; i < adapterSpinner.getCount(); i++) {
            if (adapterSpinner.getItem(i).toString().equalsIgnoreCase(currentUsuario.getRol())) {
                spinnerRol.setSelection(i);
                break;
            }
        }

        builder.setPositiveButton("GUARDAR CAMBIOS", null);
        builder.setNegativeButton("Cancelar", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nuevoNombre = etNombre.getText().toString().trim();
            String nuevoRol = spinnerRol.getSelectedItem().toString();

            if (nuevoNombre.isEmpty()) {
                Toast.makeText(this, "El nombre no puede estar vacío.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!nuevoNombre.equals(currentUsuario.getNombre()) || !nuevoRol.equals(currentUsuario.getRol())) {
                guardarCambiosEdicion(nuevoNombre, nuevoRol, dialog);
            } else {
                Toast.makeText(this, "No se detectaron cambios.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    private void guardarCambiosEdicion(String nuevoNombre, String nuevoRol, AlertDialog dialog) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", nuevoNombre);
        updates.put("rol", nuevoRol);

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        firebaseHelper.actualizarUsuario(userId, updates, new FirebaseHelper.OperacionUsuarioCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(DetalleVoluntarioActivity.this, message, Toast.LENGTH_SHORT).show();
                // Actualizar la instancia local y la UI
                currentUsuario.setNombre(nuevoNombre);
                currentUsuario.setRol(nuevoRol);
                actualizarUI(currentUsuario);
                dialog.dismiss();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(DetalleVoluntarioActivity.this, error, Toast.LENGTH_LONG).show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            }
        });
    }


    // --- LÓGICA DE ACTIVACIÓN/DESACTIVACIÓN (RF-04) ---
    private void actualizarEstado(boolean esActivo) {
        showLoading(true);

        Map<String, Object> updates = new HashMap<>();
        updates.put("estadoActivo", esActivo);

        firebaseHelper.actualizarUsuario(userId, updates, new FirebaseHelper.OperacionUsuarioCallback() {
            @Override
            public void onSuccess(String message) {
                showLoading(false);
                Toast.makeText(DetalleVoluntarioActivity.this, "Estado actualizado a " + (esActivo ? "Activo" : "Inactivo"), Toast.LENGTH_SHORT).show();
                currentUsuario.setEstadoActivo(esActivo);
                actualizarUI(currentUsuario);
            }

            @Override
            public void onFailure(String error) {
                showLoading(false);
                Toast.makeText(DetalleVoluntarioActivity.this, "Error al cambiar estado: " + error, Toast.LENGTH_LONG).show();
                // Revertir el switch y la instancia local en caso de fallo
                switchStatus.setChecked(!esActivo);
                currentUsuario.setEstadoActivo(!esActivo);
                actualizarUI(currentUsuario);
            }
        });
    }
}