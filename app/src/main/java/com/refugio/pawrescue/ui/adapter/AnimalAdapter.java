package com.refugio.pawrescue.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.Animal;

import java.util.ArrayList;
import java.util.List;

public class AnimalAdapter extends RecyclerView.Adapter<AnimalAdapter.AnimalViewHolder> {

    private final Context context;
    private List<Animal> animalesList;
    private final OnAnimalClickListener listener;

    // Constructor 1: Para Admin / Voluntario (sin lista inicial)
    public AnimalAdapter(Context context, OnAnimalClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.animalesList = new ArrayList<>();
    }

    // Constructor 2: Para GalleryActivity (con lista inicial)
    public AnimalAdapter(Context context, List<Animal> animalesList, OnAnimalClickListener listener) {
        this.context = context;
        this.animalesList = animalesList != null ? animalesList : new ArrayList<>();
        this.listener = listener;
    }

    public void setAnimalesList(List<Animal> animalesList) {
        this.animalesList = animalesList != null ? animalesList : new ArrayList<>();
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

        // Cargar imagen
        if (animal.getFotoUrl() != null && !animal.getFotoUrl().isEmpty()) {
            Glide.with(context)
                    .load(animal.getFotoUrl())
                    .placeholder(R.drawable.ic_pet_placeholder)
                    .error(R.drawable.ic_pet_error)
                    .into(holder.ivAnimalPhoto);
        } else if (animal.getFotosUrls() != null && !animal.getFotosUrls().isEmpty()) {
            Glide.with(context)
                    .load(animal.getFotosUrls().get(0))
                    .placeholder(R.drawable.ic_pet_placeholder)
                    .error(R.drawable.ic_pet_error)
                    .into(holder.ivAnimalPhoto);
        } else {
            holder.ivAnimalPhoto.setImageResource(R.drawable.ic_pet_placeholder);
        }

        // Mostrar ID Numérico
        String animalIdDisplay = String.format("#%04d", animal.getIdNumerico());
        holder.tvAnimalName.setText(String.format("%s %s", animal.getNombre(), animalIdDisplay));
        holder.tvAnimalDetails.setText(String.format("%s • %s", animal.getEspecie(), animal.getRaza()));
        holder.tvAnimalStatus.setText(animal.getEstadoRefugio());

        // Puntos de estado
        String estado = animal.getEstadoRefugio() != null ? animal.getEstadoRefugio() : "";

        if (holder.dotStatusMain != null) {
            if (estado.equals("Disponible Adopcion") || estado.equals("En Proceso Adopción")) {
                holder.dotStatusMain.setVisibility(View.VISIBLE);
                holder.dotStatusMain.setBackgroundResource(R.drawable.legend_dot_red);
            } else {
                holder.dotStatusMain.setVisibility(View.GONE);
            }
        }

        if (holder.dotStatusMedication != null) {
            if (animal.getCondicionesEspeciales() != null && !animal.getCondicionesEspeciales().isEmpty()) {
                holder.dotStatusMedication.setVisibility(View.VISIBLE);
                holder.dotStatusMedication.setBackgroundResource(R.drawable.legend_dot_orange);
            } else {
                holder.dotStatusMedication.setVisibility(View.GONE);
            }
        }

        // Colores de estado
        int colorRes = R.color.status_rescued;
        if (estado.equals("Adoptado")) {
            colorRes = R.color.status_adopted;
        } else if (estado.equals("Disponible Adopcion") || estado.equals("En Proceso Adopción")) {
            colorRes = R.color.status_available;
        }

        try {
            holder.tvAnimalStatus.setBackgroundColor(ContextCompat.getColor(context, colorRes));
        } catch (Exception e) {
            // Ignorar si el color no existe
        }

        // Click en la tarjeta
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAnimalClick(animal);
            }
        });

        // Botón de favorito
        if (holder.btnFavorite != null) {
            if (animal.isFavorited()) {
                holder.btnFavorite.setImageResource(R.drawable.ic_favorite_filled);
            } else {
                holder.btnFavorite.setImageResource(R.drawable.ic_favorite_border);
            }

            holder.btnFavorite.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFavoriteClick(animal);
                }
            });
        }
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
        final View dotStatusMain;
        final View dotStatusMedication;
        final ImageButton btnFavorite;

        public AnimalViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            ivAnimalPhoto = itemView.findViewById(R.id.iv_animal_photo);
            tvAnimalName = itemView.findViewById(R.id.tv_animal_name);
            tvAnimalDetails = itemView.findViewById(R.id.tv_animal_details);
            tvAnimalStatus = itemView.findViewById(R.id.tv_animal_status);
            dotStatusMain = itemView.findViewById(R.id.dot_status_main);
            dotStatusMedication = itemView.findViewById(R.id.dot_status_medication);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }

    public interface OnAnimalClickListener {
        void onAnimalClick(Animal animal);

        // Método con implementación por defecto (opcional)
        default void onFavoriteClick(Animal animal) {
            // Vacío por defecto - solo lo usa la vista pública
        }
    }
}