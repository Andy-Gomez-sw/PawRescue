package com.refugio.pawrescue.ui.admin;

import android.content.Intent;
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

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.refugio.pawrescue.R;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AdminDashboardFragment extends Fragment {

    private static final String TAG = "AdminDashboardFragment";

    // ... (Inicialización de Firebase Firestore)
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Vistas de Alerta y Estadísticas (asumiendo que existen en el layout)
    private TextView tvTotalAnimals, tvNewRescues, tvAdoptedCount;
    private View cardCitasToday;
    private TextView tvCitasHoyCount;

    private MaterialButton btnVetDetails;
    private MaterialButton btnMedicationDetails;
    private MaterialButton btnCitasDetails;

    // ... (Resto de campos y onCreate)

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);

        // Inicialización de Vistas (Asumiendo IDs del layout)
        // ... (otras inicializaciones)
        cardCitasToday = view.findViewById(R.id.card_citas_today);
        tvCitasHoyCount = view.findViewById(R.id.tv_citas_hoy_count);

        btnVetDetails = view.findViewById(R.id.btn_view_vet_alerts);
        btnMedicationDetails = view.findViewById(R.id.btn_view_meds_alerts);
        btnCitasDetails = view.findViewById(R.id.btn_view_citas);

        // Listener para Alerta de Atención Veterinaria
        btnVetDetails.setOnClickListener(v -> {
            if (getActivity() instanceof AdminMainActivity) {
                // Navegar a la lista de animales con filtro de ATENCIÓN VETERINARIA
                ((AdminMainActivity) getActivity()).navigateToAnimalListWithFilter(AnimalesListFragment.FILTER_ATTENTION);
            }
        });

        // Listener para Alerta de Medicamentos (Reutiliza el mismo filtro)
        btnMedicationDetails.setOnClickListener(v -> {
            if (getActivity() instanceof AdminMainActivity) {
                // Navegar a la lista de animales con filtro de ATENCIÓN VETERINARIA
                ((AdminMainActivity) getActivity()).navigateToAnimalListWithFilter(AnimalesListFragment.FILTER_ATTENTION);
            }
        });

        // Listener para Citas de Hoy (Navegación directa a la actividad de Citas)
        btnCitasDetails.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CitasAdopcionActivity.class);
            startActivity(intent);
        });

        cargarCitasHoy();
        // ... (Otras llamadas a métodos de carga de estadísticas)

        return view;
    }

    private void cargarCitasHoy() {
        // Obtener inicio y fin del día actual (Timestamp)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date inicioDia = cal.getTime();

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date finDia = cal.getTime();

        // Consulta para citas del día (requiere índice compuesto en Firebase)
        db.collection("solicitudes_adopcion")
                .whereEqualTo("estadoSolicitud", "Cita Agendada")
                .whereGreaterThanOrEqualTo("fechaCita", new Timestamp(inicioDia))
                .whereLessThanOrEqualTo("fechaCita", new Timestamp(finDia))
                .orderBy("fechaCita", Query.Direction.ASCENDING) // Necesario para el índice compuesto
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();

                    // Se asume que el layout tiene estas vistas
                    if (cardCitasToday != null && tvCitasHoyCount != null) {
                        if (count > 0) {
                            cardCitasToday.setVisibility(View.VISIBLE);
                            tvCitasHoyCount.setText("🗓️ " + count + " cita(s) de adopción programada(s) para hoy");
                        } else {
                            cardCitasToday.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar citas de hoy (Verifique el índice compuesto en Firestore): ", e);
                    // Ocultar la tarjeta si falla
                    if (cardCitasToday != null) {
                        cardCitasToday.setVisibility(View.GONE);
                    }
                });
    }
}