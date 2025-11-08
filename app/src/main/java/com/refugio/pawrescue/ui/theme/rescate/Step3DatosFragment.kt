package com.refugio.pawrescue.ui.theme.rescate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.chip.Chip
import com.refugio.pawrescue.R
import com.refugio.pawrescue.databinding.Step3DatosBinding
import com.refugio.pawrescue.ui.theme.utils.Constants

class Step3DatosFragment : Fragment() {

    private var _binding: Step3DatosBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NuevoRescateViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = Step3DatosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDropdowns()
        setupListeners()
    }

    private fun setupDropdowns() {
        // Tipo de animal
        val tiposAnimal = arrayOf(
            "🐕 Perro", // Asumiendo que tienes R.string.animal_type_dog = "Perro"
            "🐈 Gato", // Asumiendo que tienes R.string.animal_type_cat = "Gato"
            "🐦 Ave", // Asumiendo que tienes R.string.animal_type_bird = "Ave"
            "🐾 Otro"  // Asumiendo que tienes R.string.animal_type_other = "Otro"
        )
        // Nota: Si R.string... no funciona, es porque falta el getString().
        // Lo he simplificado para que no falle si no tienes esos strings.
        val adapterTipo = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, tiposAnimal)
        binding.actvTipo.setAdapter(adapterTipo)
        binding.actvTipo.setText(tiposAnimal[0], false)

        // Estado de salud
        val estadosSalud = arrayOf(
            "🩺 Bueno", // Asumiendo R.string.health_good
            "⚠️ Regular", // Asumiendo R.string.health_regular
            "🚨 Malo" // Asumiendo R.string.health_bad
        )
        val adapterSalud = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, estadosSalud)
        binding.actvSalud.setAdapter(adapterSalud)
        binding.actvSalud.setText(estadosSalud[0], false)
    }

    private fun setupListeners() {
        // Tipo de animal
        binding.actvTipo.setOnItemClickListener { _, _, position, _ ->
            val tipo = when (position) {
                0 -> Constants.TIPO_PERRO
                1 -> Constants.TIPO_GATO
                2 -> Constants.TIPO_AVE
                else -> Constants.TIPO_OTRO
            }
            viewModel.setTipoAnimal(tipo)
        }

        // Raza
        binding.etRaza.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                viewModel.setRaza(binding.etRaza.text.toString())
            }
        }

        // Edad
        binding.chipGroupAge.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val edad = when (checkedIds[0]) {
                    R.id.chipCachorro -> Constants.EDAD_CACHORRO
                    R.id.chipAdulto -> Constants.EDAD_ADULTO
                    R.id.chipVejez -> Constants.EDAD_VEJEZ
                    else -> Constants.EDAD_ADULTO
                }
                viewModel.setEdad(edad)
            }
        }

        // Sexo
        binding.rgSexo.setOnCheckedChangeListener { _, checkedId ->
            val sexo = when (checkedId) {
                R.id.rbMacho -> "macho"
                R.id.rbHembra -> "hembra"
                R.id.rbDesconocido -> "desconocido"
                else -> "macho"
            }
            viewModel.setSexo(sexo)
        }

        // Estado de salud
        binding.actvSalud.setOnItemClickListener { _, _, position, _ ->
            val salud = when (position) {
                0 -> Constants.SALUD_BUENO
                1 -> Constants.SALUD_REGULAR
                2 -> Constants.SALUD_MALO
                else -> Constants.SALUD_BUENO
            }
            viewModel.setEstadoSalud(salud)
        }

        // Condiciones especiales
        val checkBoxes = listOf(
            binding.cbHeridas to "heridas",
            binding.cbDesnutrido to "desnutrido",
            binding.cbParasitos to "parasitos",
            binding.cbPrenada to "prenada"
        )

        checkBoxes.forEach { (checkbox, condition) ->
            checkbox.setOnCheckedChangeListener { _, _ ->
                updateCondiciones(checkBoxes)
            }
        }
    }

    private fun updateCondiciones(checkBoxes: List<Pair<android.widget.CheckBox, String>>) {
        val condiciones = checkBoxes
            .filter { it.first.isChecked }
            .map { it.second }
        viewModel.setCondicionesEspeciales(condiciones)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
