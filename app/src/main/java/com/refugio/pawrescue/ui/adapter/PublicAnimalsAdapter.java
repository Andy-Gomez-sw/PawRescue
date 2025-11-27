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
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.Animal;
import java.util.List;

public class PublicAnimalsAdapter extends RecyclerView.Adapter<PublicAnimalsAdapter.ViewHolder> {

    private Context context;
    private List<Animal> animalList;
    private OnAnimalClickListener listener;

    public interface OnAnimalClickListener {
        void onAnimalClick(Animal animal);
    }

    public PublicAnimalsAdapter(Context context, List<Animal> animalList, OnAnimalClickListener listener) {
        this.context = context;
        this.animalList = animalList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Vinculamos con tu diseño XML item_animal_public
        View view = LayoutInflater.from(context).inflate(R.layout.item_animal_public, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Animal animal = animalList.get(position);

        // Llenar datos de texto
        holder.tvNombre.setText(animal.getNombre());
        holder.tvRaza.setText(animal.getRaza());
        holder.tvEdad.setText(animal.getEdadTexto());
        holder.tvSexo.setText(animal.getSexo());

        // Configurar Estado
        String estado = animal.getEstadoRefugio();
        if (estado != null) {
            holder.tvEstado.setText(estado);
            holder.tvEstado.setVisibility(View.VISIBLE);

            // Lógica simple para cambiar el fondo del badge según disponibilidad
            if (estado.equalsIgnoreCase("Disponible Adopcion") || estado.equalsIgnoreCase("Disponible")) {
                holder.tvEstado.setBackgroundResource(R.drawable.bg_badge_available);
            } else {
                holder.tvEstado.setBackgroundResource(R.drawable.bg_badge_warning); // O el que prefieras para no disponibles
            }
        } else {
            holder.tvEstado.setVisibility(View.GONE);
        }

        // Lógica robusta para la IMAGEN (igual que en el detalle)
        String imageUrl = null;
        if (animal.getFotosUrls() != null && !animal.getFotosUrls().isEmpty()) {
            imageUrl = animal.getFotosUrls().get(0); // Usar la primera de la galería
        } else if (animal.getFotoUrl() != null && !animal.getFotoUrl().isEmpty()) {
            imageUrl = animal.getFotoUrl(); // Usar foto de perfil
        }

        if (imageUrl != null) {
            Glide.with(context)
                    .load(imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_pet_placeholder)
                    .into(holder.ivAnimalPhoto);
        } else {
            holder.ivAnimalPhoto.setImageResource(R.drawable.ic_pet_placeholder);
        }

        // Click Listener
        holder.itemView.setOnClickListener(v -> listener.onAnimalClick(animal));
    }

    @Override
    public int getItemCount() {
        return animalList != null ? animalList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAnimalPhoto;
        TextView tvNombre, tvRaza, tvEdad, tvSexo, tvEstado;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAnimalPhoto = itemView.findViewById(R.id.ivAnimalPhoto);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvRaza = itemView.findViewById(R.id.tvRaza);
            tvEdad = itemView.findViewById(R.id.tvEdad);
            tvSexo = itemView.findViewById(R.id.tvSexo);
            tvEstado = itemView.findViewById(R.id.tvEstado);
        }
    }
}
