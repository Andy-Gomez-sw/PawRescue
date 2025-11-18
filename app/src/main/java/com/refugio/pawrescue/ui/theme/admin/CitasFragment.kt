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
import com.refugio.pawrescue.data.model.EstadoSolicitud // Importar el Enum
import com.refugio.pawrescue.data.model.SolicitudAdopcion
import com.refugio.pawrescue.ui.theme.utils.Constants

class CitasFragment : Fragment() {

    private var _binding: FragmentCitasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CitasViewModel by viewModels()
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
        observeViewModel()

        // --- ¡AQUÍ ESTÁ LA CORRECCIÓN! ---
        // Obtenemos el refugioId del admin
        val refugioId = prefs.getString(Constants.KEY_REFUGIO_ID, "") ?: ""

        // Le pedimos al ViewModel que cargue las citas programadas (usando el Enum)
        // Y le pasamos el refugioId para que pueda filtrar
        viewModel.cargarSolicitudesByEstado(EstadoSolicitud.ENTREVISTA_PROGRAMADA, refugioId)
    }

    private fun setupRecyclerView() {
        citasAdapter = CitasAdapter(
            onAprobarClick = { cita -> aprobarCita(cita) },
            onRechazarClick = { cita -> rechazarCita(cita) },
            onVerDetallesClick = { cita -> verDetalles(cita) }
        )

        binding.rvCitas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = citasAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.solicitudes.observe(viewLifecycleOwner) { solicitudes ->
            citasAdapter.submitList(solicitudes)
            updateEmptyState(solicitudes.isEmpty())
            updateStats(solicitudes)
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

    private fun aprobarCita(cita: SolicitudAdopcion) {
        val evaluador = prefs.getString(Constants.KEY_USER_ID, "") ?: ""
        // TODO: Deberías recargar la lista con el refugioId
        viewModel.aprobarSolicitud(cita.id, evaluador)
        Toast.makeText(requireContext(), "Solicitud aprobada", Toast.LENGTH_SHORT).show()
    }

    private fun rechazarCita(cita: SolicitudAdopcion) {
        val evaluador = prefs.getString(Constants.KEY_USER_ID, "") ?: ""
        // TODO: Deberías recargar la lista con el refugioId
        viewModel.rechazarSolicitud(cita.id, evaluador, "")
        Toast.makeText(requireContext(), "Solicitud rechazada", Toast.LENGTH_SHORT).show()
    }

    private fun verDetalles(cita: SolicitudAdopcion) {
        val dialog = DetalleCitaDialog.newInstance(cita)
        dialog.show(childFragmentManager, "DetalleCitaDialog")
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.rvCitas.visibility = View.GONE
            binding.llEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvCitas.visibility = View.VISIBLE
            binding.llEmptyState.visibility = View.GONE
        }
    }

    private fun updateStats(solicitudes: List<SolicitudAdopcion>) {
        // Esta lógica podría necesitar ajustarse si 'solicitudes' ahora solo trae ENTREVISTA_PROGRAMADA
        val pendientes = solicitudes.count { it.estado == EstadoSolicitud.PENDIENTE }
        val aprobadas = solicitudes.count { it.estado == EstadoSolicitud.APROBADA }
        val rechazadas = solicitudes.count { it.estado == EstadoSolicitud.RECHAZADA }

        binding.tvPendientes.text = pendientes.toString()
        binding.tvAprobadas.text = aprobadas.toString()
        binding.tvRechazadas.text = rechazadas.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}