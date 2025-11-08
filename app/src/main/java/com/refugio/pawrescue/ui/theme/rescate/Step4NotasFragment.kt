package com.refugio.pawrescue.ui.theme.rescate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.refugio.pawrescue.R
import com.refugio.pawrescue.databinding.Step4NotasBinding
import com.refugio.pawrescue.ui.theme.utils.Constants

class Step4NotasFragment : Fragment() {

    private var _binding: Step4NotasBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NuevoRescateViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = Step4NotasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        // Notas
        binding.etNotas.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                viewModel.setNotas(binding.etNotas.text.toString())
            }
        }

        // Prioridad
        binding.rgPrioridad.setOnCheckedChangeListener { _, checkedId ->
            val prioridad = when (checkedId) {
                R.id.rbBaja -> Constants.PRIORIDAD_BAJA
                R.id.rbMedia -> Constants.PRIORIDAD_MEDIA
                R.id.rbAlta -> Constants.PRIORIDAD_ALTA
                else -> Constants.PRIORIDAD_MEDIA
            }
            viewModel.setPrioridad(prioridad)
        }

        // Necesita veterinario
        binding.cbNecesitaVet.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setNecesitaVet(isChecked)
        }

        // Video (placeholder)
        binding.btnRecordVideo.setOnClickListener {
            // TODO: Implement video recording
        }
    }

    private fun observeViewModel() {
        // Observar cambios y actualizar resumen
        viewModel.tipoAnimal.observe(viewLifecycleOwner) { updateSummary() }
        viewModel.raza.observe(viewLifecycleOwner) { updateSummary() }
        viewModel.edad.observe(viewLifecycleOwner) { updateSummary() }
        viewModel.prioridad.observe(viewLifecycleOwner) { updateSummary() }
    }

    private fun updateSummary() {
        val tipo = viewModel.tipoAnimal.value ?: "Animal"
        val raza = viewModel.raza.value?.takeIf { it.isNotEmpty() } ?: "Desconocido"
        val edad = viewModel.edad.value ?: "Adulto"
        val prioridad = viewModel.prioridad.value ?: "Media"

        val summary = "Estás por guardar:\n" +
                "• Tipo: $tipo\n" +
                "• Raza: $raza\n" +
                "• Edad: $edad\n" +
                "• Prioridad: $prioridad"

        binding.tvSummary.text = summary
    }

    override fun onPause() {
        super.onPause()
        // Guardar notas al salir
        viewModel.setNotas(binding.etNotas.text.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}