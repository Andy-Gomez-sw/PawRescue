package com.refugio.pawrescue.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.ListenerRegistration;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.data.repository.AnimalRepository;
import com.refugio.pawrescue.model.Animal;
import com.refugio.pawrescue.ui.adapter.AnimalAdapter;

import java.util.List;

/**
 * Fragmento que muestra la lista de animales rescatados (RF-06).
 * Incluye un FAB para iniciar el registro (RF-05).
 */
public class AnimalesListFragment extends Fragment implements AnimalAdapter.OnAnimalClickListener {

    private static final String TAG = "AnimalesListFragment";
    private RecyclerView recyclerView;
    private AnimalAdapter adapter;
    private AnimalRepository repository;
    private ProgressBar progressBar;
    private ListenerRegistration listenerRegistration;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_animales_list, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_animales);
        progressBar = view.findViewById(R.id.progress_bar_list);
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add_animal);

        repository = new AnimalRepository();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AnimalAdapter(getContext(), this);
        recyclerView.setAdapter(adapter);

        // Listener para el FAB de agregar animal (RF-05)
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RegistroAnimalActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cargarAnimales();
    }

    /**
     * Inicia la escucha en tiempo real de la colección de animales (RF-06).
     */
    private void cargarAnimales() {
        progressBar.setVisibility(View.VISIBLE);
        listenerRegistration = repository.getAnimalesEnTiempoReal(new AnimalRepository.AnimalesListener() {
            @Override
            public void onAnimalesLoaded(List<Animal> animales) {
                progressBar.setVisibility(View.GONE);
                if (animales.isEmpty()) {
                    Toast.makeText(getContext(), "No hay animales registrados.", Toast.LENGTH_SHORT).show();
                    // Aquí se podría mostrar una vista de estado vacío (RF-06, Excepción 1.1)
                }
                adapter.setAnimalesList(animales);
            }

            @Override
            public void onError(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error al cargar animales: " + message);
            }
        });
    }

    /**
     * Maneja el click en un animal para ir a la vista de detalle/edición (RF-06/RF-07).
     */
    @Override
    public void onAnimalClick(Animal animal) {
        Intent intent = new Intent(getActivity(), DetalleAnimalActivity.class);
        intent.putExtra("animalId", animal.getIdAnimal());
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Detener la escucha en tiempo real para evitar fugas de memoria
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}