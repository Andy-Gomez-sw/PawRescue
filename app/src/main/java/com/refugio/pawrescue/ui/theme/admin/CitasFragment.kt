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
import com.refugio.pawrescue.databinding.FragmentCitasBinding
import com.refugio.pawrescue.data.model.SolicitudAdopcion

class CitasFragment : Fragment() {

    private var _binding: FragmentCitasBinding? = null
    private val binding get() = _binding!!

    private lateinit var citasAdapter: CitasAdapter
    private val citasList = mutableListOf<SolicitudAdopcion>()
    private val firestore = FirebaseFirestore.getInstance()

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
        setupRecyclerView()
        setupButtons()
        loadCitasFromFirebase()
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

    private fun loadCitasFromFirebase() {
        binding.progressBar.visibility = View.VISIBLE

        firestore.collection("solicitudes_adopcion")
            .orderBy("fechaSolicitud", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                binding.progressBar.visibility = View.GONE

                if (error != null) {
                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                citasList.clear()
                snapshot?.documents?.forEach { doc ->
                    val cita = doc.toObject(SolicitudAdopcion::class.java)
                    cita?.let { citasList.add(it) }
                }

                citasAdapter.notifyDataSetChanged()
                updateEmptyState()
                updateStats()
            }
    }

    private fun mostrarDialogoNuevaCita() {
        val dialog = NuevaCitaDialog()
        dialog.setOnCitaCreatedListener {
            Toast.makeText(requireContext(), "Cita creada exitosamente", Toast.LENGTH_SHORT).show()
        }
        dialog.show(childFragmentManager, "NuevaCitaDialog")
    }

    private fun aprobarCita(cita: SolicitudAdopcion) {
        firestore.collection("solicitudes_adopcion")
            .document(cita.id)
            .update("estado", "aprobada")
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Cita aprobada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun rechazarCita(cita: SolicitudAdopcion) {
        firestore.collection("solicitudes_adopcion")
            .document(cita.id)
            .update("estado", "rechazada")
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Cita rechazada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
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
        val pendientes = citasList.count { it.estado == "pendiente" }
        val aprobadas = citasList.count { it.estado == "aprobada" }
        val rechazadas = citasList.count { it.estado == "rechazada" }

        binding.tvPendientes.text = pendientes.toString()
        binding.tvAprobadas.text = aprobadas.toString()
        binding.tvRechazadas.text = rechazadas.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}