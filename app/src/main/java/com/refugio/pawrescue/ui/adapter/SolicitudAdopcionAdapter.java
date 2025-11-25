package com.refugio.pawrescue.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.SolicitudAdopcion;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SolicitudAdopcionAdapter extends RecyclerView.Adapter<SolicitudAdopcionAdapter.ViewHolder> {

    private Context context;
    private List<SolicitudAdopcion> listaSolicitudes;
    private SolicitudInteractionListener listener;

    // Interfaz para comunicación
    public interface SolicitudInteractionListener {
        void onSolicitudClick(SolicitudAdopcion solicitud);
    }

    public SolicitudAdopcionAdapter(Context context, List<SolicitudAdopcion> listaSolicitudes, SolicitudInteractionListener listener) {
        this.context = context;
        this.listaSolicitudes = listaSolicitudes;
        this.listener = listener;
    }

    public void setListaSolicitudes(List<SolicitudAdopcion> listaSolicitudes) {
        this.listaSolicitudes = listaSolicitudes;
        notifyDataSetChanged();
    }

    // Método de compatibilidad
    public void setSolicitudList(List<SolicitudAdopcion> listaSolicitudes) {
        setListaSolicitudes(listaSolicitudes);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Usamos el layout item_cita_adopcion.xml (asegúrate de que exista)
        View view = LayoutInflater.from(context).inflate(R.layout.item_cita_adopcion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SolicitudAdopcion solicitud = listaSolicitudes.get(position);

        // 🔴 CORRECCIÓN: Usamos getNombreAnimal() que es el que tiene tu modelo
        String animal = solicitud.getNombreAnimal();
        holder.tvMascota.setText(animal != null ? animal : "Mascota");

        // Nombre Adoptante
        // Si te marca error aquí en el futuro, es porque falta actualizar el modelo SolicitudAdopcion.java
        // Por ahora lo dejamos así:
        if (solicitud.getNombreAdoptante() != null) {
            holder.tvAdoptante.setText(solicitud.getNombreAdoptante());
        } else {
            holder.tvAdoptante.setText("Usuario");
        }

        // Estado
        String estado = solicitud.getEstado() != null ? solicitud.getEstado() : "Pendiente";
        holder.tvEstado.setText(estado.toUpperCase());

        if (estado.equalsIgnoreCase("aprobada")) {
            holder.tvEstado.setTextColor(Color.parseColor("#4CAF50"));
        } else if (estado.equalsIgnoreCase("pendiente")) {
            holder.tvEstado.setTextColor(Color.parseColor("#FF9800"));
        }

        // Fecha
        if (solicitud.getFechaSolicitud() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.tvFecha.setText(dateFormat.format(solicitud.getFechaSolicitud()));
        } else {
            holder.tvFecha.setText("--/--/----");
        }

        // Teléfono
        if (solicitud.getTelefonoAdoptante() != null) {
            holder.tvContacto.setText(solicitud.getTelefonoAdoptante());
        } else {
            holder.tvContacto.setText("Sin contacto");
        }

        // Click Listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSolicitudClick(solicitud);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaSolicitudes != null ? listaSolicitudes.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMascota, tvAdoptante, tvFecha, tvEstado, tvContacto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // IDs deben coincidir con item_cita_adopcion.xml
            tvMascota = itemView.findViewById(R.id.tvMascotaName);
            tvAdoptante = itemView.findViewById(R.id.tvAdoptanteName);
            tvFecha = itemView.findViewById(R.id.tvFechaCita);
            tvEstado = itemView.findViewById(R.id.tvEstadoSolicitud);
            tvContacto = itemView.findViewById(R.id.tvContactoInfo);
        }
    }
}