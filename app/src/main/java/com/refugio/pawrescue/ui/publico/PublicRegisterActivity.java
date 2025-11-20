package com.refugio.pawrescue.ui.publico;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.refugio.pawrescue.data.helper.FirebaseHelper; // <--- Importante
import com.refugio.pawrescue.databinding.ActivityPublicRegisterBinding;
import com.refugio.pawrescue.model.Usuario;
import com.refugio.pawrescue.ui.auth.LoginActivity;

public class PublicRegisterActivity extends AppCompatActivity {

    private ActivityPublicRegisterBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseHelper firebaseHelper; // <--- Instancia del Helper

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPublicRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar Firebase Auth y nuestro Helper
        mAuth = FirebaseAuth.getInstance();
        firebaseHelper = new FirebaseHelper(); // <--- Inicialización

        setupUI();
    }

    private void setupUI() {
        binding.btnRegistrar.setOnClickListener(v -> {
            String nombre = binding.etNombre.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            String passwordConfirm = binding.etPasswordConfirm.getText().toString().trim();
            String telefono = binding.etTelefono.getText().toString().trim();

            if (validateInputs(nombre, email, password, passwordConfirm, telefono)) {
                register(nombre, email, password, telefono);
            }
        });

        binding.tvYaTienesCuenta.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private boolean validateInputs(String nombre, String email, String password, String passwordConfirm, String telefono) {
        boolean isValid = true;

        if (nombre.isEmpty()) {
            binding.tilNombre.setError("El nombre es requerido");
            isValid = false;
        } else {
            binding.tilNombre.setError(null);
        }

        if (email.isEmpty()) {
            binding.tilEmail.setError("El email es requerido");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Email inválido");
            isValid = false;
        } else {
            binding.tilEmail.setError(null);
        }

        if (password.isEmpty()) {
            binding.tilPassword.setError("La contraseña es requerida");
            isValid = false;
        } else if (password.length() < 6) {
            binding.tilPassword.setError("Mínimo 6 caracteres");
            isValid = false;
        } else {
            binding.tilPassword.setError(null);
        }

        if (!passwordConfirm.equals(password)) {
            binding.tilPasswordConfirm.setError("Las contraseñas no coinciden");
            isValid = false;
        } else {
            binding.tilPasswordConfirm.setError(null);
        }

        if (telefono.isEmpty()) {
            binding.tilTelefono.setError("El teléfono es requerido");
            isValid = false;
        } else {
            binding.tilTelefono.setError(null);
        }

        return isValid;
    }

    private void register(String nombre, String email, String password, String telefono) {
        showLoading(true);

        // 1. Crear usuario en Firebase Authentication (Email/Password)
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Autenticación exitosa
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // 2. Guardar datos en Firestore con ID Autoincremental
                            guardarUsuarioEnFirestore(firebaseUser.getUid(), nombre, email, telefono);
                        }
                    } else {
                        showLoading(false);
                        String error = task.getException() != null ? task.getException().getMessage() : "Error desconocido";
                        Toast.makeText(this, "Error Auth: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void guardarUsuarioEnFirestore(String uid, String nombre, String email, String telefono) {
        // Crear objeto Usuario
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUid(uid);
        nuevoUsuario.setNombre(nombre);
        nuevoUsuario.setCorreo(email);
        nuevoUsuario.setRol("Usuario"); // Rol por defecto
        nuevoUsuario.setEstadoActivo(true);

        // Si decidiste agregar el campo telefono al modelo Usuario.java, descomenta esto:
        // nuevoUsuario.setTelefono(telefono);

        // 3. USAR EL HELPER PARA ASIGNAR ID AUTOINCREMENTAL Y GUARDAR
        firebaseHelper.registrarUsuarioConContador(nuevoUsuario, new FirebaseHelper.RegistroUsuarioCallback() {
            @Override
            public void onSuccess(long idGenerado) {
                showLoading(false);

                // Mostrar mensaje de éxito con el ID asignado
                Toast.makeText(PublicRegisterActivity.this,
                        "¡Bienvenido! Tu ID de usuario es #" + idGenerado,
                        Toast.LENGTH_LONG).show();

                navigateToPublicMain();
            }

            @Override
            public void onFailure(String error) {
                showLoading(false);
                Toast.makeText(PublicRegisterActivity.this,
                        "Usuario creado, pero error al guardar datos: " + error,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void navigateToPublicMain() {
        Intent intent = new Intent(this, PublicMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnRegistrar.setEnabled(!show);
    }
}