package com.refugio.pawrescue.ui.theme.public_user

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.refugio.pawrescue.R
import com.refugio.pawrescue.data.model.EstadoSolicitud
import com.refugio.pawrescue.data.model.SolicitudAdopcion
import com.refugio.pawrescue.databinding.ItemMyAdoptionRequestBinding
import java.text.SimpleDateFormat
import java.util.*

class MyAdoptionRequestsAdapter : ListAdapter<SolicitudAdopcion, MyAdoptionRequestsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMyAdoptionRequestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemMyAdoptionRequestBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(solicitud: SolicitudAdopcion) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            binding.apply {
                tvNombreAnimal.text = "Animal ID: ${solicitud.animalId.take(8)}"
                tvFechaSolicitud.text = "Solicitado: ${dateFormat.format(solicitud.fechaSolicitud)}"

                // Estado con chip
                when (solicitud.estado) {
                    EstadoSolicitud.PENDIENTE -> {
                        chipEstado.text = "⏳ Pendiente"
                        chipEstado.setChipBackgroundColorResource(android.R.color.holo_orange_light)
                        tvMensaje.text = "El refugio revisará tu solicitud pronto"
                        tvMensaje.visibility = android.view.View.VISIBLE
                    }
                    EstadoSolicitud.APROBADA -> {
                        chipEstado.text = "✅ Aprobada"
                        chipEstado.setChipBackgroundColorResource(android.R.color.holo_green_light)
                        tvMensaje.text = "¡Felicidades! Tu solicitud fue aprobada"
                        tvMensaje.visibility = android.view.View.VISIBLE
                    }
                    EstadoSolicitud.RECHAZADA -> {
                        chipEstado.text = "❌ Rechazada"
                        chipEstado.setChipBackgroundColorResource(android.R.color.holo_red_light)
                        tvMensaje.text = "Lo sentimos, tu solicitud no fue aprobada"
                        tvMensaje.visibility = android.view.View.VISIBLE
                    }
                    EstadoSolicitud.ENTREVISTA_PROGRAMADA -> {
                        chipEstado.text = "📅 Entrevista"
                        chipEstado.setChipBackgroundColorResource(android.R.color.holo_blue_light)
                        tvMensaje.text = "Se programó una entrevista contigo"
                        tvMensaje.visibility = android.view.View.VISIBLE
                    }
                }

                // Emoji por defecto
                tvEmoji.text = "🐾"
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<SolicitudAdopcion>() {
        override fun areItemsTheSame(oldItem: SolicitudAdopcion, newItem: SolicitudAdopcion): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SolicitudAdopcion, newItem: SolicitudAdopcion): Boolean {
            return oldItem == newItem
        }
    }
}