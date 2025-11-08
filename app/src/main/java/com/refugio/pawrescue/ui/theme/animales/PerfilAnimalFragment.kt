package com.refugio.pawrescue.ui.theme.animales

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.refugio.pawrescue.R
import com.refugio.pawrescue.databinding.FragmentPerfilAnimalBinding

class PerfilAnimalFragment : Fragment() {

    private var _binding: FragmentPerfilAnimalBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PerfilAnimalViewModel by viewModels()
    private val args: PerfilAnimalFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilAnimalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupTabs()
        observeViewModel()
        loadAnimal()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupTabs() {
        val adapter = AnimalDetailsPagerAdapter(this, args.animalId)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Información"
                1 -> "Cuidados"
                2 -> "Historial"
                else -> ""
            }
        }.attach()
    }

    private fun observeViewModel() {
        viewModel.animal.observe(viewLifecycleOwner) { animal ->
            animal?.let {
                binding.tvNombre.text = "${it.nombre} #${it.id.take(4).uppercase()}"
                binding.tvInfoBasica.text = "${it.raza} • ${it.sexo} • ${it.edad}"

                // Cargar imagen
                if (it.fotosPrincipales.isNotEmpty()) {
                    Glide.with(requireContext())
                        .load(it.fotosPrincipales.first())
                        .placeholder(R.drawable.bg_gradient_green)
                        .into(binding.ivAnimalPhoto)
                }

                // Estado de adopción
                binding.tvEstadoAdopcion.text = when (it.estadoAdopcion) {
                    "disponible" -> "❤️ Disponible para adopción"
                    "en_proceso" -> "📋 En proceso de adopción"
                    "adoptado" -> "🏠 Adoptado"
                    else -> ""
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun loadAnimal() {
        viewModel.loadAnimal(args.animalId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}