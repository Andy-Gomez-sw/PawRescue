package com.refugio.pawrescue.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.Usuario;
import com.refugio.pawrescue.ui.adapter.VolunteerAdapter;

import java.util.ArrayList;
import java.util.List;

public class VolunteerManagmentFragment extends Fragment {

    private static final String TAG = "VolunteerManagement";
    private RecyclerView recyclerViewVolunteers;
    private FloatingActionButton fabAddVolunteer;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private VolunteerAdapter adapter;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_volunteer_managment, container, false);

        db = FirebaseFirestore.getInstance();

        recyclerViewVolunteers = view.findViewById(R.id.recycler_volunteers);
        fabAddVolunteer = view.findViewById(R.id.fab_add_volunteer);
        tvEmpty = view.findViewById(R.id.tv_empty_volunteers);
        progressBar = view.findViewById(R.id.progress_bar_volunteers);

        recyclerViewVolunteers.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new VolunteerAdapter(getContext());
        recyclerViewVolunteers.setAdapter(adapter);

        fabAddVolunteer.setOnClickListener(v -> mostrarDialogoRegistroVoluntario());

        cargarVoluntarios();

        return view;
    }

    private void cargarVoluntarios() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        db.collection("usuarios")
                .whereIn("rol", java.util.Arrays.asList("Voluntario", "Coordinador"))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);

                    List<Usuario> voluntarios = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Usuario usuario = doc.toObject(Usuario.class);
                        voluntarios.add(usuario);
                    }

                    if (voluntarios.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText("No hay voluntarios registrados. Usa el botón '+' para agregar uno.");
                        recyclerViewVolunteers.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        recyclerViewVolunteers.setVisibility(View.VISIBLE);
                        adapter.setVolunteersList(voluntarios);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Error al cargar voluntarios: " + e.getMessage());
                    Log.e(TAG, "Error cargando voluntarios: ", e);
                });
    }

    private void mostrarDialogoRegistroVoluntario() {
        Toast.makeText(getContext(), "Abriendo formulario de Registro de Voluntario (RF-18)", Toast.LENGTH_LONG).show();
    }
}