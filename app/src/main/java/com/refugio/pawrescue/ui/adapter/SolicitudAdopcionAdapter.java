package com.refugio.pawrescue.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.SolicitudAdopcion;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adaptador para el RecyclerView que muestra las solicitudes de adopción (RF-14).
 */
public class SolicitudAdopcionAdapter extends RecyclerView.Adapter<SolicitudAdopcionAdapter.SolicitudViewHolder> {

    private final Context context;
    private List<SolicitudAdopcion> solicitudList;
    private final SolicitudInteractionListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MMM/yyyy", new Locale("es", "ES"));

    public interface SolicitudInteractionListener {
        void onAgendarCitaClick(SolicitudAdopcion solicitud);
        void onRegistrarResultadoClick(SolicitudAdopcion solicitud);
    }

    public SolicitudAdopcionAdapter(Context context, SolicitudInteractionListener listener) {
        this.context = context;
        this.solicitudList = new ArrayList<>();
        this.listener = listener;
    }

    public void setSolicitudList(List<SolicitudAdopcion> solicitudList) {
        this.solicitudList = solicitudList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SolicitudViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_solicitud_adopcion, parent, false);
        return new SolicitudViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SolicitudViewHolder holder, int position) {
        SolicitudAdopcion solicitud = solicitudList.get(position);

        holder.tvNombre.setText(solicitud.getNombreAdoptante());
        holder.tvTelefono.setText("Tel: " + solicitud.getTelefonoAdoptante());
        holder.tvEstado.setText("Estado: " + solicitud.getEstadoSolicitud());

        if (solicitud.getFechaSolicitud() != null) {
            holder.tvFecha.setText(dateFormat.format(solicitud.getFechaSolicitud().toDate()));
        }

        // Lógica de color y visibilidad según el estado
        int colorEstado;
        boolean enableCita = false;
        boolean enableResultado = false;

        switch (solicitud.getEstadoSolicitud()) {
            case "Pendiente":
                colorEstado = ContextCompat.getColor(context, R.color.accent_orange);
                enableCita = true;
                break;
            case "Aprobada":
                colorEstado = ContextCompat.getColor(context, R.color.status_success);
                enableResultado = true;
                break;
            case "Rechazada":
                colorEstado = ContextCompat.getColor(context, R.color.status_error);
                break;
            case "Cita Agendada":
                colorEstado = ContextCompat.getColor(context, R.color.accent_blue);
                enableResultado = true;
                break;
            default:
                colorEstado = ContextCompat.getColor(context, R.color.text_secondary);
        }

        holder.tvEstado.setTextColor(colorEstado);

        // Habilitar/Deshabilitar botones
        holder.btnAgendarCita.setEnabled(enableCita);
        holder.btnRegistrarResultado.setEnabled(enableResultado);

        // Listeners
        holder.btnAgendarCita.setOnClickListener(v -> listener.onAgendarCitaClick(solicitud));
        holder.btnRegistrarResultado.setOnClickListener(v -> listener.onRegistrarResultadoClick(solicitud));
    }

    @Override
    public int getItemCount() {
        return solicitudList.size();
    }

    static class SolicitudViewHolder extends RecyclerView.ViewHolder {
        final TextView tvNombre, tvTelefono, tvFecha, tvEstado;
        final Button btnAgendarCita, btnRegistrarResultado;

        public SolicitudViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tv_adoptante_nombre);
            tvTelefono = itemView.findViewById(R.id.tv_adoptante_telefono);
            tvFecha = itemView.findViewById(R.id.tv_fecha_solicitud);
            tvEstado = itemView.findViewById(R.id.tv_solicitud_estado);
            btnAgendarCita = itemView.findViewById(R.id.btn_agendar_cita);
            btnRegistrarResultado = itemView.findViewById(R.id.btn_registrar_resultado);
        }
    }
}