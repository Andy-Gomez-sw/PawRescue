package com.refugio.pawrescue.ui.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.refugio.pawrescue.R;

// 🔴 IMPORTANTE: Aquí estaba el error.
// Estamos importando la clase desde donde TÚ la tienes (ui.publico)
import com.refugio.pawrescue.ui.publico.AdoptionRequest;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AdoptionRequestAdapter extends RecyclerView.Adapter<AdoptionRequestAdapter.ViewHolder> {

    private Context context;
    private List<AdoptionRequest> requests;
    private OnRequestClickListener listener;

    public interface OnRequestClickListener {
        void onRequestClick(AdoptionRequest request);
    }

    public AdoptionRequestAdapter(Context context, List<AdoptionRequest> requests, OnRequestClickListener listener) {
        this.context = context;
        this.requests = requests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Asegúrate de que el nombre del layout XML sea correcto (item_request_card o item_adoption_request)
        View view = LayoutInflater.from(context).inflate(R.layout.item_request_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdoptionRequest req = requests.get(position);

        // 1. Nombre
        String nombre = req.getAnimalNombre() != null ? req.getAnimalNombre() : "Mascota";
        holder.tvAnimalName.setText(nombre);

        // 2. Raza (Usando tu modelo)
        if (req.getAnimalRaza() != null) {
            holder.tvBreed.setText(req.getAnimalRaza());
        } else {
            holder.tvBreed.setText("Raza no especificada");
        }

        // 3. Folio
        String folioText = req.getFolio() != null ? req.getFolio() : "Sin Folio";
        holder.tvFolio.setText(folioText);

        // 4. Fecha (Aquí daba el error, ahora funcionará)
        if (req.getFechaSolicitud() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            holder.tvDate.setText(sdf.format(req.getFechaSolicitud()));
        } else {
            holder.tvDate.setText("Pendiente");
        }

        // 5. Foto (Usando tu campo animalFotoUrl)
        if (req.getAnimalFotoUrl() != null && !req.getAnimalFotoUrl().isEmpty()) {
            Glide.with(context).load(req.getAnimalFotoUrl()).into(holder.ivAnimalPhoto);
        } else {
            holder.ivAnimalPhoto.setImageResource(R.drawable.ic_pet_placeholder); // Asegúrate de tener un icono default
        }

        // 6. Estado
        configureStatus(holder, req.getEstado());

        // 7. Cita (Usando tu clase interna CitaAgendada)
        if (req.getCitaAgendada() != null) {
            holder.appointmentCard.setVisibility(View.VISIBLE);
            holder.tvAppointmentInfo.setText("Cita: " + req.getCitaAgendada().getFechaHoraFormateada());
        } else {
            holder.appointmentCard.setVisibility(View.GONE);
        }

        // Clics
        holder.btnVerDetalles.setOnClickListener(v -> listener.onRequestClick(req));
        holder.itemView.setOnClickListener(v -> listener.onRequestClick(req));
    }

    private void configureStatus(ViewHolder holder, String estadoRaw) {
        String estado = estadoRaw != null ? estadoRaw.toLowerCase() : "pendiente";
        String textoEstado;
        int colorIcon;

        switch (estado) {
            case "aprobada":
                textoEstado = "Aprobada";
                colorIcon = Color.parseColor("#4CAF50"); // Verde
                break;
            case "rechazada":
                textoEstado = "No Aprobada";
                colorIcon = Color.parseColor("#F44336"); // Rojo
                break;
            case "cita_agendada":
                textoEstado = "Cita Agendada";
                colorIcon = Color.parseColor("#9C27B0"); // Morado
                break;
            case "en_revision":
                textoEstado = "En Revisión";
                colorIcon = Color.parseColor("#2196F3"); // Azul
                break;
            default:
                textoEstado = "Pendiente";
                colorIcon = Color.parseColor("#FF9800"); // Naranja
                break;
        }

        holder.tvStatus.setText(textoEstado);
        holder.tvStatus.setTextColor(colorIcon);
        holder.ivStatusIcon.setColorFilter(colorIcon);
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAnimalPhoto, ivStatusIcon;
        TextView tvAnimalName, tvBreed, tvFolio, tvDate, tvStatus, tvAppointmentInfo;
        MaterialCardView appointmentCard;
        MaterialButton btnVerDetalles;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAnimalPhoto = itemView.findViewById(R.id.ivAnimalPhoto);
            tvAnimalName = itemView.findViewById(R.id.tvAnimalName);
            tvBreed = itemView.findViewById(R.id.tvBreed);
            tvFolio = itemView.findViewById(R.id.tvFolio);
            tvDate = itemView.findViewById(R.id.tvDate);

            // Estado
            ivStatusIcon = itemView.findViewById(R.id.ivStatusIcon);
            tvStatus = itemView.findViewById(R.id.tvStatus);

            // Cita
            appointmentCard = itemView.findViewById(R.id.appointmentCard);
            tvAppointmentInfo = itemView.findViewById(R.id.tvAppointmentInfo);

            // Botón
            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);
        }
    }
}