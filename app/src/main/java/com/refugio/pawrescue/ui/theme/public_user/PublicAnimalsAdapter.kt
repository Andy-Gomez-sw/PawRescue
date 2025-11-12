package com.refugio.pawrescue.ui.theme.public_user

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.refugio.pawrescue.R
import com.refugio.pawrescue.data.model.Animal
import com.refugio.pawrescue.databinding.ItemPublicAnimalBinding

class PublicAnimalsAdapter(
    private val onAnimalClick: (Animal) -> Unit
) : ListAdapter<Animal, PublicAnimalsAdapter.AnimalViewHolder>(AnimalDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimalViewHolder {
        val binding = ItemPublicAnimalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AnimalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnimalViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AnimalViewHolder(
        private val binding: ItemPublicAnimalBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(animal: Animal) {
            binding.apply {
                tvNombre.text = animal.nombre
                tvInfo.text = "${animal.raza} • ${getEdadText(animal.edad)}"

                // Características
                val caracteristicas = mutableListOf<String>()
                if (animal.vacunasCompletas) caracteristicas.add("✓ Vacunado")
                if (animal.esterilizado) caracteristicas.add("✓ Esterilizado")
                if (animal.desparasitado) caracteristicas.add("✓ Desparasitado")
                tvCaracteristicas.text = caracteristicas.joinToString(" • ")

                // Emoji según especie
                tvEmoji.text = when (animal.especie) {
                    "perro" -> "🐕"
                    "gato" -> "🐈"
                    "ave" -> "🐦"
                    else -> "🐾"
                }

                // Cargar foto si existe
                if (animal.fotosPrincipales.isNotEmpty()) {
                    Glide.with(itemView.context)
                        .load(animal.fotosPrincipales.first())
                        .placeholder(R.drawable.bg_gradient_green)
                        .into(ivAnimalPhoto)
                    tvEmoji.visibility = android.view.View.GONE
                } else {
                    ivAnimalPhoto.setImageResource(R.drawable.bg_gradient_green)
                    tvEmoji.visibility = android.view.View.VISIBLE
                }

                root.setOnClickListener { onAnimalClick(animal) }
            }
        }

        private fun getEdadText(edad: String): String {
            return when (edad) {
                "cachorro" -> "Cachorro"
                "adulto" -> "Adulto"
                "vejez" -> "Mayor"
                else -> edad
            }
        }
    }

    private class AnimalDiffCallback : DiffUtil.ItemCallback<Animal>() {
        override fun areItemsTheSame(oldItem: Animal, newItem: Animal): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Animal, newItem: Animal): Boolean {
            return oldItem == newItem
        }
    }
}