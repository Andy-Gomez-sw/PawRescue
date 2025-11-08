package com.refugio.pawrescue.ui.theme.animales.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.refugio.pawrescue.databinding.FragmentAnimalCuidadosBinding
import com.refugio.pawrescue.data.model.Cuidado
import com.refugio.pawrescue.ui.theme.animales.adapters.CuidadosAdapter
import java.text.SimpleDateFormat
import java.util.*

class AnimalCuidadosFragment : Fragment() {

    private var _binding: FragmentAnimalCuidadosBinding? = null
    private val binding get() = _binding!!

    private lateinit var cuidadosAdapter: CuidadosAdapter
    private val cuidadosList = mutableListOf<Cuidado>()
    private var animalId: String? = null

    companion object {
        private const val ARG_ANIMAL_ID = "animal_id"

        fun newInstance(animalId: String) = AnimalCuidadosFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_ANIMAL_ID, animalId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            animalId = it.getString(ARG_ANIMAL_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnimalCuidadosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        loadCuidados()
    }

    private fun setupRecyclerView() {
        cuidadosAdapter = CuidadosAdapter(
            cuidados = cuidadosList,
            onEditClick = { cuidado ->
                editarCuidado(cuidado)
            },
            onDeleteClick = { cuidado ->
                eliminarCuidado(cuidado)
            }
        )

        binding.rvCuidados.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cuidadosAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnAgregarCuidado.setOnClickListener {
            agregarNuevoCuidado()
        }
    }

    private fun loadCuidados() {
        // TODO: Cargar cuidados desde la base de datos
        // Por ahora, datos de ejemplo
        cuidadosList.clear()
        cuidadosList.addAll(getCuidadosEjemplo())
        cuidadosAdapter.notifyDataSetChanged()

        updateEmptyState()
    }

    private fun getCuidadosEjemplo(): List<Cuidado> {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val calendar = Calendar.getInstance()

        return listOf(
            Cuidado(
                id = "1",
                animalId = animalId ?: "",
                tipo = "Alimentación",
                descripcion = "Comida seca para perros adultos - 300g",
                completado = true,
                horaProgramada = calendar.time,
                voluntarioId = "vol1",
                voluntarioNombre = "María González",
                fotosEvidencia = emptyList(),
                observaciones = ""
            ),
            Cuidado(
                id = "2",
                animalId = animalId ?: "",
                tipo = "Baño",
                descripcion = "Baño completo con shampoo especial",
                completado = false,
                horaProgramada = calendar.apply {
                    add(Calendar.DAY_OF_MONTH, -2)
                }.time,
                voluntarioId = "vol2",
                voluntarioNombre = "Carlos Ramírez",
                fotosEvidencia = emptyList(),
                observaciones = ""
            ),
            Cuidado(
                id = "3",
                animalId = animalId ?: "",
                tipo = "Paseo",
                descripcion = "Paseo de 30 minutos por el parque",
                completado = true,
                horaProgramada = calendar.apply {
                    add(Calendar.HOUR_OF_DAY, -4)
                }.time,
                voluntarioId = "vol3",
                voluntarioNombre = "Ana López",
                fotosEvidencia = emptyList(),
                observaciones = ""
            ),
            Cuidado(
                id = "4",
                animalId = animalId ?: "",
                tipo = "Medicación",
                descripcion = "Antiparasitario oral - Dosis completa",
                completado = false,
                horaProgramada = calendar.apply {
                    add(Calendar.DAY_OF_MONTH, -5)
                }.time,
                voluntarioId = "vol4",
                voluntarioNombre = "Dr. Veterinario",
                fotosEvidencia = emptyList(),
                observaciones = ""
            )
        )
    }

    private fun agregarNuevoCuidado() {
        Toast.makeText(
            requireContext(),
            "Funcionalidad de agregar cuidado próximamente",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun editarCuidado(cuidado: Cuidado) {
        Toast.makeText(
            requireContext(),
            "Editar: ${cuidado.tipo}",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun eliminarCuidado(cuidado: Cuidado) {
        cuidadosList.remove(cuidado)
        cuidadosAdapter.notifyDataSetChanged()
        updateEmptyState()

        Toast.makeText(
            requireContext(),
            "Cuidado eliminado",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateEmptyState() {
        if (cuidadosList.isEmpty()) {
            binding.rvCuidados.visibility = View.GONE
            binding.llEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvCuidados.visibility = View.VISIBLE
            binding.llEmptyState.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}