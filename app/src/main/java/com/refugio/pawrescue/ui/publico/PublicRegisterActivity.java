package com.refugio.pawrescue.ui.publico;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.refugio.pawrescue.databinding.ActivityPublicRegisterBinding;
import com.refugio.pawrescue.ui.auth.LoginActivity;

public class PublicRegisterActivity extends AppCompatActivity {

    private ActivityPublicRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPublicRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

    private boolean validateInputs(
            String nombre,
            String email,
            String password,
            String passwordConfirm,
            String telefono
    ) {
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

        // lógica de registro llamar a Firebase

        /*
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Guardar datos adicionales en Firestore o Realtime Database
                    saveUserData(nombre, email, telefono);
                } else {
                    showLoading(false);
                    Toast.makeText(this,
                        "Error: " + task.getException().getMessage(),
                        Toast.LENGTH_LONG).show();
                }
            });
        */

        new android.os.Handler().postDelayed(() -> {
            showLoading(false);
            Toast.makeText(
                    this,
                    "✅ Registro exitoso. ¡Bienvenido!",
                    Toast.LENGTH_LONG
            ).show();
            navigateToPublicMain();
        }, 2000);
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnRegistrar.setEnabled(!show);
    }

    private void navigateToPublicMain() {
        Intent intent = new Intent(this, PublicMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}