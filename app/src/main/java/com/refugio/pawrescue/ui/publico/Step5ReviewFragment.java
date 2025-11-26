package com.refugio.pawrescue.ui.publico;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.textfield.TextInputEditText;
import com.refugio.pawrescue.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Step5ReviewFragment extends Fragment {

    private TextInputEditText etFechaCita, etHoraCita;
    private CheckBox cbTerminos;
    private Calendar calendar;

    // Campos para almacenar la fecha y hora seleccionadas
    private String selectedDateStr;
    private String selectedTimeStr;
    private Date selectedDateTime;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_step5_review, container, false);

        etFechaCita = view.findViewById(R.id.etFechaCita);
        etHoraCita = view.findViewById(R.id.etHoraCita);
        cbTerminos = view.findViewById(R.id.cbTerminos);
        calendar = Calendar.getInstance();

        setupDateTimePickers();

        return view;
    }

    private void setupDateTimePickers() {
        etFechaCita.setOnClickListener(v -> showDatePicker());
        etHoraCita.setOnClickListener(v -> showTimePicker());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, monthOfYear, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, monthOfYear);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateLabel();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        // Impedir fechas pasadas
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    updateTimeLabel();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false // Formato 12 horas (false) o 24 horas (true)
        );
        timePickerDialog.show();
    }

    private void updateDateLabel() {
        String format = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        selectedDateStr = sdf.format(calendar.getTime());
        etFechaCita.setText(selectedDateStr);
        // También actualizamos la fecha y hora completa
        updateSelectedDateTime();
    }

    private void updateTimeLabel() {
        String format = "HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        selectedTimeStr = sdf.format(calendar.getTime());
        etHoraCita.setText(selectedTimeStr);
        // También actualizamos la fecha y hora completa
        updateSelectedDateTime();
    }

    private void updateSelectedDateTime() {
        // Solo guardamos la fecha completa si ambos campos tienen valor
        if (selectedDateStr != null && selectedTimeStr != null) {
            selectedDateTime = calendar.getTime();
        }
    }

    public Map<String, Object> getData() {
        Map<String, Object> data = new HashMap<>();

        // Agregar los datos de la cita y la confirmación
        data.put("cbTerminos", cbTerminos.isChecked());
        // 🟢 Se guardan los componentes de fecha/hora como strings
        data.put("fechaCitaString", selectedDateStr);
        data.put("horaCitaString", selectedTimeStr);
        // 🟢 Se guarda el Timestamp completo
        data.put("fechaCita", selectedDateTime);

        return data;
    }

    /**
     * Valida que la cita haya sido agendada y que se acepten los términos.
     */
    public boolean validateFields() {
        if (selectedDateTime == null) {
            Toast.makeText(requireContext(), "Debes agendar una fecha y hora para la cita de visita.", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!cbTerminos.isChecked()) {
            Toast.makeText(requireContext(), "Debes aceptar los términos y condiciones.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}