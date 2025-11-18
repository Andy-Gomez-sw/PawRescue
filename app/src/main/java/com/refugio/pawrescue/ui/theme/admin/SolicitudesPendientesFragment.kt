// Archivo: SolicitudesPendientesFragment.kt
package com.refugio.pawrescue.ui.theme.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
// --- CORRECCIÓN 1: Ruta de importación correcta para el Repositorio ---
import com.refugio.pawrescue.data.model.repository.AdopcionRepository
import com.refugio.pawrescue.databinding.FragmentSolicitudesPendientesBinding

class SolicitudesPendientesFragment : Fragment() {

    private var _binding: FragmentSolicitudesPendientesBinding? = null
    private val binding get() = _binding!!

    // Usamos el ViewModel y el Factory que ya analizamos
    private val viewModel: SolicitudesAdopcionViewModel by viewModels {
        SolicitudesViewModelFactory(AdopcionRepository())
    }

    private lateinit var solicitudesAdapter: SolicitudesPendientesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Usamos el binding del nuevo archivo XML
        _binding = FragmentSolicitudesPendientesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        // 1. Cargar los datos iniciales
        viewModel.cargarSolicitudes()

        // 2. Configurar el Swipe-to-Refresh para recargar
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.cargarSolicitudes()
        }
    }

    private fun setupRecyclerView() {
        solicitudesAdapter = SolicitudesPendientesAdapter { solicitudSeleccionada ->
            // 1. Notificas al ViewModel cuál es la solicitud activa.
            viewModel.seleccionarSolicitud(solicitudSeleccionada)

            // --- CORRECCIÓN 2: Llamada correcta al diálogo ---
            // Simplemente creas y muestras el diálogo. No necesita argumentos
            // porque obtiene los datos desde el ViewModel compartido.
            GestionarSolicitudDialog().show(parentFragmentManager, "GestionarSolicitudDialog")
        }

        binding.recyclerViewSolicitudes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = solicitudesAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.solicitudes.observe(viewLifecycleOwner) { solicitudes ->
            solicitudesAdapter.submitList(solicitudes)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            if (!errorMsg.isNullOrEmpty()) {
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
