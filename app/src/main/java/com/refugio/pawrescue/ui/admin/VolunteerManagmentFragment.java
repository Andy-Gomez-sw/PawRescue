package com.refugio.pawrescue.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.refugio.pawrescue.R;

/**
 * Fragmento para la gestión de voluntarios (RF-18, RF-19).
 * Permite al coordinador registrar nuevos voluntarios y asignar turnos.
 */
public class VolunteerManagmentFragment extends Fragment {

    private RecyclerView recyclerViewVolunteers;
    private FloatingActionButton fabAddVolunteer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_volunteer_managment, container, false);

        recyclerViewVolunteers = view.findViewById(R.id.recycler_volunteers);
        fabAddVolunteer = view.findViewById(R.id.fab_add_volunteer);

        // Configurar RecyclerView (usando un placeholder por ahora)
        recyclerViewVolunteers.setLayoutManager(new LinearLayoutManager(getContext()));
        // TODO: Implementar VolunteerAdapter
        // recyclerViewVolunteers.setAdapter(new VolunteerAdapter(...));

        // Placeholder: Mensaje de lista vacía
        TextView tvEmpty = view.findViewById(R.id.tv_empty_volunteers);
        tvEmpty.setText("No hay voluntarios registrados. Usa el botón '+' para agregar uno.");
        tvEmpty.setVisibility(View.VISIBLE);
        recyclerViewVolunteers.setVisibility(View.GONE);


        // Listener para el FAB (RF-18: Registrar Voluntario)
        fabAddVolunteer.setOnClickListener(v -> mostrarDialogoRegistroVoluntario());

        // Nota: La carga de voluntarios y la asignación de turnos (RF-19) irán aquí.

        return view;
    }

    /**
     * Muestra un diálogo para registrar un nuevo voluntario (RF-18).
     */
    private void mostrarDialogoRegistroVoluntario() {
        // En una implementación completa, se abriría un formulario o una nueva Activity
        Toast.makeText(getContext(), "Abriendo formulario de Registro de Voluntario (RF-18)", Toast.LENGTH_LONG).show();
    }
}