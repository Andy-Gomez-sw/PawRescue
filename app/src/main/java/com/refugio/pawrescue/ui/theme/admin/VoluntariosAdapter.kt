package com.refugio.pawrescue.ui.theme.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.refugio.pawrescue.databinding.ItemVoluntarioBinding
import com.refugio.pawrescue.data.model.Usuario
import java.text.SimpleDateFormat
import java.util.*

class VoluntariosAdapter(
    private var voluntarios: List<Usuario>,
    private val onEditClick: (Usuario) -> Unit,
    private val onToggleActivoClick: (Usuario) -> Unit,
    private val onVerDetallesClick: (Usuario) -> Unit
) : RecyclerView.Adapter<VoluntariosAdapter.VoluntarioViewHolder>() {

    fun updateList(newList: List<Usuario>) {
        voluntarios = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoluntarioViewHolder {
        val binding = ItemVoluntarioBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VoluntarioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VoluntarioViewHolder, position: Int) {
        holder.bind(voluntarios[position])
    }

    override fun getItemCount() = voluntarios.size

    inner class VoluntarioViewHolder(
        private val binding: ItemVoluntarioBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(voluntario: Usuario) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            binding.apply {
                // Avatar con inicial
                tvInicial.text = voluntario.nombre.firstOrNull()?.toString()?.uppercase() ?: "V"

                tvNombre.text = voluntario.nombre
                tvEmail.text = voluntario.email
                tvTelefono.text = "📞 ${voluntario.telefono}"
                tvRol.text = getRolEmoji(voluntario.rol) + " " + voluntario.rol.capitalize()

                // Animales asignados
                val numAnimales = voluntario.animalesAsignados.size
                tvAnimalesAsignados.text = "$numAnimales ${if (numAnimales == 1) "animal" else "animales"} asignados"

                // Fecha de registro
                tvFechaRegistro.text = "Desde: ${voluntario.fechaRegistro?.let { dateFormat.format(it) } ?: "N/A"}"

                // Estado activo/inactivo
                if (voluntario.activo) {
                    tvEstado.text = "✅ Activo"
                    tvEstado.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
                    btnToggleActivo.text = "Desactivar"
                } else {
                    tvEstado.text = "❌ Inactivo"
                    tvEstado.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
                    btnToggleActivo.text = "Activar"
                }

                // Turno
                if (voluntario.turno.isNotEmpty()) {
                    tvTurno.visibility = android.view.View.VISIBLE
                    tvTurno.text = "🕐 Turno: ${voluntario.turno}"
                } else {
                    tvTurno.visibility = android.view.View.GONE
                }

                btnEdit.setOnClickListener { onEditClick(voluntario) }
                btnToggleActivo.setOnClickListener { onToggleActivoClick(voluntario) }
                root.setOnClickListener { onVerDetallesClick(voluntario) }
            }
        }

        private fun getRolEmoji(rol: String): String {
            return when (rol.lowercase()) {
                "admin" -> "👑"
                "coordinador" -> "⭐"
                "voluntario" -> "🤝"
                else -> "👤"
            }
        }
    }
}