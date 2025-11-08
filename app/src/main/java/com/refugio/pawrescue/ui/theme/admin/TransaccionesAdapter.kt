package com.refugio.pawrescue.ui.theme.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.refugio.pawrescue.databinding.ItemTransaccionBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class TransaccionesAdapter(
    private val transacciones: List<Transaccion>,
    private val onEditClick: (Transaccion) -> Unit,
    private val onDeleteClick: (Transaccion) -> Unit
) : RecyclerView.Adapter<TransaccionesAdapter.TransaccionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransaccionViewHolder {
        val binding = ItemTransaccionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransaccionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransaccionViewHolder, position: Int) {
        holder.bind(transacciones[position])
    }

    override fun getItemCount() = transacciones.size

    inner class TransaccionViewHolder(
        private val binding: ItemTransaccionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transaccion: Transaccion) {
            val formatter = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            binding.apply {
                tvIcono.text = if (transaccion.tipo == "ingreso") "💰" else "💸"

                tvConcepto.text = transaccion.concepto
                tvCategoria.text = transaccion.categoria
                tvFecha.text = transaccion.fecha?.let { dateFormat.format(it) } ?: "Sin fecha"

                val montoFormateado = formatter.format(transaccion.monto)
                tvMonto.text = if (transaccion.tipo == "ingreso") "+$montoFormateado" else "-$montoFormateado"

                tvMonto.setTextColor(
                    if (transaccion.tipo == "ingreso")
                        itemView.context.getColor(android.R.color.holo_green_dark)
                    else
                        itemView.context.getColor(android.R.color.holo_red_dark)
                )

                btnEdit.setOnClickListener { onEditClick(transaccion) }
                btnDelete.setOnClickListener { onDeleteClick(transaccion) }
            }
        }
    }
}