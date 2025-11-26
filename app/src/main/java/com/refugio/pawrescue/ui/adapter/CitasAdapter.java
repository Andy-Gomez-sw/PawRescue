package com.refugio.pawrescue.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.SolicitudAdopcion;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CitasAdapter extends RecyclerView.Adapter<CitasAdapter.CitaViewHolder> {

    private Context context;
    private List<SolicitudAdopcion> citasList;
    private OnCitaClickListener listener;

    public interface OnCitaClickListener {
        void onCitaClick(SolicitudAdopcion solicitud);
    }

    public CitasAdapter(Context context, OnCitaClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setCitasList(List<SolicitudAdopcion> citasList) {
        this.citasList = citasList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CitaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Asegúrate de tener este layout: item_cita_adopcion.xml
        // Si se llama diferente, cámbialo aquí.
        View view = LayoutInflater.from(context).inflate(R.layout.item_cita_adopcion, parent, false);
        return new CitaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CitaViewHolder holder, int position) {
        if (citasList == null) return;

        SolicitudAdopcion cita = citasList.get(position);

        // 🔹 1. NOMBRE DEL ANIMAL (con validación robusta)
        String nombreAnimal = cita.getNombreAnimal();
        if (nombreAnimal == null || nombreAnimal.isEmpty()) {
            holder.tvMascota.setText("🐾 Animal sin nombre");
        } else {
            holder.tvMascota.setText("🐾 " + nombreAnimal);
        }

        // 🔹 2. NOMBRE DEL ADOPTANTE
        holder.tvAdoptante.setText("👤 " + cita.getNombreAdoptante());

        // 🔹 3. TELÉFONO
        holder.tvContacto.setText("📞 " + cita.getTelefonoAdoptante());

        // 🔹 4. FECHA
        if (cita.getFechaCita() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.tvFecha.setText("📅 Cita: " + sdf.format(cita.getFechaCita()));
        } else if (cita.getFechaSolicitud() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.tvFecha.setText("📋 Solicitud: " + sdf.format(cita.getFechaSolicitud()));
        } else {
            holder.tvFecha.setText("📋 Fecha pendiente");
        }

        // 🔹 5. ESTADO (con colores)
        String estado = cita.getEstado();
        if (estado != null) {
            holder.tvEstado.setText(estado.toUpperCase());

            switch (estado.toLowerCase()) {
                case "aprobada":
                case "adoptado":
                    holder.tvEstado.setTextColor(context.getResources().getColor(R.color.status_success));
                    break;
                case "pendiente":
                    holder.tvEstado.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                    break;
                case "cita_agendada":
                    holder.tvEstado.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
                    break;
                default:
                    holder.tvEstado.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            }
        } else {
            holder.tvEstado.setText("PENDIENTE");
        }

        // Click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCitaClick(cita);
            }
        });
    }

    @Override
    public int getItemCount() {
        return citasList != null ? citasList.size() : 0;
    }

    public static class CitaViewHolder extends RecyclerView.ViewHolder {
        // Ajusta estos IDs según tu XML (item_cita_adopcion.xml)
        TextView tvMascota, tvAdoptante, tvFecha, tvContacto, tvEstado;

        public CitaViewHolder(@NonNull View itemView) {
            super(itemView);
            // Estos son IDs comunes, verifica tu XML si te marca error
            tvMascota = itemView.findViewById(R.id.tvMascotaName);
            tvAdoptante = itemView.findViewById(R.id.tvAdoptanteName);
            tvFecha = itemView.findViewById(R.id.tvFechaCita);
            tvContacto = itemView.findViewById(R.id.tvContactoInfo); // o tvTelefono
            tvEstado = itemView.findViewById(R.id.tvEstadoSolicitud);
        }
    }
}