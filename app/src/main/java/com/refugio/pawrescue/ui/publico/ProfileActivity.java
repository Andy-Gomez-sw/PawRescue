package com.refugio.pawrescue.ui.publico;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog; // Importante para el diálogo
import androidx.appcompat.app.AppCompatActivity;

// Importaciones de Material Design
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText; // Para los campos de texto

// Importaciones de Firebase
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import com.refugio.pawrescue.R;
import com.refugio.pawrescue.ui.auth.LoginActivity;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserEmail, tvUserPhone;
    private MaterialButton btnLogout;
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

    private void setupButtons() {
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // 🟢 Lógica para editar perfil
        if (cardEditProfile != null) {
            cardEditProfile.setOnClickListener(v -> showEditProfileDialog());
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
                            String nombre = documentSnapshot.getString("nombre");
                            String telefono = documentSnapshot.getString("telefono");

                            if (nombre != null && !nombre.isEmpty()) {
                                tvUserName.setText(nombre);
                            }
                            if (telefono != null && !telefono.isEmpty()) {
                                tvUserPhone.setText(telefono);
                                tvUserPhone.setVisibility(View.VISIBLE);
                            } else {
                                tvUserPhone.setVisibility(View.GONE);
                            }
                        }
                    });
        }
    }

    /**
     * 🟢 Muestra el diálogo para editar nombre y teléfono
     */
    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_profile, null);
        builder.setView(dialogView);

        TextInputEditText etName = dialogView.findViewById(R.id.etEditName);
        TextInputEditText etPhone = dialogView.findViewById(R.id.etEditPhone);

        // Pre-llenar con los datos actuales que se ven en pantalla
        etName.setText(tvUserName.getText().toString().equals("Usuario") ? "" : tvUserName.getText().toString());
        etPhone.setText(tvUserPhone.getText().toString().equals("No registrado") ? "" : tvUserPhone.getText().toString());

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String newName = etName.getText().toString().trim();
            String newPhone = etPhone.getText().toString().trim();

            if (!newName.isEmpty()) {
                updateProfileInFirebase(newName, newPhone);
            } else {
                Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    /**
     * 🟢 Guarda los datos en Firebase
     */
    private void updateProfileInFirebase(String name, String phone) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            Map<String, Object> updates = new HashMap<>();
            updates.put("nombre", name);
            updates.put("telefono", phone);

            db.collection("usuarios").document(userId)
                    .update(updates) // Usamos update para no borrar otros campos
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
                        // Actualizar la vista inmediatamente
                        tvUserName.setText(name);
                        if (!phone.isEmpty()) {
                            tvUserPhone.setText(phone);
                            tvUserPhone.setVisibility(View.VISIBLE);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Si el documento no existe (primera vez), usamos set con merge
                        db.collection("usuarios").document(userId)
                                .set(updates, com.google.firebase.firestore.SetOptions.merge())
                                .addOnSuccessListener(aVoid2 -> {
                                    tvUserName.setText(name);
                                    tvUserPhone.setText(phone);
                                    tvUserPhone.setVisibility(View.VISIBLE);
                                });
                    });
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_profile);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, PublicMainActivity.class)); // Verifica que sea PublicMainActivity
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