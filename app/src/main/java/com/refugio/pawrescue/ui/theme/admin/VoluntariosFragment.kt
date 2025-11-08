package com.refugio.pawrescue.ui.theme.admin

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.refugio.pawrescue.databinding.FragmentVoluntariosBinding
import com.refugio.pawrescue.data.model.Usuario
import com.refugio.pawrescue.ui.theme.utils.Constants

class VoluntariosFragment : Fragment() {

    private var _binding: FragmentVoluntariosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VoluntariosViewModel by viewModels()
    private lateinit var voluntariosAdapter: VoluntariosAdapter
    private val voluntariosList = mutableListOf<Usuario>()
    private lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVoluntariosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = requireContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

        setupRecyclerView()
        setupButtons()
        observeViewModel()
        loadVoluntarios()
    }

    private fun setupRecyclerView() {
        voluntariosAdapter = VoluntariosAdapter(
            voluntarios = voluntariosList,
            onEditClick = { voluntario -> editarVoluntario(voluntario) },
            onToggleActivoClick = { voluntario -> toggleActivo(voluntario) },
            onVerDetallesClick = { voluntario -> verDetalles(voluntario) }
        )

        binding.rvVoluntarios.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = voluntariosAdapter
        }
    }

    private fun setupButtons() {
        binding.btnNuevoVoluntario.setOnClickListener {
            mostrarDialogoNuevoVoluntario()
        }

        binding.chipGroupFiltro.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val filtro = when (checkedIds[0]) {
                    binding.chipTodos.id -> null
                    binding.chipActivos.id -> true
                    binding.chipInactivos.id -> false
                    else -> null
                }
                filtrarVoluntarios(filtro)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.voluntarios.observe(viewLifecycleOwner) { voluntarios ->
            voluntariosList.clear()
            voluntariosList.addAll(voluntarios)
            voluntariosAdapter.notifyDataSetChanged()
            updateEmptyState()
            updateStats()
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

    private fun loadVoluntarios() {
        val refugioId = prefs.getString(Constants.KEY_REFUGIO_ID, "") ?: ""
        if (refugioId.isNotEmpty()) {
            viewModel.cargarVoluntarios(refugioId)
        }
    }

    private fun filtrarVoluntarios(activo: Boolean?) {
        val refugioId = prefs.getString(Constants.KEY_REFUGIO_ID, "") ?: ""

        if (activo == null) {
            viewModel.cargarVoluntarios(refugioId)
        } else if (activo) {
            viewModel.cargarVoluntariosActivos(refugioId)
        } else {
            // Filtrar inactivos del lado del cliente
            val filtrados = voluntariosList.filter { !it.activo }
            voluntariosAdapter.updateList(filtrados)
        }
    }

    private fun mostrarDialogoNuevoVoluntario() {
        val dialog = NuevoVoluntarioDialog()
        dialog.setOnVoluntarioCreatedListener {
            Toast.makeText(requireContext(), "Voluntario creado exitosamente", Toast.LENGTH_SHORT).show()
            loadVoluntarios()
        }
        dialog.show(childFragmentManager, "NuevoVoluntarioDialog")
    }

    private fun editarVoluntario(voluntario: Usuario) {
        val dialog = EditarVoluntarioDialog.newInstance(voluntario)
        dialog.setOnVoluntarioEditedListener {
            loadVoluntarios()
        }
        dialog.show(childFragmentManager, "EditarVoluntarioDialog")
    }

    private fun toggleActivo(voluntario: Usuario) {
        val nuevoEstado = !voluntario.activo
        viewModel.toggleEstadoVoluntario(voluntario.id, nuevoEstado)

        val mensaje = if (nuevoEstado) "Voluntario activado" else "Voluntario desactivado"
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
    }

    private fun verDetalles(voluntario: Usuario) {
        val dialog = DetalleVoluntarioDialog.newInstance(voluntario)
        dialog.show(childFragmentManager, "DetalleVoluntarioDialog")
    }

    private fun updateEmptyState() {
        if (voluntariosList.isEmpty()) {
            binding.rvVoluntarios.visibility = View.GONE
            binding.llEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvVoluntarios.visibility = View.VISIBLE
            binding.llEmptyState.visibility = View.GONE
        }
    }

    private fun updateStats() {
        val total = voluntariosList.size
        val activos = voluntariosList.count { it.activo }
        val inactivos = total - activos

        binding.tvTotal.text = total.toString()
        binding.tvActivos.text = activos.toString()
        binding.tvInactivos.text = inactivos.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}