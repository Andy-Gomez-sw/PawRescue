package com.refugio.pawrescue.ui.theme.animales.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.refugio.pawrescue.R
import com.refugio.pawrescue.data.model.Animal
import com.refugio.pawrescue.databinding.ItemAnimalCardBinding
import com.refugio.pawrescue.ui.theme.utils.Constants

class AnimalesAdapter(
    private val onAnimalClick: (Animal) -> Unit
) : ListAdapter<Animal, AnimalesAdapter.AnimalViewHolder>(AnimalDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimalViewHolder {
        val binding = ItemAnimalCardBinding.inflate(
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
        private val binding: ItemAnimalCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(animal: Animal) {
            binding.apply {
                tvAnimalName.text = "${animal.nombre} - #${animal.id.take(4).uppercase()}"
                tvAnimalBreed.text = "${animal.raza}, ${getAgeText(animal.edad)}"

                // Set emoji based on species
                tvAnimalEmoji.text = when (animal.especie) {
                    Constants.TIPO_PERRO -> "🐕"
                    Constants.TIPO_GATO -> "🐈"
                    Constants.TIPO_AVE -> "🐦"
                    else -> "🐾"
                }

                // Load photo if available
                if (animal.fotosPrincipales.isNotEmpty()) {
                    Glide.with(itemView.context)
                        .load(animal.fotosPrincipales.first())
                        .placeholder(R.drawable.bg_gradient_green)
                        .into(ivAnimalPhoto)
                    tvAnimalEmoji.visibility = View.GONE
                } else {
                    ivAnimalPhoto.setImageResource(R.drawable.bg_gradient_green)
                    tvAnimalEmoji.visibility = View.VISIBLE
                }

                // TODO: Set status badges based on care schedule
                tvStatus1.text = "⏰ Pendiente: Comida"

                root.setOnClickListener {
                    onAnimalClick(animal)
                }
            }
        }

        private fun getAgeText(edad: String): String {
            return when (edad) {
                Constants.EDAD_CACHORRO -> "Cachorro"
                Constants.EDAD_ADULTO -> "Adulto"
                Constants.EDAD_VEJEZ -> "Mayor"
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