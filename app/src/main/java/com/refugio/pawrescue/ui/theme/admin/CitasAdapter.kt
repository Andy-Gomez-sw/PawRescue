package com.refugio.pawrescue.ui.theme.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
// CORRECCIÓN: Usar el Binding correcto generado a partir de item_solicitud_adopcion.xml
import com.refugio.pawrescue.databinding.ItemSolicitudAdopcionBinding
import com.refugio.pawrescue.data.model.EstadoSolicitud
import com.refugio.pawrescue.data.model.SolicitudAdopcion
import java.text.SimpleDateFormat
import java.util.*

class CitasAdapter(
    private var citas: List<SolicitudAdopcion> = emptyList(),
    private val onAprobarClick: (SolicitudAdopcion) -> Unit,
    private val onRechazarClick: (SolicitudAdopcion) -> Unit,
    private val onVerDetallesClick: (SolicitudAdopcion) -> Unit
) : RecyclerView.Adapter<CitasAdapter.CitaViewHolder>() {

    fun submitList(nuevasCitas: List<SolicitudAdopcion>) {
        citas = nuevasCitas
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        // CORRECCIÓN: Inflar el layout correcto
        val binding = ItemSolicitudAdopcionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CitaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        holder.bind(citas[position])
    }

    override fun getItemCount() = citas.size

    inner class CitaViewHolder(
        // CORRECCIÓN: Usar el tipo de Binding correcto
        private val binding: ItemSolicitudAdopcionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cita: SolicitudAdopcion) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            binding.apply {
                tvNombreSolicitante.text = cita.nombreSolicitante
                // Nota: Recuerda que animalId es solo el ID, idealmente deberías buscar el nombre del animal
                tvNombreAnimal.text = "Interesado en ID: ${cita.animalId}"
                tvFechaSolicitud.text = "Solicitado: ${dateFormat.format(cita.fechaSolicitud)}"

                when (cita.estado) {
                    EstadoSolicitud.PENDIENTE -> {
                        chipEstado.text = "Pendiente"
                        chipEstado.setChipBackgroundColorResource(android.R.color.holo_orange_light)
                    }
                    EstadoSolicitud.APROBADA -> {
                        chipEstado.text = "Aprobada"
                        chipEstado.setChipBackgroundColorResource(android.R.color.holo_green_light)
                    }
                    EstadoSolicitud.RECHAZADA -> {
                        chipEstado.text = "Rechazada"
                        chipEstado.setChipBackgroundColorResource(android.R.color.holo_red_light)
                    }
                    EstadoSolicitud.ENTREVISTA_PROGRAMADA -> {
                        chipEstado.text = "Entrevista"
                        chipEstado.setChipBackgroundColorResource(android.R.color.holo_blue_light)
                    }
                }

                root.setOnClickListener { onVerDetallesClick(cita) }
            }
        }
    }
}