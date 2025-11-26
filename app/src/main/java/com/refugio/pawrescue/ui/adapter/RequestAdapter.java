package com.refugio.pawrescue.ui.adapter;

import android.content.Context;
import android.graphics.Color; // Se mantiene por si usas Color.BLACK en otro lado, pero no para el estado
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.ui.publico.AdoptionRequest;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    private Context context;
    private List<AdoptionRequest> requests;
    private OnRequestClickListener listener;

    public interface OnRequestClickListener {
        void onRequestClick(AdoptionRequest request);
    }

    public RequestAdapter(Context context, List<AdoptionRequest> requests, OnRequestClickListener listener) {
        this.context = context;
        this.requests = requests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_request_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdoptionRequest req = requests.get(position);

        holder.tvAnimalName.setText(req.getAnimalNombre() != null ? req.getAnimalNombre() : "Mascota");

        if (req.getAnimalRaza() != null) {
            holder.tvBreed.setText(req.getAnimalRaza());
        } else {
            holder.tvBreed.setText("Raza no especificada");
        }

        String idStr = req.getId();
        String folioText = req.getFolio();

        if (folioText != null && !folioText.isEmpty()) {
            holder.tvFolio.setText(folioText);
        } else {
            if (idStr != null && idStr.length() >= 8) {
                holder.tvFolio.setText("ID: " + idStr.substring(0, 8));
            } else {
                holder.tvFolio.setText("ID: " + (idStr != null ? idStr : "Pendiente"));
            }
        }

        if (req.getFechaSolicitud() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            holder.tvDate.setText(sdf.format(req.getFechaSolicitud()));
        } else {
            holder.tvDate.setText("Fecha pendiente");
        }

        if (req.getAnimalFotoUrl() != null && !req.getAnimalFotoUrl().isEmpty()) {
            Glide.with(context).load(req.getAnimalFotoUrl()).into(holder.ivAnimalPhoto);
        } else {
            holder.ivAnimalPhoto.setImageResource(R.drawable.ic_pet_placeholder);
        }

        // Configuración de estado (Solo texto, colores vienen del XML)
        configureStatus(holder, req.getEstado());

        if (req.getCitaAgendada() != null) {
            holder.appointmentCard.setVisibility(View.VISIBLE);
            holder.tvAppointmentInfo.setText("Cita: " + req.getCitaAgendada().getFechaHoraFormateada());
        } else {
            holder.appointmentCard.setVisibility(View.GONE);
        }

        holder.btnVerDetalles.setOnClickListener(v -> listener.onRequestClick(req));
        holder.itemView.setOnClickListener(v -> listener.onRequestClick(req));
    }

    private void configureStatus(ViewHolder holder, String estadoRaw) {
        String estado = estadoRaw != null ? estadoRaw.toLowerCase() : "pendiente";
        String textoEstado;

        // Definimos SOLO el texto según el estado
        switch (estado) {
            case "aprobada":
                textoEstado = "Aprobada";
                break;
            case "rechazada":
                textoEstado = "No Aprobada";
                break;
            case "cita_agendada":
                textoEstado = "Cita Agendada";
                break;
            case "en_revision":
                textoEstado = "En Revisión";
                break;
            default:
                textoEstado = "Pendiente";
                break;
        }

        holder.tvStatus.setText(textoEstado);

        // 🧹 LIMPIEZA: Eliminamos setCardBackgroundColor, setTextColor y setColorFilter
        // Ahora el XML manda y asegura que sea Naranja con texto Negro.
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAnimalPhoto, ivStatusIcon;
        TextView tvAnimalName, tvBreed, tvFolio, tvDate, tvStatus, tvAppointmentInfo;
        MaterialCardView appointmentCard, statusBadge;
        MaterialButton btnVerDetalles;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAnimalPhoto = itemView.findViewById(R.id.ivAnimalPhoto);
            tvAnimalName = itemView.findViewById(R.id.tvAnimalName);
            tvBreed = itemView.findViewById(R.id.tvBreed);
            tvFolio = itemView.findViewById(R.id.tvFolio);
            tvDate = itemView.findViewById(R.id.tvDate);
            ivStatusIcon = itemView.findViewById(R.id.ivStatusIcon);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            appointmentCard = itemView.findViewById(R.id.appointmentCard);
            statusBadge = itemView.findViewById(R.id.statusBadge);
            tvAppointmentInfo = itemView.findViewById(R.id.tvAppointmentInfo);
            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);
        }
    }
}