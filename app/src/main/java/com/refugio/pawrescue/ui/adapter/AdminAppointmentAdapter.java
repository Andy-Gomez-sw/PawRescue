package com.refugio.pawrescue.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.Cita;
import java.util.List;

/**
 * Adapter para mostrar las citas en el panel del Admin
 */
public class AdminAppointmentAdapter extends RecyclerView.Adapter<AdminAppointmentAdapter.ViewHolder> {

    private Context context;
    private List<Cita> citas;
    private OnCitaClickListener listener;

    public interface OnCitaClickListener {
        void onCitaClick(Cita cita);
    }

    public AdminAppointmentAdapter(List<Cita> citas, OnCitaClickListener listener) {
        this.citas = citas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_admin_appointment, parent, false);
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
        TextView tvAnimalName, tvUsuarioEmail, tvFechaHora, tvEstadoBadge, tvVoluntarioNombre;
        LinearLayout llVoluntario;
        MaterialButton btnAccion;

        ViewHolder(View itemView) {
            super(itemView);
            tvAnimalName = itemView.findViewById(R.id.tvAnimalName);
            tvUsuarioEmail = itemView.findViewById(R.id.tvUsuarioEmail);
            tvFechaHora = itemView.findViewById(R.id.tvFechaHora);
            tvEstadoBadge = itemView.findViewById(R.id.tvEstadoBadge);
            tvVoluntarioNombre = itemView.findViewById(R.id.tvVoluntarioNombre);
            llVoluntario = itemView.findViewById(R.id.llVoluntario);
            btnAccion = itemView.findViewById(R.id.btnAccion);
        }

        void bind(Cita cita) {
            tvAnimalName.setText(cita.getAnimalNombre());
            tvUsuarioEmail.setText(cita.getUsuarioEmail());
            tvFechaHora.setText(cita.getFechaHoraCompleta());

            // Configurar badge de estado
            String estado = cita.getEstado();
            tvEstadoBadge.setText(cita.getEstadoTexto());

            switch (estado) {
                case "pendiente_asignacion":
                    tvEstadoBadge.setBackgroundResource(R.drawable.bg_badge_warning);
                    btnAccion.setText("Asignar Voluntario");
                    btnAccion.setVisibility(View.VISIBLE);
                    llVoluntario.setVisibility(View.GONE);
                    break;

                case "asignada":
                    tvEstadoBadge.setBackgroundResource(R.drawable.bg_badge_info);
                    btnAccion.setVisibility(View.GONE);
                    llVoluntario.setVisibility(View.VISIBLE);
                    tvVoluntarioNombre.setText(cita.getVoluntarioNombre());
                    break;

                case "completada":
                    tvEstadoBadge.setBackgroundResource(R.drawable.bg_badge_success);
                    llVoluntario.setVisibility(View.VISIBLE);
                    tvVoluntarioNombre.setText(cita.getVoluntarioNombre());

                    if (cita.isReporteCompleto()) {
                        btnAccion.setText("Ver Reporte");
                        btnAccion.setVisibility(View.VISIBLE);
                    } else {
                        btnAccion.setVisibility(View.GONE);
                    }
                    break;

                case "cancelada":
                    tvEstadoBadge.setBackgroundResource(R.drawable.bg_badge_error);
                    btnAccion.setVisibility(View.GONE);
                    llVoluntario.setVisibility(View.GONE);
                    break;

                default:
                    btnAccion.setVisibility(View.GONE);
                    llVoluntario.setVisibility(View.GONE);
            }

            // Click en el botón de acción
            btnAccion.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCitaClick(cita);
                }
            });

            // Click en la tarjeta completa para ver detalles
            itemView.setOnClickListener(v -> {
                if (listener != null && "completada".equals(estado)) {
                    listener.onCitaClick(cita);
                }
            });
        }
    }
}