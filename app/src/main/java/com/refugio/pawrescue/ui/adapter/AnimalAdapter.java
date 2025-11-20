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
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.Animal;

import java.util.List;

/**
 * Adaptador para el RecyclerView que muestra los animales en formato de tarjeta.
 * Se adhiere al diseño del mockup "Mis Animales".
 */
public class AnimalAdapter extends RecyclerView.Adapter<AnimalAdapter.AnimalViewHolder> {

    private final Context context;
    private List<Animal> animalesList;
    private final OnAnimalClickListener listener;

    public AnimalAdapter(Context context, OnAnimalClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setAnimalesList(List<Animal> animalesList) {
        this.animalesList = animalesList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AnimalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_animal_card, parent, false);
        return new AnimalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnimalViewHolder holder, int position) {
        Animal animal = animalesList.get(position);

        // Cargar imagen usando Glide (o Picasso) para manejo eficiente (RF-06)
        Glide.with(context)
                .load(animal.getFotoUrl())
                .placeholder(R.drawable.ic_pet_placeholder) // Placeholder de huella
                .error(R.drawable.ic_pet_error)
                .into(holder.ivAnimalPhoto);

        // Mostrar ID Numérico como #0001
        String animalIdDisplay = String.format("#%04d", animal.getIdNumerico());
        holder.tvAnimalName.setText(String.format("%s %s", animal.getNombre(), animalIdDisplay));

        holder.tvAnimalDetails.setText(String.format("%s • %s", animal.getEspecie(), animal.getRaza()));
        holder.tvAnimalStatus.setText(animal.getEstadoRefugio());

        // --- LÓGICA DE PUNTOS DE ESTADO ---
        String estado = animal.getEstadoRefugio();

        // Punto 1: Alerta o Disponibilidad (Rojo/Naranja fuerte)
        if (estado.equals("Disponible Adopcion") || estado.equals("En Proceso Adopción")) {
            holder.dotStatusMain.setVisibility(View.VISIBLE);
            holder.dotStatusMain.setBackgroundResource(R.drawable.legend_dot_red);
        } else {
            holder.dotStatusMain.setVisibility(View.GONE);
        }

        // Punto 2: Medicación Activa (Simulación: Si tiene "Tratamiento" en condiciones especiales)
        // NOTA: Para una implementación real, tendrías que consultar el Historial Médico.
        // Aquí simulamos si tiene alguna condición que requiera atención.
        if (animal.getCondicionesEspeciales() != null && !animal.getCondicionesEspeciales().isEmpty()) {
            holder.dotStatusMedication.setVisibility(View.VISIBLE);
            holder.dotStatusMedication.setBackgroundResource(R.drawable.legend_dot_orange);
        } else {
            holder.dotStatusMedication.setVisibility(View.GONE);
        }

        // Colores de la Etiqueta de Estado (Mockup inferior)
        if (estado.equals("Adoptado")) {
            holder.tvAnimalStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.status_adopted));
        } else if (estado.equals("Disponible Adopcion") || estado.equals("En Proceso Adopción")) {
            holder.tvAnimalStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.status_available));
        } else {
            holder.tvAnimalStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.status_rescued));
        }

        // Click en la tarjeta para ir al detalle (RF-06)
        holder.cardView.setOnClickListener(v -> listener.onAnimalClick(animal));
    }

    @Override
    public int getItemCount() {
        return animalesList != null ? animalesList.size() : 0;
    }

    public static class AnimalViewHolder extends RecyclerView.ViewHolder {
        final CardView cardView;
        final ImageView ivAnimalPhoto;
        final TextView tvAnimalName;
        final TextView tvAnimalDetails;
        final TextView tvAnimalStatus;
        // Puntos de estado
        final View dotStatusMain;
        final View dotStatusMedication;

        public AnimalViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            ivAnimalPhoto = itemView.findViewById(R.id.iv_animal_photo);
            tvAnimalName = itemView.findViewById(R.id.tv_animal_name);
            tvAnimalDetails = itemView.findViewById(R.id.tv_animal_details);
            tvAnimalStatus = itemView.findViewById(R.id.tv_animal_status);
            dotStatusMain = itemView.findViewById(R.id.dot_status_main);
            dotStatusMedication = itemView.findViewById(R.id.dot_status_medication);
        }
    }

    public interface OnAnimalClickListener {
        void onAnimalClick(Animal animal);
    }
}