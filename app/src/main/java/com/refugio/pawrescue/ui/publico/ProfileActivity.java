package com.refugio.pawrescue.ui.publico;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

// Importaciones de Material Design
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton; // Usar MaterialButton es mejor si usas ese componente en XML
import com.google.android.material.card.MaterialCardView;

// Importaciones de Firebase
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import com.refugio.pawrescue.R;
import com.refugio.pawrescue.ui.auth.LoginActivity; // IMPORTANTE: Asegúrate de importar LoginActivity

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserEmail, tvUserPhone;
    private MaterialButton btnLogout; // Cambiado a MaterialButton para coincidir con XML
    private MaterialCardView cardEditProfile;
    private BottomNavigationView bottomNavigation;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupBottomNavigation();
        loadUserData();
        setupButtons();
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserPhone = findViewById(R.id.tvUserPhone);

        // Asegúrate de que el ID R.id.btnLogout exista en tu XML
        btnLogout = findViewById(R.id.btnLogout);
        cardEditProfile = findViewById(R.id.cardEditProfile);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupButtons() {
        btnLogout.setOnClickListener(v -> {
            // 1. Cerrar sesión en Firebase
            mAuth.signOut();

            // 2. Redirigir al Login (Recomendado: LoginActivity)
            // Asegúrate de tener importada la clase LoginActivity
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);

            // 3. Limpiar la pila de actividades para que el usuario no pueda volver atrás
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish(); // Cierra la actividad actual
        });

        // Botón editar perfil
        if (cardEditProfile != null) {
            cardEditProfile.setOnClickListener(v -> {
                // Lógica para editar perfil (opcional)
            });
        }
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            tvUserEmail.setText(user.getEmail());
            String userId = user.getUid();

            db.collection("usuarios").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String nombre = documentSnapshot.getString("nombre"); // Verifica que el campo en Firestore se llame "nombre"
                            String telefono = documentSnapshot.getString("telefono");

                            if (nombre != null) {
                                tvUserName.setText(nombre);
                            }
                            if (telefono != null) {
                                tvUserPhone.setText(telefono);
                                tvUserPhone.setVisibility(View.VISIBLE);
                            }
                        }
                    });
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_profile);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // Navegar a Home (ajusta la clase si es diferente)
                startActivity(new Intent(this, PublicMainActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_favorites) {
                startActivity(new Intent(this, FavoritesActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_requests) {
                startActivity(new Intent(this, MyRequestsActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }
}