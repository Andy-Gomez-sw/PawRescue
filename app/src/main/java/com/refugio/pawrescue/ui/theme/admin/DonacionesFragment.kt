package com.refugio.pawrescue.ui.theme.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.refugio.pawrescue.databinding.FragmentDonacionesBinding
import java.text.NumberFormat
import java.util.*

class DonacionesFragment : Fragment() {

    private var _binding: FragmentDonacionesBinding? = null
    private val binding get() = _binding!!

    private lateinit var transaccionesAdapter: TransaccionesAdapter
    private val transaccionesList = mutableListOf<Transaccion>()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDonacionesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupButtons()
        loadTransaccionesFromFirebase()
    }

    private fun setupRecyclerView() {
        transaccionesAdapter = TransaccionesAdapter(
            transacciones = transaccionesList,
            onEditClick = { transaccion -> editarTransaccion(transaccion) },
            onDeleteClick = { transaccion -> eliminarTransaccion(transaccion) }
        )

        binding.rvTransacciones.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transaccionesAdapter
        }
    }

    private fun setupButtons() {
        binding.btnNuevaDonacion.setOnClickListener {
            mostrarDialogoNuevaTransaccion("ingreso")
        }

        binding.btnNuevoGasto.setOnClickListener {
            mostrarDialogoNuevaTransaccion("egreso")
        }

        binding.btnExportar.setOnClickListener {
            Toast.makeText(requireContext(), "Exportar a Excel (próximamente)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadTransaccionesFromFirebase() {
        binding.progressBar.visibility = View.VISIBLE

        firestore.collection("transacciones")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                binding.progressBar.visibility = View.GONE

                if (error != null) {
                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                transaccionesList.clear()
                snapshot?.documents?.forEach { doc ->
                    val transaccion = doc.toObject(Transaccion::class.java)
                    transaccion?.let { transaccionesList.add(it) }
                }

                transaccionesAdapter.notifyDataSetChanged()
                updateStats()
                updateEmptyState()
            }
    }

    private fun mostrarDialogoNuevaTransaccion(tipo: String) {
        val dialog = NuevaTransaccionDialog.newInstance(tipo)
        dialog.setOnTransaccionCreatedListener {
            Toast.makeText(requireContext(), "Transacción guardada", Toast.LENGTH_SHORT).show()
        }
        dialog.show(childFragmentManager, "NuevaTransaccionDialog")
    }

    private fun updateStats() {
        val ingresos = transaccionesList
            .filter { it.tipo == "ingreso" }
            .sumOf { it.monto }

        val egresos = transaccionesList
            .filter { it.tipo == "egreso" }
            .sumOf { it.monto }

        val balance = ingresos - egresos

        val formatter = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

        binding.tvIngresos.text = formatter.format(ingresos)
        binding.tvEgresos.text = formatter.format(egresos)
        binding.tvBalance.text = formatter.format(balance)

        binding.tvBalance.setTextColor(
            if (balance >= 0)
                requireContext().getColor(android.R.color.holo_green_dark)
            else
                requireContext().getColor(android.R.color.holo_red_dark)
        )
    }

    private fun updateEmptyState() {
        if (transaccionesList.isEmpty()) {
            binding.rvTransacciones.visibility = View.GONE
            binding.llEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvTransacciones.visibility = View.VISIBLE
            binding.llEmptyState.visibility = View.GONE
        }
    }

    private fun editarTransaccion(transaccion: Transaccion) {
        Toast.makeText(requireContext(), "Editar: ${transaccion.concepto}", Toast.LENGTH_SHORT).show()
    }

    private fun eliminarTransaccion(transaccion: Transaccion) {
        firestore.collection("transacciones")
            .document(transaccion.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Transacción eliminada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class Transaccion(
    val id: String = "",
    val tipo: String = "", // "ingreso" o "egreso"
    val concepto: String = "",
    val monto: Double = 0.0,
    val fecha: Date? = null,
    val categoria: String = "",
    val descripcion: String = ""
)