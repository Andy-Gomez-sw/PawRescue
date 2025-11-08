package com.refugio.pawrescue.ui.theme.animales.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.refugio.pawrescue.databinding.ItemCuidadoBinding
import com.refugio.pawrescue.data.model.Cuidado
import com.refugio.pawrescue.ui.theme.utils.DateUtils

class CuidadosAdapter(
    private val cuidados: MutableList<Cuidado>,
    private val onEditClick: (Cuidado) -> Unit,
    private val onDeleteClick: (Cuidado) -> Unit
) : RecyclerView.Adapter<CuidadosAdapter.CuidadoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CuidadoViewHolder {
        val binding = ItemCuidadoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CuidadoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CuidadoViewHolder, position: Int) {
        holder.bind(cuidados[position])
    }

    override fun getItemCount(): Int = cuidados.size

    inner class CuidadoViewHolder(
        private val binding: ItemCuidadoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cuidado: Cuidado) {
            binding.apply {
                // Emoji según tipo
                tvCuidadoIcon.text = when (cuidado.tipo) {
                    "Alimentación" -> "🍖"
                    "Baño" -> "🛁"
                    "Paseo" -> "🚶"
                    "Medicación" -> "💊"
                    else -> "📋"
                }

                tvCuidadoTipo.text = cuidado.tipo
                tvCuidadoDescripcion.text = cuidado.descripcion
                tvCuidadoFecha.text = DateUtils.formatDateTime(cuidado.horaProgramada)
                tvCuidadoResponsable.text = "Por: ${cuidado.voluntarioNombre}"

                // Estado
                if (cuidado.completado) {
                    tvCuidadoEstado.text = "✅ Completado"
                    tvCuidadoEstado.setTextColor(
                        itemView.context.getColor(android.R.color.holo_green_dark)
                    )
                } else {
                    tvCuidadoEstado.text = "⏰ Pendiente"
                    tvCuidadoEstado.setTextColor(
                        itemView.context.getColor(android.R.color.holo_orange_dark)
                    )
                }

                btnEdit.setOnClickListener { onEditClick(cuidado) }
                btnDelete.setOnClickListener { onDeleteClick(cuidado) }
            }
        }
    }
}