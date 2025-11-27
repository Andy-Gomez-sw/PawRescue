package com.refugio.pawrescue.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.SolicitudAdopcion;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SolicitudAdopcionAdapter extends RecyclerView.Adapter<SolicitudAdopcionAdapter.ViewHolder> {

    private final Context context;
    private List<SolicitudAdopcion> listaSolicitudes;
    private final SolicitudInteractionListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MMM/yyyy", Locale.getDefault());

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

    public void setSolicitudList(List<SolicitudAdopcion> listaSolicitudes) {
        setListaSolicitudes(listaSolicitudes);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Se infla item_solicitud_adopcion.xml
        View view = LayoutInflater.from(context).inflate(R.layout.item_solicitud_adopcion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SolicitudAdopcion solicitud = listaSolicitudes.get(position);

        // 1. MAPEAMOS LOS DATOS
        holder.tvAdoptanteNombre.setText(solicitud.getNombreAdoptante());
        holder.tvAdoptanteTelefono.setText("Tel: " + solicitud.getTelefonoAdoptante());

        // Estado
        String estado = solicitud.getEstado() != null ? solicitud.getEstado() : "Pendiente";
        holder.tvSolicitudEstado.setText("Estado: " + estado);

        // Fecha de Solicitud
        if (solicitud.getFechaSolicitud() != null) {
            holder.tvFechaSolicitud.setText(dateFormat.format(solicitud.getFechaSolicitud()));
        } else {
            holder.tvFechaSolicitud.setText("--/--/----");
        }

        // 2. LÓGICA DE BOTONES (Fases)

        boolean isCitaAgendada = solicitud.getFechaCita() != null;
        boolean isVoluntarioAsignado = solicitud.getVoluntarioId() != null;
        boolean isReporteEnviado = solicitud.getReporteId() != null;

        // --- Configuración Botón 1 (btn_agendar_cita -> Acción Principal) ---
        holder.btnAgendarCita.setVisibility(View.VISIBLE);
        holder.btnAgendarCita.setEnabled(true);

        if (isReporteEnviado) {
            // 🟢 FASE 3: Reporte Enviado -> Decisión Final
            holder.btnAgendarCita.setText("VER REPORTE / DECIDIR"); // 🚨 Texto solicitado
            holder.btnAgendarCita.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.primary_green));
            holder.btnAgendarCita.setEnabled(true);
        } else if (!isCitaAgendada) {
            // Estado Inicial: El usuario aún no ha agendado.
            holder.btnAgendarCita.setText("CITA PENDIENTE");
            holder.btnAgendarCita.setBackgroundTintList(ContextCompat.getColorStateList(context, android.R.color.darker_gray));
            holder.btnAgendarCita.setEnabled(false);
        } else if (!isVoluntarioAsignado) {
            // FASE 1: Cita agendada, requiere asignación.
            holder.btnAgendarCita.setText("ASIGNAR VOLUNTARIO");
            holder.btnAgendarCita.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.accent_orange));
        } else {
            // FASE 2: Voluntario asignado, esperando reporte.
            holder.btnAgendarCita.setText("ESPERANDO REPORTE");
            holder.btnAgendarCita.setBackgroundTintList(ContextCompat.getColorStateList(context, android.R.color.holo_orange_dark));
            holder.btnAgendarCita.setEnabled(false);
        }

        // --- Configuración Botón 2 (btn_registrar_resultado -> VER DETALLES) ---
        holder.btnRegistrarResultado.setVisibility(View.VISIBLE);
        holder.btnRegistrarResultado.setText("VER DETALLES");
        holder.btnRegistrarResultado.setBackgroundTintList(ContextCompat.getColorStateList(context, android.R.color.holo_blue_dark));


        // 3. LISTENERS

        // Clic en el botón principal
        holder.btnAgendarCita.setOnClickListener(v -> {
            if (holder.btnAgendarCita.isEnabled()) {
                listener.onSolicitudClick(solicitud);
            } else {
                Toast.makeText(context, "Acción bloqueada. Estado: " + holder.btnAgendarCita.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        // Clic en el botón DETALLES
        holder.btnRegistrarResultado.setOnClickListener(v -> listener.onSolicitudClick(solicitud));
    }

    @Override
    public int getItemCount() {
        return listaSolicitudes != null ? listaSolicitudes.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvAdoptanteNombre;
        final TextView tvFechaSolicitud;
        final TextView tvSolicitudEstado;
        final TextView tvAdoptanteTelefono;
        final Button btnAgendarCita;
        final Button btnRegistrarResultado;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // IDs del layout item_solicitud_adopcion.xml
            tvAdoptanteNombre = itemView.findViewById(R.id.tv_adoptante_nombre);
            tvFechaSolicitud = itemView.findViewById(R.id.tv_fecha_solicitud);
            tvSolicitudEstado = itemView.findViewById(R.id.tv_solicitud_estado);
            tvAdoptanteTelefono = itemView.findViewById(R.id.tv_adoptante_telefono);
            btnAgendarCita = itemView.findViewById(R.id.btn_agendar_cita);
            btnRegistrarResultado = itemView.findViewById(R.id.btn_registrar_resultado);
        }
    }
}