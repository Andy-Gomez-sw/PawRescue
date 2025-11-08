package com.refugio.pawrescue.ui.theme.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.refugio.pawrescue.databinding.FragmentDonacionesBinding
import java.text.NumberFormat
import java.util.*

class DonacionesFragment : Fragment() {

    private var _binding: FragmentDonacionesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DonacionesViewModel by viewModels()
    private lateinit var transaccionesAdapter: TransaccionesAdapter
    private val transaccionesList = mutableListOf<Transaccion>()

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
        observeViewModel()
        loadTransacciones()
    }

    private fun setupRecyclerView() {
        transaccionesAdapter = TransaccionesAdapter(
            transacciones = transaccionesList,
            onEditClick = { transaccion -> editarTransaccion(transaccion) },
            onDeleteClick = { transaccion -> eliminarTransaccion(transaccion) }
        )

        binding.rvTransacciones.apply { // <-- AQUÍ PUEDE ESTAR EL ERROR SI EL ID NO ES CORRECTO EN EL XML
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

    private fun observeViewModel() {
        viewModel.transacciones.observe(viewLifecycleOwner) { transacciones ->
            transaccionesList.clear()
            transaccionesList.addAll(transacciones)
            transaccionesAdapter.notifyDataSetChanged()
            updateEmptyState()
        }

        viewModel.balance.observe(viewLifecycleOwner) { balance ->
            updateStats(balance)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), "Error: $it", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadTransacciones() {
        viewModel.cargarTransacciones()
    }

    private fun mostrarDialogoNuevaTransaccion(tipo: String) {
        val dialog = NuevaTransaccionDialog.newInstance(tipo)
        dialog.setOnTransaccionCreatedListener {
            Toast.makeText(requireContext(), "Transacción guardada", Toast.LENGTH_SHORT).show()
            loadTransacciones()
        }
        dialog.show(childFragmentManager, "NuevaTransaccionDialog")
    }

    private fun updateStats(balance: Map<String, Double>) {
        val formatter = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

        val ingresos = balance["ingresos"] ?: 0.0
        val egresos = balance["egresos"] ?: 0.0
        val balanceTotal = balance["balance"] ?: 0.0

        binding.tvIngresos.text = formatter.format(ingresos)
        binding.tvEgresos.text = formatter.format(egresos)
        binding.tvBalance.text = formatter.format(balanceTotal)

        binding.tvBalance.setTextColor(
            if (balanceTotal >= 0)
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
        viewModel.eliminarTransaccion(transaccion.id)
        Toast.makeText(requireContext(), "Transacción eliminada", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}