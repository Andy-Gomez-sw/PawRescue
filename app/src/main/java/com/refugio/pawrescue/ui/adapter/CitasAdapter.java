package com.refugio.pawrescue.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.SolicitudAdopcion;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adaptador para mostrar las citas de adopción programadas.
 */
public class CitasAdapter extends RecyclerView.Adapter<CitasAdapter.CitaViewHolder> {

    private final Context context;
    private List<SolicitudAdopcion> citasList;
    private final OnCitaClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd MMM yyyy", new Locale("es", "ES"));
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", new Locale("es", "ES"));

    public interface OnCitaClickListener {
        void onCitaClick(SolicitudAdopcion solicitud);
    }

    public CitasAdapter(Context context, OnCitaClickListener listener) {
        this.context = context;
        this.citasList = new ArrayList<>();
        this.listener = listener;
    }

    public void setCitasList(List<SolicitudAdopcion> citasList) {
        this.citasList = citasList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CitaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cita_adopcion, parent, false);
        return new CitaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CitaViewHolder holder, int position) {
        SolicitudAdopcion cita = citasList.get(position);

        holder.tvNombreAdoptante.setText(cita.getNombreAdoptante());
        holder.tvTelefono.setText("📞 " + cita.getTelefonoAdoptante());

        if (cita.getFechaCita() != null) {
            Date fechaCita = cita.getFechaCita().toDate();
            holder.tvFecha.setText(dateFormat.format(fechaCita));
            holder.tvHora.setText(timeFormat.format(fechaCita));

            // Determinar si la cita es hoy
            Calendar citaCal = Calendar.getInstance();
            citaCal.setTime(fechaCita);

            Calendar hoyCal = Calendar.getInstance();

            boolean esHoy = citaCal.get(Calendar.YEAR) == hoyCal.get(Calendar.YEAR) &&
                    citaCal.get(Calendar.DAY_OF_YEAR) == hoyCal.get(Calendar.DAY_OF_YEAR);

            if (esHoy) {
                holder.tvIndicadorHoy.setVisibility(View.VISIBLE);
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.background_light));
            } else {
                holder.tvIndicadorHoy.setVisibility(View.GONE);
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            }
        }

        holder.itemView.setOnClickListener(v -> listener.onCitaClick(cita));
    }

    @Override
    public int getItemCount() {
        return citasList.size();
    }

    static class CitaViewHolder extends RecyclerView.ViewHolder {
        final TextView tvNombreAdoptante, tvTelefono, tvFecha, tvHora, tvIndicadorHoy;

        public CitaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreAdoptante = itemView.findViewById(R.id.tv_cita_nombre);
            tvTelefono = itemView.findViewById(R.id.tv_cita_telefono);
            tvFecha = itemView.findViewById(R.id.tv_cita_fecha);
            tvHora = itemView.findViewById(R.id.tv_cita_hora);
            tvIndicadorHoy = itemView.findViewById(R.id.tv_indicador_hoy);
        }
    }
}