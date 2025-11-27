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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore; // 🟢 Importante
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.ui.publico.AdoptionRequest;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    private Context context;
    private List<AdoptionRequest> requests;
    private OnRequestClickListener listener;
    private FirebaseFirestore db; // 🟢 Instancia de Firestore

    public interface OnRequestClickListener {
        void onRequestClick(AdoptionRequest request);
    }

    public RequestAdapter(Context context, List<AdoptionRequest> requests, OnRequestClickListener listener) {
        this.context = context;
        this.requests = requests;
        this.listener = listener;
        this.db = FirebaseFirestore.getInstance(); // 🟢 Inicializar Firestore
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

        // 1. Textos Básicos
        holder.tvAnimalName.setText(req.getAnimalNombre() != null ? req.getAnimalNombre() : "Mascota");

        if (req.getAnimalRaza() != null) {
            holder.tvBreed.setText(req.getAnimalRaza());
        } else {
            holder.tvBreed.setText("Raza no especificada");
        }

        // 2. Folio
        String idStr = req.getId();
        String folioText = req.getFolio();
        if (folioText != null && !folioText.isEmpty()) {
            holder.tvFolio.setText(folioText);
        } else {
            if (idStr != null && idStr.length() >= 8) {
                holder.tvFolio.setText("ID: " + idStr.substring(0, 8));
            } else {
                holder.tvFolio.setText("ID: Pendiente");
            }
        }

        // 3. Fecha
        if (req.getFechaSolicitud() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            holder.tvDate.setText(sdf.format(req.getFechaSolicitud()));
        } else {
            holder.tvDate.setText("Fecha pendiente");
        }

        // 🟢 4. LÓGICA DE IMAGEN (CORRECCIÓN)
        // Primero intentamos cargar la URL que viene en la solicitud
        if (req.getAnimalFotoUrl() != null && !req.getAnimalFotoUrl().isEmpty()) {
            Glide.with(context)
                    .load(req.getAnimalFotoUrl())
                    .placeholder(R.drawable.ic_pet_placeholder)
                    .into(holder.ivAnimalPhoto);
        } else {
            // ⚠️ SI NO HAY URL, LA BUSCAMOS EN FIRESTORE EN TIEMPO REAL
            holder.ivAnimalPhoto.setImageResource(R.drawable.ic_pet_placeholder); // Placeholder temporal

            if (req.getAnimalId() != null) {
                db.collection("animales").document(req.getAnimalId())
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                // Buscamos el campo de la imagen (prueba ambos nombres comunes)
                                String url = documentSnapshot.getString("imageUrl");
                                if (url == null) url = documentSnapshot.getString("fotoUrl");

                                if (url != null && !url.isEmpty()) {
                                    // Guardamos la URL en el objeto para que al hacer scroll no la busque de nuevo
                                    req.setAnimalFotoUrl(url);

                                    // Cargamos la imagen encontrada
                                    Glide.with(context)
                                            .load(url)
                                            .placeholder(R.drawable.ic_pet_placeholder)
                                            .into(holder.ivAnimalPhoto);
                                }
                            }
                        });
            }
        }

        // 5. Estado
        configureStatus(holder, req.getEstado());

        // 6. Cita
        if (req.getCitaAgendada() != null) {
            holder.appointmentCard.setVisibility(View.VISIBLE);
            holder.tvAppointmentInfo.setText("Cita: " + req.getCitaAgendada().getFechaHoraFormateada());
        } else {
            holder.appointmentCard.setVisibility(View.GONE);
        }

        // Listeners
        holder.btnVerDetalles.setOnClickListener(v -> listener.onRequestClick(req));
        holder.itemView.setOnClickListener(v -> listener.onRequestClick(req));
    }

    private void configureStatus(ViewHolder holder, String estadoRaw) {
        String estado = estadoRaw != null ? estadoRaw.toLowerCase() : "pendiente";
        String textoEstado;
        int colorEstado; // Variable para el color del texto

        switch (estado) {
            case "aprobada":
            case "adoptado":
                textoEstado = "Aprobada";
                colorEstado = context.getResources().getColor(android.R.color.holo_green_dark);
                break;
            case "rechazada":
                textoEstado = "No Aprobada";
                colorEstado = context.getResources().getColor(android.R.color.holo_red_dark);
                break;
            case "cita_agendada":
                textoEstado = "Cita Agendada";
                colorEstado = context.getResources().getColor(android.R.color.holo_purple);
                break;
            case "en_revision":
                textoEstado = "En Revisión";
                colorEstado = context.getResources().getColor(android.R.color.holo_blue_dark);
                break;
            default:
                textoEstado = "Pendiente";
                colorEstado = context.getResources().getColor(android.R.color.holo_orange_dark);
                break;
        }

        holder.tvStatus.setText(textoEstado);
        holder.tvStatus.setTextColor(colorEstado); // Aplicamos color al texto
        holder.ivStatusIcon.setColorFilter(colorEstado); // Aplicamos color al icono
    }

    @Override
    public int getItemCount() {
        return requests != null ? requests.size() : 0;
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