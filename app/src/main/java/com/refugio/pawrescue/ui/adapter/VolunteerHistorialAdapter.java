package com.refugio.pawrescue.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.VolunteerHistorialItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VolunteerHistorialAdapter extends RecyclerView.Adapter<VolunteerHistorialAdapter.HistorialViewHolder> {

    private final Context context;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    private List<VolunteerHistorialItem> items = new ArrayList<>();

    public VolunteerHistorialAdapter(Context context) {
        this.context = context;
    }

    public void setItems(List<VolunteerHistorialItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    private String textoSiNo(Boolean valor) {
        if (valor == null) return "No registrado";
        return valor ? "Sí" : "No";
    }

    private String textoComportamiento(Boolean normal) {
        if (normal == null) return "No registrado";
        return normal ? "Normal" : "Inusual";
    }




    @NonNull
    @Override
    public HistorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_animal_historial, parent, false);
        return new HistorialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistorialViewHolder holder, int position) {
        VolunteerHistorialItem item = items.get(position);

        // Fecha
        if (item.getFecha() != null) {
            holder.tvFecha.setText(dateFormat.format(item.getFecha().toDate()));
        } else {
            holder.tvFecha.setText("Fecha desconocida");
        }

        // Según tipo
        if (VolunteerHistorialItem.TIPO_OBSERVACION.equals(item.getTipo())) {
            // Observación
            holder.tvIcono.setText("📝");
            holder.tvTitulo.setText("Observación del voluntario");

            StringBuilder detalle = new StringBuilder();

            detalle.append("Comió bien: ")
                    .append(textoSiNo(item.getComioBien()))
                    .append(" • Medicamentos: ")
                    .append(textoSiNo(item.getMedicamentosOk()))
                    .append(" • Comportamiento: ")
                    .append(textoComportamiento(item.getComportamientoNormal()));

            String notas = item.getDetalle();
            if (notas != null && !notas.isEmpty()) {
                detalle.append("\nNotas: ").append(notas);
            }

            holder.tvDetalle.setText(detalle.toString());
            holder.imgFoto.setVisibility(View.GONE);

        } else if (VolunteerHistorialItem.TIPO_FOTO.equals(item.getTipo())) {
            // Foto
            holder.tvIcono.setText("📷");
            holder.tvTitulo.setText("Foto del estado");

            String notas = item.getDetalle();
            holder.tvDetalle.setText(
                    (notas != null && !notas.isEmpty())
                            ? notas
                            : "Foto subida por el voluntario."
            );

            if (item.getFotoUrl() != null && !item.getFotoUrl().isEmpty()) {
                holder.imgFoto.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(item.getFotoUrl())
                        .centerCrop()
                        .into(holder.imgFoto);
            } else {
                holder.imgFoto.setVisibility(View.GONE);
            }

        } else if (VolunteerHistorialItem.TIPO_ACCION.equals(item.getTipo())) {
            // Acciones rápidas: atendido hoy / cita / problema
            String tipoAccion = item.getTipoAccion();
            if ("ATENDIDO_HOY".equals(tipoAccion)) {
                holder.tvIcono.setText("✅");
                holder.tvTitulo.setText("Atendido hoy");
            } else if ("CITA_CONFIRMADA".equals(tipoAccion)) {
                holder.tvIcono.setText("📅");
                holder.tvTitulo.setText("Cita confirmada");
            } else if ("PROBLEMA".equals(tipoAccion)) {
                holder.tvIcono.setText("⚠️");
                holder.tvTitulo.setText("Problema reportado");
            } else {
                holder.tvIcono.setText("ℹ️");
                holder.tvTitulo.setText("Acción del voluntario");
            }

            String detalle = item.getDetalle();
            holder.tvDetalle.setText(
                    (detalle != null && !detalle.isEmpty())
                            ? detalle
                            : "Acción registrada por el voluntario."
            );

            holder.imgFoto.setVisibility(View.GONE);
        } else {
            // Por si acaso, tipo desconocido
            holder.tvIcono.setText("ℹ️");
            holder.tvTitulo.setText("Registro");
            holder.tvDetalle.setText(item.getDetalle() != null ? item.getDetalle() : "");
            holder.imgFoto.setVisibility(View.GONE);
        }
    }



    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HistorialViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcono, tvTitulo, tvFecha, tvDetalle;
        ImageView imgFoto;

        HistorialViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcono = itemView.findViewById(R.id.tvIcono);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvDetalle = itemView.findViewById(R.id.tvDetalle);
            imgFoto = itemView.findViewById(R.id.imgFotoHistorial);
        }
    }
}
