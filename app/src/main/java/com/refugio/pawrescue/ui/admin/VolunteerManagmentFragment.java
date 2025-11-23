package com.refugio.pawrescue.ui.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.data.helper.FirebaseHelper;
import com.refugio.pawrescue.model.Usuario;
import com.refugio.pawrescue.ui.adapter.VolunteerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fragmento para la gestión de voluntarios (RF-18, RF-19).
 * Permite al coordinador registrar nuevos voluntarios y ver sus detalles.
 */
public class VolunteerManagmentFragment extends Fragment implements VolunteerAdapter.OnVolunteerClickListener {

    private static final String TAG = "VolunteerManagmentFrag";

    private RecyclerView recyclerViewVolunteers;
    private TextView tvEmpty;
    private ProgressBar progressBar;
    private VolunteerAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseHelper firebaseHelper;
    private ListenerRegistration listenerRegistration;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        firebaseHelper = new FirebaseHelper();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_volunteer_managment, container, false);

        recyclerViewVolunteers = view.findViewById(R.id.recycler_volunteers);
        FloatingActionButton fabAddVolunteer = view.findViewById(R.id.fab_add_volunteer);
        tvEmpty = view.findViewById(R.id.tv_empty_volunteers);
        progressBar = view.findViewById(R.id.progress_bar_volunteers);

        recyclerViewVolunteers.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new VolunteerAdapter(getContext(), this); // <<-- Se pasa 'this' como listener
        recyclerViewVolunteers.setAdapter(adapter);

        fabAddVolunteer.setOnClickListener(v -> mostrarDialogoRegistroVoluntario());

        cargarVoluntarios();

        return view;
    }

    private void cargarVoluntarios() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        // Se cargan todos los usuarios que tienen un rol administrativo o de voluntariado
        Query query = db.collection("usuarios")
                .whereIn("rol", java.util.Arrays.asList("Voluntario", "Coordinador", "Administrador")) // Asegúrate de incluir 'Administrador' si es necesario
                .orderBy("nombre", Query.Direction.ASCENDING);

        // Uso de SnapshotListener para actualizar en tiempo real si se añade/modifica un usuario
        listenerRegistration = query.addSnapshotListener((snapshots, e) -> {
            progressBar.setVisibility(View.GONE);
            if (e != null) {
                Log.w(TAG, "Error al escuchar voluntarios:", e);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Error al cargar personal: " + e.getMessage());
                return;
            }

            if (snapshots != null) {
                List<Usuario> usuarios = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snapshots) {
                    Usuario usuario = doc.toObject(Usuario.class);
                    if (usuario != null) {
                        usuario.setUid(doc.getId());
                        usuarios.add(usuario);
                    }
                }

                // CORRECCIÓN: Usar el método correcto del adaptador
                adapter.setVolunteersList(usuarios); // <<-- LÍNEA CORREGIDA

                if (usuarios.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("No hay personal registrado. Usa el botón '+' para agregar uno (RF-18).");
                    recyclerViewVolunteers.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    recyclerViewVolunteers.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * Muestra un diálogo para registrar un nuevo voluntario/personal (RF-18).
     */
    private void mostrarDialogoRegistroVoluntario() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Registrar Nuevo Personal (RF-18)");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_registro_voluntario, null);
        builder.setView(dialogView);

        // Componentes del diálogo (asumiendo que existen en dialog_registro_voluntario.xml)
        TextInputEditText etNombre = dialogView.findViewById(R.id.et_nombre_voluntario);
        TextInputEditText etCorreo = dialogView.findViewById(R.id.et_correo_voluntario);
        TextInputEditText etPassword = dialogView.findViewById(R.id.et_password_voluntario);
        Spinner spinnerRol = dialogView.findViewById(R.id.spinner_rol_voluntario);

        // Configurar el Spinner de Roles
        ArrayAdapter<CharSequence> adapterSpinner = ArrayAdapter.createFromResource(
                getContext(), R.array.roles_voluntario, android.R.layout.simple_spinner_dropdown_item);
        spinnerRol.setAdapter(adapterSpinner);

        builder.setPositiveButton("Registrar", null);
        builder.setNegativeButton("Cancelar", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Sobreescribir el listener del botón Positivo para manejar la validación y el registro
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String correo = etCorreo.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String rol = spinnerRol.getSelectedItem().toString();

            if (validarCampos(nombre, correo, password)) {
                registrarVoluntario(nombre, correo, password, rol, dialog);
            }
        });
    }

    private boolean validarCampos(String nombre, String correo, String password) {
        if (nombre.isEmpty() || correo.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Todos los campos son obligatorios.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(getContext(), "Formato de correo inválido.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 6) {
            Toast.makeText(getContext(), "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void registrarVoluntario(String nombre, String correo, String password, String rol, AlertDialog dialog) {
        // Deshabilitar UI durante el proceso
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        Toast.makeText(getContext(), "Iniciando registro de " + rol + "...", Toast.LENGTH_SHORT).show();

        // 1. Crear usuario en Firebase Authentication (Email/Password)
        mAuth.createUserWithEmailAndPassword(correo, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // 2. Guardar datos en Firestore con ID Autoincremental
                            guardarUsuarioEnFirestore(firebaseUser.getUid(), nombre, correo, rol, dialog);
                        }
                    } else {
                        // Error de autenticación (ej: correo ya registrado)
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        String error = task.getException() != null ? task.getException().getMessage() : "Error desconocido";
                        Toast.makeText(getContext(), "❌ Error Auth: " + error, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error en Firebase Auth: ", task.getException());
                    }
                });
    }

    private void guardarUsuarioEnFirestore(String uid, String nombre, String email, String rol, AlertDialog dialog) {
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUid(uid);
        nuevoUsuario.setNombre(nombre);
        nuevoUsuario.setCorreo(email);
        nuevoUsuario.setRol(rol);
        nuevoUsuario.setEstadoActivo(true);

        firebaseHelper.registrarUsuarioConContador(nuevoUsuario, new FirebaseHelper.RegistroUsuarioCallback() {
            @Override
            public void onSuccess(long idGenerado) {
                Toast.makeText(getContext(),
                        "✅ Personal registrado. ID: #" + idGenerado,
                        Toast.LENGTH_LONG).show();
                dialog.dismiss(); // Cerrar el diálogo solo al finalizar el proceso
            }

            @Override
            public void onFailure(String error) {
                // Caso crítico: Auth pasó, pero Firestore falló. El usuario existe pero sin rol/datos.
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                Toast.makeText(getContext(),
                        "⚠️ Error en DB: " + error + ". Contacte a soporte.",
                        Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error al registrar en Firestore: " + error);
            }
        });
    }

    /**
     * Implementación del clic para ver detalles y editar/desactivar.
     */
    @Override
    public void onVolunteerClick(Usuario usuario) {
        Intent intent = new Intent(getActivity(), DetalleVoluntarioActivity.class);
        intent.putExtra("userId", usuario.getUid()); // Pasar el UID
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Detener la escucha de Firestore
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}