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
        updateSelectedDateTime();
    }

    private void updateTimeLabel() {
        String format = "HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        selectedTimeStr = sdf.format(calendar.getTime());
        etHoraCita.setText(selectedTimeStr);
        updateSelectedDateTime();
    }

    private void updateSelectedDateTime() {
        if (selectedDateStr != null && selectedTimeStr != null) {
            selectedDateTime = calendar.getTime();
        }
    }

    /**
     * 🟢 NUEVO MÉTODO: Verifica si la fecha y hora seleccionadas están dentro del horario de atención.
     */
    private boolean isAppointmentTimeValid(Calendar selectedCalendar) {
        int dayOfWeek = selectedCalendar.get(Calendar.DAY_OF_WEEK);
        int hourOfDay = selectedCalendar.get(Calendar.HOUR_OF_DAY);

        // 1. Días (Lunes a Sábado)
        // Calendar.SUNDAY = 1, Calendar.SATURDAY = 7
        if (dayOfWeek == Calendar.SUNDAY) {
            Toast.makeText(requireContext(), "Las visitas no se realizan los Domingos.", Toast.LENGTH_LONG).show();
            return false;
        }

        // 2. Horas (8:00 AM a 7:00 PM (19:00), exclusiva)
        if (hourOfDay < 8 || hourOfDay >= 19) {
            Toast.makeText(requireContext(), "El horario de atención es de 8:00 AM a 7:00 PM (19:00).", Toast.LENGTH_LONG).show();
            return false;
        }

        // 3. Minutos (Para asegurar que la hora de cierre es 18:xx)
        if (hourOfDay == 18 && selectedCalendar.get(Calendar.MINUTE) > 59) {
            // Este caso es redundante si se chequea hourOfDay >= 19, pero es buena práctica para asegurar.
            Toast.makeText(requireContext(), "El horario de atención es de 8:00 AM a 7:00 PM (19:00).", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }


    /**
     * Valida que la cita haya sido agendada, que esté dentro del horario y que se acepten los términos.
     */
    public boolean validateFields() {
        if (selectedDateTime == null) {
            Toast.makeText(requireContext(), "Debes agendar una fecha y hora para la cita de visita.", Toast.LENGTH_LONG).show();
            return false;
        }

        // 🟢 VALIDACIÓN DE NEGOCIO: Días y horas
        if (!isAppointmentTimeValid(calendar)) {
            // El error específico ya fue mostrado en isAppointmentTimeValid
            return false;
        }

        if (!cbTerminos.isChecked()) {
            Toast.makeText(requireContext(), "Debes aceptar los términos y condiciones.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public Map<String, Object> getData() {
        Map<String, Object> data = new HashMap<>();

        data.put("cbTerminos", cbTerminos.isChecked());
        data.put("fechaCitaString", selectedDateStr);
        data.put("horaCitaString", selectedTimeStr);
        data.put("fechaCita", selectedDateTime);

        return data;
    }
}