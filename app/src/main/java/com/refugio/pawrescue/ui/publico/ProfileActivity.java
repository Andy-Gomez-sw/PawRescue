package com.refugio.pawrescue.ui.publico;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.refugio.pawrescue.R;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserEmail, tvUserPhone;
    private Button btnLogout;
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
        btnLogout = findViewById(R.id.btnLogout);
        cardEditProfile = findViewById(R.id.cardEditProfile);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupBottomNavigation() {
        // Marcar como seleccionado el item de Perfil
        bottomNavigation.setSelectedItemId(R.id.nav_profile);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, GalleryActivity.class));
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
                // Ya estamos aquí
                return true;
            }

            return false;
        });
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Mostrar email desde Auth
            tvUserEmail.setText(user.getEmail());

            // Cargar datos adicionales desde Firestore
            String userId = user.getUid();
            db.collection("usuarios").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String nombre = documentSnapshot.getString("nombre");
                            String telefono = documentSnapshot.getString("telefono");

                            if (nombre != null) {
                                tvUserName.setText(nombre);
                            } else {
                                tvUserName.setText("Usuario");
                            }

                            if (telefono != null) {
                                tvUserPhone.setText(telefono);
                                tvUserPhone.setVisibility(View.VISIBLE);
                            }
                        }
                    });
        }
    }

    private void setupButtons() {
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            // Redirigir al Login o Onboarding
            Intent intent = new Intent(ProfileActivity.this, OnboardingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Botón para editar perfil (opcional)
        if (cardEditProfile != null) {
            cardEditProfile.setOnClickListener(v -> {
                // Aquí puedes abrir una actividad para editar el perfil
                // startActivity(new Intent(this, EditProfileActivity.class));
            });
        }
    }
}