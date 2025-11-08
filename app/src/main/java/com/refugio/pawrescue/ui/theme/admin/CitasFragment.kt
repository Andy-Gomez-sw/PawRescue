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
import com.refugio.pawrescue.databinding.FragmentCitasBinding
import com.refugio.pawrescue.data.model.EstadoSolicitud // Importamos el Enum
import com.refugio.pawrescue.data.model.SolicitudAdopcion
import com.refugio.pawrescue.ui.theme.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // Importante si usas Hilt para inyectar el ViewModel
class CitasFragment : Fragment() {

    private var _binding: FragmentCitasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SolicitudesAdopcionViewModel by viewModels() // Usamos el ViewModel correcto
    private lateinit var citasAdapter: CitasAdapter
    private lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCitasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = requireContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

        setupRecyclerView()
        // setupButtons() // Ya no parece haber un botón de "Nueva Cita" en el XML nuevo, pero si lo hay, descomenta esto.
        observeViewModel()
        // loadCitas() // El ViewModel ya debería cargar las citas al iniciarse o al observar.
    }

    private fun setupRecyclerView() {
        citasAdapter = CitasAdapter(
            onAprobarClick = { cita -> aprobarCita(cita) },
            onRechazarClick = { cita -> rechazarCita(cita) },
            onVerDetallesClick = { cita -> verDetalles(cita) }
        )

        binding.rvSolicitudes.apply { // Asegúrate de que el ID en tu XML sea rvSolicitudes o cámbialo aquí
            layoutManager = LinearLayoutManager(requireContext())
            adapter = citasAdapter
        }
    }

    /* Si tienes botones de filtro o agregar nueva cita, configúralos aquí
    private fun setupButtons() {
        binding.btnNuevaCita.setOnClickListener {
            mostrarDialogoNuevaCita()
        }
    }
    */

    private fun observeViewModel() {
        viewModel.solicitudes.observe(viewLifecycleOwner) { solicitudes ->
            citasAdapter.submitList(solicitudes)
            updateEmptyState(solicitudes.isEmpty())
            updateStats(solicitudes)
        }

        // Si tu ViewModel tiene LiveData para loading/error, obsérvalos aquí.
        // Por ejemplo:
        /*
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        */
    }

    private fun aprobarCita(cita: SolicitudAdopcion) {
        viewModel.actualizarEstado(cita.id, EstadoSolicitud.APROBADA)
        Toast.makeText(requireContext(), "Solicitud aprobada", Toast.LENGTH_SHORT).show()
    }

    private fun rechazarCita(cita: SolicitudAdopcion) {
        viewModel.actualizarEstado(cita.id, EstadoSolicitud.RECHAZADA)
        Toast.makeText(requireContext(), "Solicitud rechazada", Toast.LENGTH_SHORT).show()
    }

    private fun verDetalles(cita: SolicitudAdopcion) {
        val dialog = DetalleCitaDialog.newInstance(cita)
        dialog.show(childFragmentManager, "DetalleCitaDialog")
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.rvSolicitudes.visibility = View.GONE
            // binding.llEmptyState.visibility = View.VISIBLE // Asegúrate de tener este view en tu XML si lo usas
        } else {
            binding.rvSolicitudes.visibility = View.VISIBLE
            // binding.llEmptyState.visibility = View.GONE
        }
    }

    private fun updateStats(solicitudes: List<SolicitudAdopcion>) {
        // CORRECCIÓN PRINCIPAL: Usar el enum EstadoSolicitud para las comparaciones
        val pendientes = solicitudes.count { it.estado == EstadoSolicitud.PENDIENTE }
        val aprobadas = solicitudes.count { it.estado == EstadoSolicitud.APROBADA }
        val rechazadas = solicitudes.count { it.estado == EstadoSolicitud.RECHAZADA }

        // Asegúrate de que estos TextView existan en tu fragment_citas.xml o fragment_solicitudes_adopcion.xml
        // Si usas un TabLayout para filtrar, esta lógica podría cambiar.
        // binding.tvPendientes.text = pendientes.toString()
        // binding.tvAprobadas.text = aprobadas.toString()
        // binding.tvRechazadas.text = rechazadas.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}