package com.refugio.pawrescue.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.refugio.pawrescue.R;

/**
 * Fragmento principal del Administrador. Muestra estadísticas y alertas (RF-Estadísticas).
 */
public class AdminDashboardFragment extends Fragment {

    private static final String TAG = "AdminDashboardFragment";
    private FirebaseFirestore db;

    // Componentes UI para estadísticas
    private TextView tvTotalAnimals, tvAvailableToday, tvAdoptedTotal;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();

        // Enlazar Componentes UI
        tvTotalAnimals = view.findViewById(R.id.tv_total_animals);
        tvAvailableToday = view.findViewById(R.id.tv_available_today);
        tvAdoptedTotal = view.findViewById(R.id.tv_adopted_total);

        // Listeners para las alertas (solo placeholders por ahora)
        view.findViewById(R.id.btn_view_vet_alerts).setOnClickListener(v ->
                Toast.makeText(getContext(), "Navegar a Alertas Veterinarias (RF-09)", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.btn_view_citas).setOnClickListener(v ->
                Toast.makeText(getContext(), "Navegar a Citas de Adopción (RF-15)", Toast.LENGTH_SHORT).show());


        cargarEstadisticas();

        return view;
    }

    /**
     * Consulta Firestore para obtener los conteos necesarios para el dashboard.
     */
    private void cargarEstadisticas() {
        CollectionReference animalesRef = db.collection("animales");

        // 1. Conteo Total de Animales (RF-Estadísticas)
        animalesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int total = task.getResult().size();
                tvTotalAnimals.setText(String.valueOf(total));
            } else {
                Log.e(TAG, "Error al obtener conteo total:", task.getException());
                tvTotalAnimals.setText("X");
            }
        });

        // 2. Conteo de Animales "Disponible Adopcion" (RF-08)
        Query disponiblesQuery = animalesRef.whereEqualTo("estadoRefugio", "Disponible Adopcion");
        disponiblesQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int disponibles = task.getResult().size();
                tvAvailableToday.setText(String.valueOf(disponibles));
            } else {
                Log.e(TAG, "Error al obtener conteo disponibles:", task.getException());
                tvAvailableToday.setText("X");
            }
        });

        // 3. Conteo de Animales "Adoptado" (RF-08)
        Query adoptadosQuery = animalesRef.whereEqualTo("estadoRefugio", "Adoptado");
        adoptadosQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int adoptados = task.getResult().size();
                tvAdoptedTotal.setText(String.valueOf(adoptados));
            } else {
                Log.e(TAG, "Error al obtener conteo adoptados:", task.getException());
                tvAdoptedTotal.setText("X");
            }
        });

        // NOTA: Las métricas de alertas (Veterinaria, Citas) se implementarían buscando en las
        // subcolecciones de Historial Médico y Citas de Adopción, respectivamente.
    }
}