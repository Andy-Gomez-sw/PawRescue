package com.refugio.pawrescue.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.ui.auth.LoginActivity;

/**
 * Fragmento que muestra la información del usuario Administrador/Coordinador
 * y permite cambiar contraseña y cerrar sesión.
 */
public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView tvUserName, tvUserEmail, tvUserRole;
    private Button btnChangePassword, btnLogout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserEmail = view.findViewById(R.id.tv_user_email);
        tvUserRole = view.findViewById(R.id.tv_user_role);
        btnChangePassword = view.findViewById(R.id.btn_change_password);
        btnLogout = view.findViewById(R.id.btn_logout);

        loadUserProfile();

        btnChangePassword.setOnClickListener(v -> changePassword());
        btnLogout.setOnClickListener(v -> logout());

        return view;
    }

    /**
     * Carga el perfil desde Firebase Auth y Firestore.
     */
    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Sesión no iniciada.", Toast.LENGTH_SHORT).show();
            logout();
            return;
        }

        // Mostrar datos de autenticación
        tvUserEmail.setText(user.getEmail());

        // Cargar datos de Firestore (Nombre y Rol)
        db.collection("usuarios").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        String rol = documentSnapshot.getString("rol");
                        long idNumerico = documentSnapshot.contains("idNumerico") ? documentSnapshot.getLong("idNumerico") : 0;

                        tvUserName.setText(nombre != null ? nombre : "N/A");
                        tvUserRole.setText(String.format("%s (ID #%04d)", rol != null ? rol : "N/A", idNumerico));
                    } else {
                        tvUserName.setText("Datos no encontrados");
                        tvUserRole.setText("Rol: N/A");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando datos de Firestore: ", e);
                    Toast.makeText(getContext(), "Error al cargar datos del perfil.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Inicia el proceso de cambio de contraseña enviando un correo (RF-03).
     */
    private void changePassword() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Toast.makeText(getContext(), "Error: Usuario no autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Deshabilitar botón para evitar múltiples envíos
        btnChangePassword.setEnabled(false);
        btnChangePassword.setText("Enviando...");

        mAuth.sendPasswordResetEmail(user.getEmail())
                .addOnCompleteListener(task -> {
                    btnChangePassword.setEnabled(true);
                    btnChangePassword.setText("Cambiar Contraseña");

                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(),
                                "✅ Enlace de restablecimiento enviado a: " + user.getEmail(),
                                Toast.LENGTH_LONG).show();
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Error desconocido";
                        Toast.makeText(getContext(), "❌ Error al enviar correo: " + error, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error al enviar reset email: ", task.getException());
                    }
                });
    }

    /**
     * Cierra la sesión y redirige a la pantalla de Login (RF-04).
     */
    private void logout() {
        mAuth.signOut();
        Toast.makeText(getContext(), "Sesión cerrada.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}