package com.refugio.pawrescue.ui.theme.admin

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.refugio.pawrescue.R
import com.refugio.pawrescue.data.model.EstadoSolicitud
import com.refugio.pawrescue.data.model.SolicitudAdopcion
import com.refugio.pawrescue.databinding.ItemSolicitudAdopcionBinding
import java.text.SimpleDateFormat
import java.util.Locale

class SolicitudesPendientesAdapter(
    private val onItemClick: (SolicitudAdopcion) -> Unit
) : ListAdapter<SolicitudAdopcion, SolicitudesPendientesAdapter.SolicitudViewHolder>(SolicitudDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SolicitudViewHolder {
        val binding = ItemSolicitudAdopcionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SolicitudViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SolicitudViewHolder, position: Int) {
        val solicitud = getItem(position)
        holder.bind(solicitud)
    }

    inner class SolicitudViewHolder(private val binding: ItemSolicitudAdopcionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        fun bind(solicitud: SolicitudAdopcion) {
            // IDs de tu XML: tvNombreSolicitante, tvNombreAnimal, tvFechaSolicitud, chipEstado
            binding.tvNombreSolicitante.text = solicitud.solicitanteNombre
            binding.tvNombreAnimal.text = "Animal: ${solicitud.animalNombre}"

            solicitud.fechaSolicitud?.let {
                binding.tvFechaSolicitud.text = "Solicitado el: ${dateFormatter.format(it)}"
            } ?: run {
                binding.tvFechaSolicitud.text = "Fecha no disponible"
            }

            // --- ESTA ES LA PARTE CORREGIDA ---
            // Usamos chipEstado y los colores de tu colors.xml
            binding.chipEstado.text = solicitud.estado.name

            val context = binding.root.context

            // Mapeamos los estados a los colores de tus "Badges"
            val (colorRes, textColorRes) = when (solicitud.estado) {
                EstadoSolicitud.PENDIENTE -> R.color.badge_pending_bg to R.color.badge_pending_text
                EstadoSolicitud.APROBADA -> R.color.badge_complete_bg to R.color.badge_complete_text
                EstadoSolicitud.RECHAZADA -> R.color.badge_urgent_bg to R.color.badge_urgent_text
                EstadoSolicitud.ENTREVISTA_PROGRAMADA -> R.color.badge_treatment_bg to R.color.badge_treatment_text // Usamos 'treatment' para 'info'
            }

            binding.chipEstado.chipBackgroundColor = ColorStateList.valueOf(
                ContextCompat.getColor(context, colorRes)
            )
            binding.chipEstado.setTextColor(
                ContextCompat.getColor(context, textColorRes)
            )
            // --- FIN DE LA CORRECCIÓN ---


            binding.root.setOnClickListener {
                onItemClick(solicitud)
            }
        }
    }

    class SolicitudDiffCallback : DiffUtil.ItemCallback<SolicitudAdopcion>() {
        override fun areItemsTheSame(oldItem: SolicitudAdopcion, newItem: SolicitudAdopcion): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SolicitudAdopcion, newItem: SolicitudAdopcion): Boolean {
            return oldItem == newItem
        }
    }
}