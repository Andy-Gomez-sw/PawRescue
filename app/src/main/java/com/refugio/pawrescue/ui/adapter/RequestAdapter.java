package com.refugio.pawrescue.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import java.util.List;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.AdoptionRequest;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private Context context;
    private List<AdoptionRequest> requestList;
    private OnRequestClickListener listener;

    public interface OnRequestClickListener {
        void onRequestClick(AdoptionRequest request);
    }

    public RequestAdapter(Context context, List<AdoptionRequest> requestList, OnRequestClickListener listener) {
        this.context = context;
        this.requestList = requestList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_request_card, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        AdoptionRequest request = requestList.get(position);
        holder.bind(request, listener, context);
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivAnimalPhoto;
        private TextView tvAnimalName;
        private TextView tvBreed;
        private TextView tvFolio;
        private TextView tvDate;
        private CardView statusBadge;
        private ImageView ivStatusIcon;
        private TextView tvStatus;
        private CardView appointmentCard;
        private TextView tvAppointmentInfo;
        private MaterialButton btnVerDetalles;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAnimalPhoto = itemView.findViewById(R.id.ivAnimalPhoto);
            tvAnimalName = itemView.findViewById(R.id.tvAnimalName);
            tvBreed = itemView.findViewById(R.id.tvBreed);
            tvFolio = itemView.findViewById(R.id.tvFolio);
            tvDate = itemView.findViewById(R.id.tvDate);
            statusBadge = itemView.findViewById(R.id.statusBadge);
            ivStatusIcon = itemView.findViewById(R.id.ivStatusIcon);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            appointmentCard = itemView.findViewById(R.id.appointmentCard);
            tvAppointmentInfo = itemView.findViewById(R.id.tvAppointmentInfo);
            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);
        }

        public void bind(AdoptionRequest request, OnRequestClickListener listener, Context context) {
            // Cargar foto del animal
            if (request.getAnimalFotoUrl() != null) {
                Glide.with(context)
                        .load(request.getAnimalFotoUrl())
                        .placeholder(R.drawable.placeholder_animal)
                        .into(ivAnimalPhoto);
            } else {
                ivAnimalPhoto.setImageResource(R.drawable.placeholder_animal);
            }

            // Información del animal
            tvAnimalName.setText(request.getAnimalNombre());
            tvBreed.setText(request.getAnimalRaza() != null ? request.getAnimalRaza() : "");

            // Folio y fecha
            tvFolio.setText("Folio: " + request.getFolio());
            tvDate.setText(request.getFechaFormateada());

            // Badge de estado
            tvStatus.setText(request.getEstadoTexto());
            ivStatusIcon.setImageResource(request.getEstadoIcon());

            // Colores del badge según estado
            int backgroundColor = ContextCompat.getColor(context, request.getEstadoColor());
            statusBadge.setCardBackgroundColor(backgroundColor);

            // Mostrar información de cita si existe
            if (request.getEstado().equals("cita_agendada") && request.getCitaAgendada() != null) {
                appointmentCard.setVisibility(View.VISIBLE);
                tvAppointmentInfo.setText("Cita: " + request.getCitaAgendada().getFechaHoraFormateada());
            } else {
                appointmentCard.setVisibility(View.GONE);
            }

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRequestClick(request);
                }
            });

            btnVerDetalles.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRequestClick(request);
                }
            });
        }
    }
}
