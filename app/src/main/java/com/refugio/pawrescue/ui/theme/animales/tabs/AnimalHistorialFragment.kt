package com.refugio.pawrescue.ui.theme.animales.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.refugio.pawrescue.R
import com.refugio.pawrescue.databinding.FragmentAnimalHistorialBinding

class AnimalHistorialFragment : Fragment() {

    private var _binding: FragmentAnimalHistorialBinding? = null
    private val binding get() = _binding!!
    private var animalId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            animalId = it.getString(ARG_ANIMAL_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnimalHistorialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvHistorialPlaceholder.text = "Mostrando historial para el animal ID: $animalId"
        // Aquí cargarías el historial del animal
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_ANIMAL_ID = "animal_id"

        @JvmStatic
        fun newInstance(animalId: String) =
            AnimalHistorialFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ANIMAL_ID, animalId)
                }
            }
    }
}

