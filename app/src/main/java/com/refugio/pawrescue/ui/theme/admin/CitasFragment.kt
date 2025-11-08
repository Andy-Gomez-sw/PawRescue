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
import com.refugio.pawrescue.data.model.SolicitudAdopcion
import com.refugio.pawrescue.ui.theme.utils.Constants

class CitasFragment : Fragment() {

    private var _binding: FragmentCitasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CitasViewModel by viewModels()
    private lateinit var citasAdapter: CitasAdapter
    private val citasList = mutableListOf<SolicitudAdopcion>()
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
        setupButtons()
        observeViewModel()
        loadCitas()
    }

    private fun setupRecyclerView() {
        citasAdapter = CitasAdapter(
            citas = citasList,
            onAprobarClick = { cita -> aprobarCita(cita) },
            onRechazarClick = { cita -> rechazarCita(cita) },
            onVerDetallesClick = { cita -> verDetalles(cita) }
        )

        binding.rvCitas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = citasAdapter
        }
    }

    private fun setupButtons() {
        binding.btnNuevaCita.setOnClickListener {
            mostrarDialogoNuevaCita()
        }
    }

    private fun observeViewModel() {
        viewModel.solicitudes.observe(viewLifecycleOwner) { solicitudes ->
            citasList.clear()
            citasList.addAll(solicitudes)
            citasAdapter.notifyDataSetChanged()
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

    private fun loadCitas() {
        viewModel.cargarSolicitudes()
    }

    private fun mostrarDialogoNuevaCita() {
        val dialog = NuevaCitaDialog()
        dialog.setOnCitaCreatedListener {
            Toast.makeText(requireContext(), "Cita creada exitosamente", Toast.LENGTH_SHORT).show()
            loadCitas()
        }
        dialog.show(childFragmentManager, "NuevaCitaDialog")
    }

    private fun aprobarCita(cita: SolicitudAdopcion) {
        val userId = prefs.getString(Constants.KEY_USER_ID, "") ?: ""
        viewModel.aprobarSolicitud(cita.id, userId)
        Toast.makeText(requireContext(), "Cita aprobada", Toast.LENGTH_SHORT).show()
    }

    private fun rechazarCita(cita: SolicitudAdopcion) {
        val userId = prefs.getString(Constants.KEY_USER_ID, "") ?: ""
        viewModel.rechazarSolicitud(cita.id, userId, "Rechazada por el administrador")
        Toast.makeText(requireContext(), "Cita rechazada", Toast.LENGTH_SHORT).show()
    }

    private fun verDetalles(cita: SolicitudAdopcion) {
        val dialog = DetalleCitaDialog.newInstance(cita)
        dialog.show(childFragmentManager, "DetalleCitaDialog")
    }

    private fun updateEmptyState() {
        if (citasList.isEmpty()) {
            binding.rvCitas.visibility = View.GONE
            binding.llEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvCitas.visibility = View.VISIBLE
            binding.llEmptyState.visibility = View.GONE
        }
    }

    private fun updateStats() {
        val pendientes = citasList.count { it.estado == Constants.ADOPCION_PENDIENTE }
        val aprobadas = citasList.count { it.estado == Constants.ADOPCION_APROBADA }
        val rechazadas = citasList.count { it.estado == Constants.ADOPCION_RECHAZADA }

        binding.tvPendientes.text = pendientes.toString()
        binding.tvAprobadas.text = aprobadas.toString()
        binding.tvRechazadas.text = rechazadas.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}