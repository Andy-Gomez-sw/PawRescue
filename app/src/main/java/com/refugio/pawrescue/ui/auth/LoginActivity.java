package com.refugio.pawrescue.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.ui.admin.AdminMainActivity;
import com.refugio.pawrescue.ui.volunteer.VolunteerMainActivity; // Asumimos esta actividad para el otro rol

/**
 * Activity para el Inicio de Sesión (RF-02) y validación de roles (RF-04).
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    // Componentes de la UI
    private TextInputEditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private TextView tvForgotPassword;
    private ProgressBar progressBar;

    // Instancias de Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Enlazar componentes de la UI
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        progressBar = findViewById(R.id.progress_bar);

        // Listener para el botón de Login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarSesion();
            }
        });

        // Listener para la recuperación de contraseña (RF-03)
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a la Activity de Recuperación de Contraseña
                // Por ahora, solo mostramos un Toast como placeholder
                Toast.makeText(LoginActivity.this, "Funcionalidad de recuperación (RF-03) implementada más tarde.", Toast.LENGTH_SHORT).show();
            }
        });

        // Listener para el botón de Registro (RF-01)
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // En una aplicación real, esta Activity permitiría crear nuevos usuarios
                // Por ahora, solo mostramos un Toast como placeholder
                Toast.makeText(LoginActivity.this, "Funcionalidad de registro (RF-01) implementada más tarde.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Verificar si el usuario ya está logeado al iniciar la Activity
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            verificarRolYRedirigir(currentUser.getUid());
        }
    }

    /**
     * Intenta iniciar sesión con el correo y contraseña proporcionados.
     */
    private void iniciarSesion() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Debe ingresar correo y contraseña.", Toast.LENGTH_SHORT).show();
            return;
        }

        mostrarCargando(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                verificarRolYRedirigir(user.getUid());
                            } else {
                                mostrarCargando(false);
                            }
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            mostrarCargando(false);
                            Toast.makeText(LoginActivity.this, "Autenticación fallida. Credenciales inválidas (RF-02).",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * Consulta Firestore para obtener el rol del usuario autenticado y redirige (RF-04).
     * @param uid El User ID de Firebase Authentication.
     */
    private void verificarRolYRedirigir(String uid) {
        db.collection("usuarios").document(uid).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        mostrarCargando(false);
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String rol = document.getString("rol");
                                if (rol != null) {
                                    redirigirSegunRol(rol);
                                } else {
                                    Toast.makeText(LoginActivity.this, "Error: Rol no asignado. Acceso denegado (RF-04).", Toast.LENGTH_LONG).show();
                                    mAuth.signOut(); // Bloquear acceso
                                }
                            } else {
                                Toast.makeText(LoginActivity.this, "Error: Datos de usuario no encontrados en DB.", Toast.LENGTH_LONG).show();
                                mAuth.signOut();
                            }
                        } else {
                            Log.e(TAG, "Fallo al obtener el rol: ", task.getException());
                            Toast.makeText(LoginActivity.this, "Error de conexión con la base de datos (RF-04).", Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                        }
                    }
                });
    }

    /**
     * Redirige a la Activity principal según el rol.
     * @param rol El rol del usuario ("Admin", "Voluntario", etc.).
     */
    private void redirigirSegunRol(String rol) {
        Intent intent;
        if ("Admin".equalsIgnoreCase(rol) || "Coordinador".equalsIgnoreCase(rol)) {
            // El Administrador tiene acceso completo.
            intent = new Intent(LoginActivity.this, AdminMainActivity.class);
        } else if ("Voluntario".equalsIgnoreCase(rol)) {
            // Redirige a la vista del Voluntario.
            intent = new Intent(LoginActivity.this, VolunteerMainActivity.class);
        } else {
            // Rol no reconocido o Bloqueado.
            Toast.makeText(LoginActivity.this, "Rol no reconocido. Contacte a soporte.", Toast.LENGTH_LONG).show();
            mAuth.signOut();
            return;
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Limpiar historial
        startActivity(intent);
        finish();
        Toast.makeText(LoginActivity.this, "Bienvenido como " + rol + ".", Toast.LENGTH_SHORT).show();
    }

    /**
     * Muestra u oculta la barra de progreso.
     */
    private void mostrarCargando(boolean cargando) {
        progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!cargando);
        btnRegister.setEnabled(!cargando);
    }
}