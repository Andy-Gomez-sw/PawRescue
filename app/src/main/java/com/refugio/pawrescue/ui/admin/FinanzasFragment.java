package com.refugio.pawrescue.ui.admin;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.Transaccion;
import com.refugio.pawrescue.ui.adapter.FinanzasAdapter;

import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Fragmento para la Gestión Financiera (RF-21, RF-22, RF-23).
 * Muestra el balance y la lista de transacciones.
 */
public class FinanzasFragment extends Fragment {

    private static final String TAG = "FinanceFragment";
    private static final int PERMISSION_REQUEST_CODE = 200;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FinanzasAdapter adapter;
    private ListenerRegistration transactionListener;

    // Componentes UI
    private TextView tvBalance, tvDonations, tvExpenses;
    private RecyclerView rvTransactions;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddDonation, fabAddExpense;
    private Button btnExportReport;
    private List<Transaccion> currentTransactions = new ArrayList<>(); // Almacena los datos para exportar

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    private final SimpleDateFormat exportDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_finanzas, container, false);

        // Enlazar componentes
        tvBalance = view.findViewById(R.id.tv_balance_amount);
        tvDonations = view.findViewById(R.id.tv_total_donations);
        tvExpenses = view.findViewById(R.id.tv_total_expenses);
        rvTransactions = view.findViewById(R.id.rv_transactions);
        progressBar = view.findViewById(R.id.progress_bar_finance);
        fabAddDonation = view.findViewById(R.id.fab_add_donation);
        fabAddExpense = view.findViewById(R.id.fab_add_expense);
        btnExportReport = view.findViewById(R.id.btn_export_report);

        // Configurar RecyclerView
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FinanzasAdapter(getContext());
        rvTransactions.setAdapter(adapter);

        // Listeners para FABs (RF-21 y RF-22)
        fabAddDonation.setOnClickListener(v -> mostrarDialogoRegistro("Donacion"));
        fabAddExpense.setOnClickListener(v -> mostrarDialogoRegistro("Gasto"));
        btnExportReport.setOnClickListener(v -> solicitarPermisoYExportar());

        // Cargar datos en tiempo real
        cargarTransaccionesEnTiempoReal();

        return view;
    }

    // --- Lógica de Exportación (RF-23) ---

    private void solicitarPermisoYExportar() {
        if (getContext() == null) return;

        // A partir de Android 10 (API 29), MediaStore se recomienda.
        // Para versiones anteriores, necesitamos el permiso WRITE_EXTERNAL_STORAGE.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        } else {
            // Permiso concedido o no requerido (API 29+)
            exportarReporteCSV(currentTransactions);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportarReporteCSV(currentTransactions);
            } else {
                Toast.makeText(getContext(), "Permiso de almacenamiento denegado. No se puede exportar el reporte.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Genera el archivo CSV y lo guarda en la carpeta de Descargas (RF-23).
     */
    private void exportarReporteCSV(List<Transaccion> transacciones) {
        if (transacciones.isEmpty()) {
            Toast.makeText(getContext(), "No hay transacciones para exportar.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generar contenido CSV
        String csvContent = generateCsvContent(transacciones);
        String fileName = "PawRescue_Finanzas_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".csv";

        try {
            ContentResolver resolver = getContext().getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
            if (uri != null) {
                try (OutputStream os = resolver.openOutputStream(uri)) {
                    os.write(csvContent.getBytes());
                    Toast.makeText(getContext(), "✅ Reporte exportado a Descargas: " + fileName, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getContext(), "❌ Error al crear el archivo de descarga.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error durante la exportación a CSV", e);
            Toast.makeText(getContext(), "❌ Error al guardar el archivo: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Convierte la lista de Transacciones a formato CSV.
     */
    private String generateCsvContent(List<Transaccion> transacciones) {
        StringBuilder sb = new StringBuilder();
        // Encabezados
        sb.append("ID;Tipo;Monto;Categoria;Descripcion;Fecha;UID_Registro\n");

        for (Transaccion t : transacciones) {
            // Aseguramos que los campos con comas (descripciones) no rompan el formato CSV usando ';' como separador
            String cleanedDescription = t.getDescripcion() != null ?
                    t.getDescripcion().replace(';', ',').replace('\n', ' ').trim() : "";

            String fechaStr = t.getFecha() != null ? exportDateFormat.format(t.getFecha().toDate()) : "";

            sb.append(String.format(Locale.US, "%s;%s;%.2f;%s;%s;%s;%s\n",
                    t.getIdTransaccion() != null ? t.getIdTransaccion() : "N/A",
                    t.getTipo() != null ? t.getTipo() : "N/A",
                    t.getMonto(),
                    t.getCategoria() != null ? t.getCategoria() : "N/A",
                    cleanedDescription,
                    fechaStr,
                    t.getIdUsuarioRegistro() != null ? t.getIdUsuarioRegistro() : "N/A"
            ));
        }
        return sb.toString();
    }


    /**
     * Muestra el diálogo para registrar una nueva Donación (RF-21) o Gasto (RF-22).
     */
    private void mostrarDialogoRegistro(String tipo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Registrar " + (tipo.equals("Donacion") ? "Donación (RF-21)" : "Gasto (RF-22)"));

        // Inflar layout del diálogo
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_finanzas_register, null);
        builder.setView(dialogView);

        // Componentes del diálogo
        EditText etMonto = dialogView.findViewById(R.id.et_monto);
        Spinner spinnerCategoria = dialogView.findViewById(R.id.spinner_categoria);
        EditText etDescripcion = dialogView.findViewById(R.id.et_descripcion);

        // Configurar el Spinner de Categoría
        int arrayResId = tipo.equals("Donacion") ? R.array.donation_categories : R.array.expense_categories;
        ArrayAdapter<CharSequence> adapterSpinner = ArrayAdapter.createFromResource(
                getContext(), arrayResId, android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapterSpinner);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            // Se implementa la lógica de guardado en el listener del botón del diálogo
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Sobreescribir el listener del botón Positivo para evitar que se cierre el diálogo si la validación falla
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String montoStr = etMonto.getText().toString().trim();
            String categoria = spinnerCategoria.getSelectedItem().toString();
            String descripcion = etDescripcion.getText().toString().trim();

            if (montoStr.isEmpty() || descripcion.isEmpty()) {
                Toast.makeText(getContext(), "Todos los campos son obligatorios.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double monto = Double.parseDouble(montoStr);
                registrarTransaccion(tipo, monto, categoria, descripcion);
                dialog.dismiss(); // Cerrar si el registro es exitoso
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Monto inválido. Use solo números.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Registra la transacción en Firestore (RF-21 / RF-22).
     */
    private void registrarTransaccion(String tipo, double monto, String categoria, String descripcion) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Error: Usuario no autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        Transaccion transaccion = new Transaccion();
        transaccion.setTipo(tipo);
        transaccion.setMonto(monto);
        transaccion.setCategoria(categoria);
        transaccion.setDescripcion(descripcion);
        transaccion.setFecha(new Timestamp(new Date()));
        transaccion.setIdUsuarioRegistro(mAuth.getCurrentUser().getUid());

        db.collection("transacciones")
                .add(transaccion)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), tipo + " registrado exitosamente.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al registrar " + tipo.toLowerCase() + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error al registrar transaccion", e);
                });
    }

    /**
     * Carga y escucha en tiempo real la colección de transacciones.
     */
    private void cargarTransaccionesEnTiempoReal() {
        progressBar.setVisibility(View.VISIBLE);

        Query query = db.collection("transacciones").orderBy("fecha", Query.Direction.DESCENDING);

        transactionListener = query.addSnapshotListener((snapshots, e) -> {
            progressBar.setVisibility(View.GONE);
            if (e != null) {
                Log.w(TAG, "Error al escuchar transacciones:", e);
                Toast.makeText(getContext(), "Error al cargar datos financieros.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (snapshots != null) {
                List<Transaccion> transacciones = new ArrayList<>();
                double totalDonaciones = 0;
                double totalGastos = 0;

                for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                    Transaccion t = doc.toObject(Transaccion.class);
                    if (t != null) {
                        t.setIdTransaccion(doc.getId());
                        transacciones.add(t);

                        if ("Donacion".equalsIgnoreCase(t.getTipo())) {
                            totalDonaciones += t.getMonto();
                        } else {
                            totalGastos += t.getMonto();
                        }
                    }
                }

                // 1. Actualizar la lista local de transacciones para la exportación
                currentTransactions = transacciones;

                // 2. Actualizar el resumen
                double balance = totalDonaciones - totalGastos;
                tvBalance.setText(currencyFormat.format(balance));
                tvDonations.setText(currencyFormat.format(totalDonaciones));
                tvExpenses.setText(currencyFormat.format(totalGastos));

                if (balance >= 0) {
                    tvBalance.setTextColor(ContextCompat.getColor(getContext(), R.color.primary_green));
                } else {
                    tvBalance.setTextColor(ContextCompat.getColor(getContext(), R.color.status_error));
                }

                // 3. Actualizar la lista visible
                adapter.setTransaccionesList(transacciones);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Detener la escucha de Firestore
        if (transactionListener != null) {
            transactionListener.remove();
        }
    }
}