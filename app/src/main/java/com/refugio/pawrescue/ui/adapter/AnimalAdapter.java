package com.refugio.pawrescue.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // Agregado para el botón de fav
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.Animal;

import java.util.ArrayList; // Agregado para inicializar lista vacía
import java.util.List;

/**
 * Adaptador para el RecyclerView que muestra los animales en formato de tarjeta.
 * Se adhiere al diseño del mockup "Mis Animales" y soporta la Galería Pública.
 */
public class AnimalAdapter extends RecyclerView.Adapter<AnimalAdapter.AnimalViewHolder> {

    private final Context context;
    private List<Animal> animalesList;
    private final OnAnimalClickListener listener;

    // --- CONSTRUCTOR 1: El que ya tenías (Para Admin / Mis Animales) ---
    public AnimalAdapter(Context context, OnAnimalClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.animalesList = new ArrayList<>(); // Inicializar para evitar crash
    }

    // --- CONSTRUCTOR 2: NUEVO (Para GalleryActivity) ---
    // Este es el que arregla el error en GalleryActivity porque recibe la lista
    public AnimalAdapter(Context context, List<Animal> animalesList, OnAnimalClickListener listener) {
        this.context = context;
        this.animalesList = animalesList;
        this.listener = listener;
    }

    public void setAnimalesList(List<Animal> animalesList) {
        this.animalesList = animalesList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AnimalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Asegúrate de usar el layout correcto. Si es galería pública, quizás quieras ocultar los puntos de estado en el XML.
        View view = LayoutInflater.from(context).inflate(R.layout.item_animal_card, parent, false);
        return new AnimalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnimalViewHolder holder, int position) {
        Animal animal = animalesList.get(position);

        // --- LÓGICA ORIGINAL (SIN CAMBIOS) ---

        // Cargar imagen
        if (animal.getFotoUrl() != null) {
            Glide.with(context)
                    .load(animal.getFotoUrl())
                    .placeholder(R.drawable.ic_pet_placeholder)
                    .error(R.drawable.ic_pet_error)
                    .into(holder.ivAnimalPhoto);
        } else if (animal.getFotosUrls() != null && !animal.getFotosUrls().isEmpty()) {
            // Soporte extra por si usas la lista de URLs
            Glide.with(context)
                    .load(animal.getFotosUrls().get(0))
                    .placeholder(R.drawable.ic_pet_placeholder)
                    .into(holder.ivAnimalPhoto);
        }

        // Mostrar ID Numérico como #0001
        String animalIdDisplay = String.format("#%04d", animal.getIdNumerico());
        holder.tvAnimalName.setText(String.format("%s %s", animal.getNombre(), animalIdDisplay));

        holder.tvAnimalDetails.setText(String.format("%s • %s", animal.getEspecie(), animal.getRaza()));
        holder.tvAnimalStatus.setText(animal.getEstadoRefugio());

        // Lógica de puntos de estado (Admin)
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

        // Colores de la Etiqueta de Estado
        int colorRes = R.color.status_rescued; // Default
        if (estado.equals("Adoptado")) {
            colorRes = R.color.status_adopted;
        } else if (estado.equals("Disponible Adopcion") || estado.equals("En Proceso Adopción")) {
            colorRes = R.color.status_available;
        }

        // Usamos try-catch o verificación simple por si el color no existe aún
        try {
            holder.tvAnimalStatus.setBackgroundColor(ContextCompat.getColor(context, colorRes));
        } catch (Exception e) {
            // Color no encontrado, ignorar
        }

        // Click en la tarjeta
        holder.cardView.setOnClickListener(v -> listener.onAnimalClick(animal));

        // --- NUEVA LÓGICA (Para soportar Favoritos en Galería) ---
        // Si el botón de favorito existe en el XML, le damos funcionalidad
        if (holder.btnFavorite != null) {
            // Cambiar icono según estado (requiere que agregues isFavorited() a tu modelo Animal)
            if (animal.isFavorited()) {
                holder.btnFavorite.setImageResource(R.drawable.ic_favorite_filled);
            } else {
                holder.btnFavorite.setImageResource(R.drawable.ic_favorite_border);
            }

            holder.btnFavorite.setOnClickListener(v -> listener.onFavoriteClick(animal));
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
        final ImageButton btnFavorite; // Nuevo campo opcional

        public AnimalViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            ivAnimalPhoto = itemView.findViewById(R.id.iv_animal_photo);
            tvAnimalName = itemView.findViewById(R.id.tv_animal_name);
            tvAnimalDetails = itemView.findViewById(R.id.tv_animal_details);
            tvAnimalStatus = itemView.findViewById(R.id.tv_animal_status);
            dotStatusMain = itemView.findViewById(R.id.dot_status_main);
            dotStatusMedication = itemView.findViewById(R.id.dot_status_medication);

            // Intentamos buscar el botón de favorito. Si no existe en el XML (modo Admin), será null y no pasará nada.
            // Asegúrate de que en tu XML tengas un ImageButton con id btnFavorite si quieres que funcione.
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }

    // Interfaz actualizada para soportar ambos casos
    public interface OnAnimalClickListener {
        void onAnimalClick(Animal animal);

        // Método nuevo necesario para GalleryActivity
        // (En tu Activity de Admin puedes dejar este método vacío al implementarlo)
        default void onFavoriteClick(Animal animal) {
            // Default vacío para no obligar a implementarlo si no se usa
        }
    }
}