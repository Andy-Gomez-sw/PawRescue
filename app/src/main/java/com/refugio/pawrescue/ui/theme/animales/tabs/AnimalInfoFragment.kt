package com.refugio.pawrescue.ui.theme.animales.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.refugio.pawrescue.databinding.FragmentAnimalInfoBinding
import com.refugio.pawrescue.ui.theme.animales.PerfilAnimalViewModel
import com.refugio.pawrescue.ui.theme.utils.DateUtils

class AnimalInfoFragment : Fragment() {

    private var _binding: FragmentAnimalInfoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PerfilAnimalViewModel by viewModels({ requireParentFragment() })

    companion object {
        private const val ARG_ANIMAL_ID = "animal_id"

        fun newInstance(animalId: String) = AnimalInfoFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_ANIMAL_ID, animalId)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnimalInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.animal.observe(viewLifecycleOwner) { animal ->
            animal?.let {
                binding.tvRescateFecha.text = DateUtils.formatDate(it.fechaRescate)
                binding.tvRescateUbicacion.text = it.ubicacionRescate?.direccion ?: "No disponible"
                binding.tvPeso.text = "${it.peso} kg"
                binding.tvTemperamento.text = it.temperamento.ifEmpty { "No especificado" }

                // Estado de salud
                binding.tvEstadoGeneral.text = it.estadoSalud
                binding.tvVacunas.text = if (it.vacunasCompletas) "✅ Al día" else "❌ Incompletas"
                binding.tvEsterilizado.text = if (it.esterilizado) "✅ Sí" else "❌ No"

                // Notas
                binding.tvNotas.text = "Animal rescatado en buenas condiciones generales."
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}