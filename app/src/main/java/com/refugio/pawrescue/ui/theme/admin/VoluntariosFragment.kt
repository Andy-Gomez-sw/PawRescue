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
import com.refugio.pawrescue.databinding.FragmentVoluntariosBinding
import com.refugio.pawrescue.data.model.Usuario

class VoluntariosFragment : Fragment() {

    private var _binding: FragmentVoluntariosBinding? = null
    private val binding get() = _binding!!

    private lateinit var voluntariosAdapter: VoluntariosAdapter
    private val voluntariosList = mutableListOf<Usuario>()
    private val firestore = FirebaseFirestore.getInstance()

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
        setupRecyclerView()
        setupButtons()
        loadVoluntariosFromFirebase()
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

    private fun loadVoluntariosFromFirebase() {
        binding.progressBar.visibility = View.VISIBLE

        firestore.collection("usuarios")
            .orderBy("nombre", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                binding.progressBar.visibility = View.GONE

                if (error != null) {
                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                voluntariosList.clear()
                snapshot?.documents?.forEach { doc ->
                    val voluntario = doc.toObject(Usuario::class.java)
                    voluntario?.let { voluntariosList.add(it) }
                }

                voluntariosAdapter.notifyDataSetChanged()
                updateEmptyState()
                updateStats()
            }
    }

    private fun filtrarVoluntarios(activo: Boolean?) {
        val filtrados = if (activo == null) {
            voluntariosList
        } else {
            voluntariosList.filter { it.activo == activo }
        }

        voluntariosAdapter.updateList(filtrados)
        updateEmptyState()
    }

    private fun mostrarDialogoNuevoVoluntario() {
        val dialog = NuevoVoluntarioDialog()
        dialog.setOnVoluntarioCreatedListener {
            Toast.makeText(requireContext(), "Voluntario creado exitosamente", Toast.LENGTH_SHORT).show()
        }
        dialog.show(childFragmentManager, "NuevoVoluntarioDialog")
    }

    private fun editarVoluntario(voluntario: Usuario) {
        val dialog = EditarVoluntarioDialog.newInstance(voluntario)
        dialog.show(childFragmentManager, "EditarVoluntarioDialog")
    }

    private fun toggleActivo(voluntario: Usuario) {
        val nuevoEstado = !voluntario.activo

        firestore.collection("usuarios")
            .document(voluntario.id)
            .update("activo", nuevoEstado)
            .addOnSuccessListener {
                val mensaje = if (nuevoEstado) "Voluntario activado" else "Voluntario desactivado"
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
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