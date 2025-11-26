package com.refugio.pawrescue.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.Cita;
import java.util.List;

/**
 * Adapter para que los voluntarios vean sus citas asignadas
 */
public class VolunteerAppointmentAdapter extends RecyclerView.Adapter<VolunteerAppointmentAdapter.ViewHolder> {

    private Context context;
    private List<Cita> citas;
    private OnCitaClickListener listener;

    public interface OnCitaClickListener {
        void onCitaClick(Cita cita);
    }

    public VolunteerAppointmentAdapter(List<Cita> citas, OnCitaClickListener listener) {
        this.citas = citas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_volunteer_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Cita cita = citas.get(position);
        holder.bind(cita);
    }

    @Override
    public int getItemCount() {
        return citas.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvAnimalName, tvUsuarioEmail, tvFechaHora, tvEstado;
        MaterialButton btnAccion;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvAnimalName = itemView.findViewById(R.id.tvAnimalName);
            tvUsuarioEmail = itemView.findViewById(R.id.tvUsuarioEmail);
            tvFechaHora = itemView.findViewById(R.id.tvFechaHora);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            btnAccion = itemView.findViewById(R.id.btnAccion);
        }

        void bind(Cita cita) {
            tvAnimalName.setText("🐾 " + cita.getAnimalNombre());
            tvUsuarioEmail.setText("Usuario: " + cita.getUsuarioEmail());
            tvFechaHora.setText("📅 " + cita.getFechaHoraCompleta());

            String estado = cita.getEstado();

            if ("asignada".equals(estado)) {
                tvEstado.setText("⏳ Pendiente de atender");
                tvEstado.setTextColor(context.getResources().getColor(R.color.status_review_text));
                btnAccion.setText("Crear Reporte");
                btnAccion.setVisibility(View.VISIBLE);
            } else if ("completada".equals(estado)) {
                tvEstado.setText("✅ Reporte enviado");
                tvEstado.setTextColor(context.getResources().getColor(R.color.status_approved_text));
                btnAccion.setText("Ver Reporte");
                btnAccion.setVisibility(View.VISIBLE);
            } else {
                tvEstado.setText("");
                btnAccion.setVisibility(View.GONE);
            }

            btnAccion.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCitaClick(cita);
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCitaClick(cita);
                }
            });
        }
    }
}