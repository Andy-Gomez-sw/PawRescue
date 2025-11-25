package com.refugio.pawrescue.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.HistorialMedico;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistorialViewHolder> {

    private final Context context;
    private List<HistorialMedico> historialList;
    private final HistoryActionListener listener;

    public interface HistoryActionListener {
        void onDeleteClick(HistorialMedico evento);
        void onItemClick(HistorialMedico evento); // Para ver los detalles
    }

    public HistoryAdapter(Context context, HistoryActionListener listener) {
        this.context = context;
        this.historialList = new ArrayList<>();
        this.listener = listener;
    }

    public void setHistorial(List<HistorialMedico> historial) {
        this.historialList = historial;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_historial_evento, parent, false);
        return new HistorialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistorialViewHolder holder, int position) {
        HistorialMedico evento = historialList.get(position);

        holder.tvTipoEvento.setText(evento.getTipoEvento());
        holder.tvDiagnostico.setText(evento.getDiagnostico());

        if (evento.getFecha() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.tvFecha.setText(sdf.format(evento.getFecha().toDate()));
        }

        holder.tvVeterinario.setText("Dr. " + evento.getVeterinario());

        // Listener para ver detalles (Clic en la tarjeta)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(evento);
            }
        });

        // Listener para eliminar (Clic en el botón)
        holder.btnDeleteEvento.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(evento);
            }
        });
    }

    @Override
    public int getItemCount() {
        return historialList.size();
    }

    static class HistorialViewHolder extends RecyclerView.ViewHolder {
        TextView tvTipoEvento, tvDiagnostico, tvFecha, tvVeterinario;
        ImageButton btnDeleteEvento;

        public HistorialViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTipoEvento = itemView.findViewById(R.id.tv_tipo_evento);
            tvDiagnostico = itemView.findViewById(R.id.tv_diagnostico);
            tvFecha = itemView.findViewById(R.id.tv_fecha);
            tvVeterinario = itemView.findViewById(R.id.tv_veterinario);
            btnDeleteEvento = itemView.findViewById(R.id.btn_delete_evento);
        }
    }
}