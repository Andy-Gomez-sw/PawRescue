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
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.Animal;
import com.refugio.pawrescue.model.Transaccion;
import com.refugio.pawrescue.ui.admin.BalanceChartView;

import java.util.Calendar;
import java.util.Date;

/**
 * Fragmento principal del Administrador con estadísticas y alertas funcionales.
 */
public class AdminDashboardFragment extends Fragment {

    private static final String TAG = "AdminDashboardFragment";
    private FirebaseFirestore db;

    // Componentes UI
    private TextView tvTotalAnimals, tvAvailableToday, tvAdoptedTotal;
    private TextView tvAlertaVetCount, tvCitasHoyCount;
    private CardView cardAlertVet, cardCitasToday;
    private Button btnViewVetAlerts, btnViewCitas;
    private BalanceChartView balanceChart; // <--- AGREGADO

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);

        db = FirebaseFirestore.getInstance();

        // Estadísticas
        tvTotalAnimals = view.findViewById(R.id.tv_total_animals);
        tvAvailableToday = view.findViewById(R.id.tv_available_today);
        tvAdoptedTotal = view.findViewById(R.id.tv_adopted_total);

        // Alertas
        cardAlertVet = view.findViewById(R.id.card_alert_vet);
        cardCitasToday = view.findViewById(R.id.card_citas_today);
        tvAlertaVetCount = view.findViewById(R.id.tv_alerta_vet_count);
        tvCitasHoyCount = view.findViewById(R.id.tv_citas_hoy_count);
        btnViewVetAlerts = view.findViewById(R.id.btn_view_vet_alerts);
        btnViewCitas = view.findViewById(R.id.btn_view_citas);

        // Gráfica de Balance
        balanceChart = view.findViewById(R.id.balance_chart); // <--- AGREGADO

        // Listeners
        btnViewVetAlerts.setOnClickListener(v -> navegarAHistorialMedico());
        btnViewCitas.setOnClickListener(v -> navegarACitas());

        cargarEstadisticas();
        cargarAlertas();
        cargarGraficoBalance(); // <--- AGREGADO

        return view;
    }

    private void cargarEstadisticas() {
        CollectionReference animalesRef = db.collection("animales");

        // Total de Animales
        animalesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int total = task.getResult().size();
                tvTotalAnimals.setText(String.valueOf(total));
            } else {
                tvTotalAnimals.setText("X");
            }
        });

        // Disponibles para Adopción
        Query disponiblesQuery = animalesRef.whereEqualTo("estadoRefugio", "Disponible Adopcion");
        disponiblesQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int disponibles = task.getResult().size();
                tvAvailableToday.setText(String.valueOf(disponibles));
            } else {
                tvAvailableToday.setText("X");
            }
        });

        // Adoptados
        Query adoptadosQuery = animalesRef.whereEqualTo("estadoRefugio", "Adoptado");
        adoptadosQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int adoptados = task.getResult().size();
                tvAdoptedTotal.setText(String.valueOf(adoptados));
            } else {
                tvAdoptedTotal.setText("X");
            }
        });
    }

    private void cargarAlertas() {
        // 1. Animales que requieren atención veterinaria
        cargarAlertasVeterinarias();

        // 2. Citas de adopción hoy
        cargarCitasHoy();
    }

    private void cargarAlertasVeterinarias() {
        // Buscar animales con condiciones especiales O en tratamiento
        db.collection("animales")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = 0;

                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Animal animal = doc.toObject(Animal.class);
                        if (animal != null) {
                            // Verificar si tiene condiciones especiales
                            boolean requiereAtencion = false;

                            if (animal.getCondicionesEspeciales() != null &&
                                    !animal.getCondicionesEspeciales().isEmpty()) {
                                requiereAtencion = true;
                            }

                            if ("En Tratamiento".equals(animal.getEstadoRefugio())) {
                                requiereAtencion = true;
                            }

                            if (requiereAtencion) {
                                count++;
                            }
                        }
                    }

                    final int finalCount = count;
                    if (finalCount > 0) {
                        cardAlertVet.setVisibility(View.VISIBLE);
                        tvAlertaVetCount.setText("⚠️ " + finalCount + " animal(es) requieren atención");
                    } else {
                        cardAlertVet.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar alertas veterinarias: ", e);
                    cardAlertVet.setVisibility(View.GONE);
                });
    }

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

        // Consultar citas del día
        db.collection("solicitudes_adopcion")
                .whereEqualTo("estadoSolicitud", "Cita Agendada")
                .whereGreaterThanOrEqualTo("fechaCita", new Timestamp(inicioDia))
                .whereLessThanOrEqualTo("fechaCita", new Timestamp(finDia))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();

                    if (count > 0) {
                        cardCitasToday.setVisibility(View.VISIBLE);
                        tvCitasHoyCount.setText("🗓️ " + count + " cita(s) de adopción programada(s) para hoy");
                    } else {
                        cardCitasToday.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar citas de hoy: ", e);
                    cardCitasToday.setVisibility(View.GONE);
                });
    }

    // <--- MÉTODO AGREGADO --->
    private void cargarGraficoBalance() {
        if (balanceChart == null) {
            Log.e(TAG, "balanceChart es null");
            return;
        }

        db.collection("transacciones").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalDonaciones = 0;
                    double totalGastos = 0;

                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Transaccion t = doc.toObject(Transaccion.class);
                        if (t != null) {
                            if ("Donacion".equalsIgnoreCase(t.getTipo())) {
                                totalDonaciones += t.getMonto();
                            } else {
                                totalGastos += t.getMonto();
                            }
                        }
                    }

                    Log.d(TAG, "Donaciones: " + totalDonaciones + ", Gastos: " + totalGastos);
                    balanceChart.setData(totalDonaciones, totalGastos);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar datos del gráfico: ", e);
                });
    }

    private void navegarAHistorialMedico() {
        // Navegar a una lista de animales en tratamiento
        Toast.makeText(getContext(), "Navegando a Alertas Veterinarias...", Toast.LENGTH_SHORT).show();
        // Implementar navegación o mostrar lista filtrada
    }

    private void navegarACitas() {
        Intent intent = new Intent(getActivity(), CitasAdopcionActivity.class);
        startActivity(intent);
    }
}