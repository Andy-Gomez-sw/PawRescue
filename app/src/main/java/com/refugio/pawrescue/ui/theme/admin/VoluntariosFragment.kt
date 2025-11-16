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
// --- MODIFICACIÓN AQUÍ ---
// Importa el Repositorio y la Factory que creaste
import com.refugio.pawrescue.data.model.repository.VoluntariosRepository

class VoluntariosFragment : Fragment() {

    private var _binding: FragmentVoluntariosBinding? = null
    private val binding get() = _binding!!

    // --- MODIFICACIÓN AQUÍ ---
    // Esta es la línea que corregimos para usar la "Factory"
    private val viewModel: VoluntariosViewModel by viewModels {
        VoluntariosViewModelFactory(VoluntariosRepository())
    }
    // --- FIN DE LA MODIFICACIÓN ---

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
            voluntariosAdapter.notifyDataSetChanged() // Puedes cambiar esto por updateList si prefieres
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
        } else {
            // Opcional: Mostrar un error si no hay refugioId
            Toast.makeText(requireContext(), "ID de Refugio no encontrado.", Toast.LENGTH_LONG).show()
            updateEmptyState() // Asegúrate de mostrar el estado vacío
        }
    }

    private fun filtrarVoluntarios(activo: Boolean?) {
        val refugioId = prefs.getString(Constants.KEY_REFUGIO_ID, "") ?: ""
        if (refugioId.isEmpty()) return // No hacer nada si no hay refugio

        if (activo == null) {
            viewModel.cargarVoluntarios(refugioId)
        } else if (activo) {
            viewModel.cargarVoluntariosActivos(refugioId)
        } else {
            // Este filtro se hace en el ViewModel para ser más limpio,
            // pero tu lógica de filtrado en el cliente también es válida.
            // Para mantener tu lógica:
            viewModel.voluntarios.value?.let {
                val filtrados = it.filter { !it.activo }
                voluntariosList.clear()
                voluntariosList.addAll(filtrados)
                voluntariosAdapter.notifyDataSetChanged()
                updateEmptyState()
                updateStats()
            }
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
        // Asumiendo que tienes un EditarVoluntarioDialog
        // val dialog = EditarVoluntarioDialog.newInstance(voluntario)
        // dialog.setOnVoluntarioEditedListener {
        //     loadVoluntarios()
        // }
        // dialog.show(childFragmentManager, "EditarVoluntarioDialog")
        Toast.makeText(requireContext(), "Función Editar no implementada", Toast.LENGTH_SHORT).show()
    }

    private fun toggleActivo(voluntario: Usuario) {
        val nuevoEstado = !voluntario.activo
        viewModel.toggleEstadoVoluntario(voluntario.id, nuevoEstado)

        val mensaje = if (nuevoEstado) "Voluntario activado" else "Voluntario desactivado"
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
    }

    private fun verDetalles(voluntario: Usuario) {
        // Asumiendo que tienes un DetalleVoluntarioDialog
        // val dialog = DetalleVoluntarioDialog.newInstance(voluntario)
        // dialog.show(childFragmentManager, "DetalleVoluntarioDialog")
        Toast.makeText(requireContext(), "Ver detalles de ${voluntario.nombre}", Toast.LENGTH_SHORT).show()
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