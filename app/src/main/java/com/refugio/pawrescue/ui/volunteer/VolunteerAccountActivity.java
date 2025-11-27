package com.refugio.pawrescue.ui.volunteer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.ui.auth.LoginActivity;

public class VolunteerAccountActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserEmail, tvUserRole;
    private MaterialButton btnChangePassword, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_account);

        // ---------------------------
        // Barra inferior (Bottom Nav)
        // ---------------------------
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_volunteer);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.menu_animales) {
                Intent intent = new Intent(this, VolunteerMainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;

            } else if (id == R.id.menu_seguimiento) {
                Intent intent = new Intent(this, VolunteerSeguimientoActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;

            } else if (id == R.id.menu_mi_cuenta) {
                // Ya estamos en Mi cuenta
                return true;
            }

            return false;
        });


        // Dejar "Mi cuenta" seleccionada
        bottomNav.setSelectedItemId(R.id.menu_mi_cuenta);

        // ---------------------------
        // Banner / Toolbar superior
        // ---------------------------
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            // Usamos el TextView del layout, no el título por defecto
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        TextView tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        if (tvToolbarTitle != null) {
            tvToolbarTitle.setText("PawRescue - Voluntario");
        }

        // ---------------------------
        // Referencias UI
        // ---------------------------
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserEmail = findViewById(R.id.tv_user_email);
        tvUserRole = findViewById(R.id.tv_user_role);

        btnChangePassword = findViewById(R.id.btn_change_password);
        btnLogout = findViewById(R.id.btn_logout);

        cargarDatosUsuario();
        configurarBotones();
    }

    private void cargarDatosUsuario() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            tvUserName.setText("Voluntario");
            tvUserEmail.setText("Desconocido");
            tvUserRole.setText("Voluntariado");
            return;
        }

        String uid = user.getUid();
        String email = user.getEmail();
        tvUserEmail.setText(email != null ? email : "Desconocido");

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Lee el documento del usuario para obtener "nombre" y "rol"
        db.collection("usuarios")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {

                        String nombre = doc.getString("nombre");
                        if (nombre != null && !nombre.isEmpty()) {
                            tvUserName.setText(nombre);
                        } else {
                            tvUserName.setText("Voluntario");
                        }

                        String rol = doc.getString("rol");
                        if (rol != null && !rol.isEmpty()) {
                            tvUserRole.setText(rol);
                        } else {
                            tvUserRole.setText("Voluntariado");
                        }

                    } else {
                        tvUserName.setText("Voluntario");
                        tvUserRole.setText("Voluntariado");
                    }
                })
                .addOnFailureListener(e -> {
                    tvUserName.setText("Voluntario");
                    tvUserRole.setText("Voluntariado");
                });
    }

    private void configurarBotones() {

        // Cambiar contraseña
        btnChangePassword.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user == null) {
                Toast.makeText(this, "No has iniciado sesión.", Toast.LENGTH_SHORT).show();
                return;
            }

            String email = user.getEmail();
            if (email == null || email.isEmpty()) {
                Toast.makeText(this, "No se encontró correo asociado.", Toast.LENGTH_LONG).show();
                return;
            }

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnSuccessListener(a ->
                            Toast.makeText(this, "Correo enviado para recuperar contraseña", Toast.LENGTH_LONG).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error al enviar correo: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        });

        // Cerrar sesión
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish();
        });
    }
}
