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

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.refugio.pawrescue.R;

import java.util.Calendar;
import java.util.Date;

/**
 * Fragmento del Dashboard principal del Administrador.
 * Muestra estadísticas generales, alertas importantes y resumen financiero.
 */
public class AdminDashboardFragment extends Fragment {

    private static final String TAG = "AdminDashboardFragment";

    // Firebase
    private FirebaseFirestore db;

    // Vistas de Estadísticas
    private TextView tvTotalAnimals, tvAvailableToday, tvAdoptedTotal;

    // Vistas de Alertas
    private View cardAlertVet, cardAlertMeds, cardCitasToday;
    private TextView tvAlertaVetCount, tvAlertaMedsCount, tvCitasHoyCount;
    private Button btnViewVetAlerts, btnViewMedsAlerts, btnViewCitas;

    // Vista de Gráfico Financiero
    private BalanceChartView balanceChart;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);

        // Inicializar vistas de estadísticas
        tvTotalAnimals = view.findViewById(R.id.tv_total_animals);
        tvAvailableToday = view.findViewById(R.id.tv_available_today);
        tvAdoptedTotal = view.findViewById(R.id.tv_adopted_total);

        // Inicializar vistas de alertas
        cardAlertVet = view.findViewById(R.id.card_alert_vet);
        cardAlertMeds = view.findViewById(R.id.card_alert_meds);
        cardCitasToday = view.findViewById(R.id.card_citas_today);

        tvAlertaVetCount = view.findViewById(R.id.tv_alerta_vet_count);
        tvAlertaMedsCount = view.findViewById(R.id.tv_alerta_meds_count);
        tvCitasHoyCount = view.findViewById(R.id.tv_citas_hoy_count);

        btnViewVetAlerts = view.findViewById(R.id.btn_view_vet_alerts);
        btnViewMedsAlerts = view.findViewById(R.id.btn_view_meds_alerts);
        btnViewCitas = view.findViewById(R.id.btn_view_citas);

        // Inicializar gráfico financiero
        balanceChart = view.findViewById(R.id.balance_chart);

        // Configurar listeners de navegación
        setupNavigationListeners();

        // Cargar datos
        cargarEstadisticas();
        cargarAlertasVeterinarias();
        cargarAlertasMedicamentos();
        cargarCitasHoy();
        cargarBalanceFinanciero();

        return view;
    }

    private void setupNavigationListeners() {
        // Navegación a alertas veterinarias
        btnViewVetAlerts.setOnClickListener(v -> {
            if (getActivity() instanceof AdminMainActivity) {
                ((AdminMainActivity) getActivity()).navigateToAnimalListWithFilter(AnimalesListFragment.FILTER_ATTENTION);
            }
        });

        // Navegación a alertas de medicamentos
        btnViewMedsAlerts.setOnClickListener(v -> {
            if (getActivity() instanceof AdminMainActivity) {
                ((AdminMainActivity) getActivity()).navigateToAnimalListWithFilter(AnimalesListFragment.FILTER_MEDICATION);
            }
        });

        // Navegación a citas
        btnViewCitas.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CitasAdopcionActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Carga las estadísticas generales de animales.
     */
    private void cargarEstadisticas() {
        // Total de animales
        db.collection("animales")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int total = queryDocumentSnapshots.size();
                    tvTotalAnimals.setText(String.valueOf(total));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando total de animales: ", e);
                    tvTotalAnimals.setText("N/A");
                });

        // Animales disponibles para adopción
        db.collection("animales")
                .whereEqualTo("estadoRefugio", "Disponible Adopcion")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int disponibles = queryDocumentSnapshots.size();
                    tvAvailableToday.setText(String.valueOf(disponibles));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando disponibles: ", e);
                    tvAvailableToday.setText("N/A");
                });

        // Animales adoptados
        db.collection("animales")
                .whereEqualTo("estadoRefugio", "Adoptado")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int adoptados = queryDocumentSnapshots.size();
                    tvAdoptedTotal.setText(String.valueOf(adoptados));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando adoptados: ", e);
                    tvAdoptedTotal.setText("N/A");
                });
    }

    /**
     * Carga las alertas de animales que requieren atención veterinaria urgente.
     */
    private void cargarAlertasVeterinarias() {
        db.collection("animales")
                .whereEqualTo("estadoRefugio", "En Tratamiento")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();

                    if (count > 0) {
                        cardAlertVet.setVisibility(View.VISIBLE);
                        tvAlertaVetCount.setText(String.format("⚠️ %d animal(es) requieren atención veterinaria urgente", count));
                    } else {
                        cardAlertVet.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando alertas veterinarias: ", e);
                    cardAlertVet.setVisibility(View.GONE);
                });
    }

    /**
     * Carga las alertas de animales con medicación activa.
     */
    private void cargarAlertasMedicamentos() {
        db.collection("animales")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int countConMedicacion = 0;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        // Verificar si tiene condiciones especiales
                        Object condicionesObj = doc.get("condicionesEspeciales");
                        if (condicionesObj instanceof java.util.List) {
                            java.util.List<?> condiciones = (java.util.List<?>) condicionesObj;
                            if (!condiciones.isEmpty()) {
                                countConMedicacion++;
                            }
                        }
                    }

                    if (countConMedicacion > 0) {
                        cardAlertMeds.setVisibility(View.VISIBLE);
                        tvAlertaMedsCount.setText(String.format("💊 %d animal(es) con medicación activa", countConMedicacion));
                    } else {
                        cardAlertMeds.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando alertas de medicamentos: ", e);
                    cardAlertMeds.setVisibility(View.GONE);
                });
    }

    /**
     * 🔴 CORRECCIÓN CRÍTICA: Carga las citas/solicitudes del día
     * Ahora usa TODOS los estados relevantes y un query más robusto
     */
    private void cargarCitasHoy() {
        // Obtener inicio y fin del día actual
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

        // 🔴 CORRECCIÓN: Primero buscar solicitudes con cita agendada para hoy
        db.collection("solicitudes_adopcion")
                .whereEqualTo("estadoSolicitud", "Cita Agendada")
                .whereGreaterThanOrEqualTo("fechaCita", new Timestamp(inicioDia))
                .whereLessThanOrEqualTo("fechaCita", new Timestamp(finDia))
                .orderBy("fechaCita", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();

                    if (count > 0) {
                        cardCitasToday.setVisibility(View.VISIBLE);
                        tvCitasHoyCount.setText(String.format("🗓️ %d cita(s) de adopción programada(s) para hoy", count));
                    } else {
                        // Si no hay citas hoy, mostrar solicitudes pendientes en general
                        cargarSolicitudesPendientes();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar citas de hoy: ", e);
                    // En caso de error (puede ser por falta de índice), mostrar pendientes
                    cargarSolicitudesPendientes();
                });
    }

    /**
     * 🔴 NUEVO MÉTODO: Carga solicitudes pendientes si no hay citas hoy
     */
    private void cargarSolicitudesPendientes() {
        db.collection("solicitudes_adopcion")
                .whereIn("estadoSolicitud", java.util.Arrays.asList(
                        "Pendiente",
                        "En Revisión",
                        "pendiente",
                        "en_revision"
                ))
                .orderBy("fechaSolicitud", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();

                    if (count > 0) {
                        cardCitasToday.setVisibility(View.VISIBLE);
                        tvCitasHoyCount.setText(String.format("📋 %d solicitud(es) de adopción pendiente(s) de revisión", count));
                    } else {
                        cardCitasToday.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar solicitudes pendientes: ", e);
                    cardCitasToday.setVisibility(View.GONE);
                });
    }

    /**
     * Carga el balance financiero mensual y actualiza el gráfico.
     */
    private void cargarBalanceFinanciero() {
        db.collection("transacciones")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalDonaciones = 0;
                    double totalGastos = 0;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String tipo = doc.getString("tipo");
                        Double monto = doc.getDouble("monto");

                        if (monto != null) {
                            if ("Donacion".equalsIgnoreCase(tipo)) {
                                totalDonaciones += monto;
                            } else if ("Gasto".equalsIgnoreCase(tipo)) {
                                totalGastos += monto;
                            }
                        }
                    }

                    // Actualizar el gráfico con los datos
                    balanceChart.setData(totalDonaciones, totalGastos);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando balance financiero: ", e);
                    // El gráfico mostrará "Sin datos financieros"
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recargar datos cuando se vuelve al fragmento
        cargarEstadisticas();
        cargarAlertasVeterinarias();
        cargarAlertasMedicamentos();
        cargarCitasHoy();
        cargarBalanceFinanciero();
    }
}