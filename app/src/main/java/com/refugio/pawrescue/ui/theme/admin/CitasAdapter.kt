package com.refugio.pawrescue.ui.theme.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.refugio.pawrescue.databinding.ItemCitaBinding
import com.refugio.pawrescue.data.model.SolicitudAdopcion
import java.text.SimpleDateFormat
import java.util.*

class CitasAdapter(
    private val citas: List<SolicitudAdopcion>,
    private val onAprobarClick: (SolicitudAdopcion) -> Unit,
    private val onRechazarClick: (SolicitudAdopcion) -> Unit,
    private val onVerDetallesClick: (SolicitudAdopcion) -> Unit
) : RecyclerView.Adapter<CitasAdapter.CitaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val binding = ItemCitaBinding.inflate(
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
        private val binding: ItemCitaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cita: SolicitudAdopcion) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            binding.apply {
                tvSolicitante.text = cita.solicitanteNombre
                tvAnimal.text = "Animal: ${cita.animalNombre}"
                tvFecha.text = "Solicitado: ${cita.fechaSolicitud?.let { dateFormat.format(it) } ?: "Sin fecha"}"
                tvTelefono.text = "📞 ${cita.solicitanteTelefono}"
                tvPuntuacion.text = "⭐ Puntuación: ${cita.puntuacionAutomatica}/100"

                // Estado
                when (cita.estado) {
                    "pendiente" -> {
                        tvEstado.text = "⏰ Pendiente"
                        tvEstado.setBackgroundColor(
                            itemView.context.getColor(android.R.color.holo_orange_light)
                        )
                        btnAprobar.visibility = android.view.View.VISIBLE
                        btnRechazar.visibility = android.view.View.VISIBLE
                    }
                    "aprobada" -> {
                        tvEstado.text = "✅ Aprobada"
                        tvEstado.setBackgroundColor(
                            itemView.context.getColor(android.R.color.holo_green_light)
                        )
                        btnAprobar.visibility = android.view.View.GONE
                        btnRechazar.visibility = android.view.View.GONE
                    }
                    "rechazada" -> {
                        tvEstado.text = "❌ Rechazada"
                        tvEstado.setBackgroundColor(
                            itemView.context.getColor(android.R.color.holo_red_light)
                        )
                        btnAprobar.visibility = android.view.View.GONE
                        btnRechazar.visibility = android.view.View.GONE
                    }
                }

                btnAprobar.setOnClickListener { onAprobarClick(cita) }
                btnRechazar.setOnClickListener { onRechazarClick(cita) }
                root.setOnClickListener { onVerDetallesClick(cita) }
            }
        }
    }
}