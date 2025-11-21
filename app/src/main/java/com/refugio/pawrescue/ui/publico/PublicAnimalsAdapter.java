package com.refugio.pawrescue.ui.publico;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.Animal;
import com.refugio.pawrescue.databinding.ItemAnimalPublicBinding;

public class PublicAnimalsAdapter extends ListAdapter<Animal, PublicAnimalsAdapter.AnimalViewHolder> {

    private final OnAnimalClickListener listener;

    public interface OnAnimalClickListener {
        void onAnimalClick(Animal animal);
    }

    public PublicAnimalsAdapter(OnAnimalClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Animal> DIFF_CALLBACK = new DiffUtil.ItemCallback<Animal>() {
        @Override
        public boolean areItemsTheSame(@NonNull Animal oldItem, @NonNull Animal newItem) {
            return oldItem.getIdAnimal() != null && oldItem.getIdAnimal().equals(newItem.getIdAnimal());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Animal oldItem, @NonNull Animal newItem) {
            return oldItem.getNombre().equals(newItem.getNombre()) &&
                    oldItem.getRaza().equals(newItem.getRaza()) &&
                    oldItem.getEstadoRefugio().equals(newItem.getEstadoRefugio());
        }
    };

    @NonNull
    @Override
    public AnimalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAnimalPublicBinding binding = ItemAnimalPublicBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new AnimalViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AnimalViewHolder holder, int position) {
        Animal animal = getItem(position);
        holder.bind(animal);
    }

    class AnimalViewHolder extends RecyclerView.ViewHolder {
        private final ItemAnimalPublicBinding binding;

        public AnimalViewHolder(ItemAnimalPublicBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onAnimalClick(getItem(position));
                }
            });
        }

        public void bind(Animal animal) {
            // Nombre
            binding.tvNombre.setText(animal.getNombre());

            // Raza
            binding.tvRaza.setText(animal.getRaza());

            // Edad aproximada (tu modelo usa edadAprox que es String: "Cachorro", "Adulto")
            String edadTexto = animal.getEdadAprox() != null ? animal.getEdadAprox() : "Edad desconocida";
            binding.tvEdad.setText(edadTexto);

            // Sexo
            binding.tvSexo.setText(animal.getSexo());

            // Estado (tu modelo usa estadoRefugio: "Rescatado", "Disponible Adopcion", "Adoptado")
            if ("Disponible Adopcion".equals(animal.getEstadoRefugio()) ||
                    "Disponible".equals(animal.getEstadoRefugio())) {
                binding.tvEstado.setText("❤️");
                binding.tvEstado.setBackgroundResource(R.drawable.bg_badge_available);
            } else if ("Adoptado".equals(animal.getEstadoRefugio())) {
                binding.tvEstado.setText("✓");
                binding.tvEstado.setBackgroundResource(R.drawable.bg_badge_adopted);
            } else {
                binding.tvEstado.setText("🐾");
                binding.tvEstado.setBackgroundResource(R.drawable.bg_badge_adopted);
            }

            // Cargar imagen con Glide
            if (animal.getFotoUrl() != null && !animal.getFotoUrl().isEmpty()) {
                Glide.with(binding.getRoot().getContext())
                        .load(animal.getFotoUrl())
                        .placeholder(R.drawable.bg_gradient_green)
                        .error(R.drawable.bg_gradient_green)
                        .centerCrop()
                        .into(binding.ivAnimalPhoto);
            } else {
                binding.ivAnimalPhoto.setImageResource(R.drawable.bg_gradient_green);
            }
        }
    }
}