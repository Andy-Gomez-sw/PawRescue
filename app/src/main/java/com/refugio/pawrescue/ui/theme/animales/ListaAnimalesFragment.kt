package com.refugio.pawrescue.ui.theme.animales

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.refugio.pawrescue.R
import com.refugio.pawrescue.databinding.FragmentListaAnimalesBinding
import com.refugio.pawrescue.ui.theme.animales.adapters.AnimalesAdapter
import com.refugio.pawrescue.ui.theme.rescate.NuevoRescateActivity
import com.refugio.pawrescue.ui.theme.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListaAnimalesFragment : Fragment() {

    private var _binding: FragmentListaAnimalesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ListaAnimalesViewModel by viewModels()
    private lateinit var animalesAdapter: AnimalesAdapter
    private lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListaAnimalesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = requireContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

        setupRecyclerView()
        setupFilters()
        setupFAB()
        observeViewModel()
        loadData()
    }

    private fun setupRecyclerView() {
        animalesAdapter = AnimalesAdapter { animal ->
            // Verifica si existe la action en nav_graph
            try {
                val action = ListaAnimalesFragmentDirections
                    .actionListaAnimalesFragmentToPerfilAnimalFragment(animal.id)
                findNavController().navigate(action)
            } catch (e: Exception) {
                // Si no existe la navegación, puedes mostrar un Toast o log
                android.util.Log.e("Navigation", "Error navegando: ${e.message}")
            }
        }

        binding.rvAnimales.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = animalesAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupFilters() {
        binding.chipGroupFilters.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val filtro = when (checkedIds[0]) {
                    R.id.chipPerros -> Constants.TIPO_PERRO
                    R.id.chipGatos -> Constants.TIPO_GATO
                    R.id.chipOtros -> Constants.TIPO_OTRO
                    else -> null
                }
                viewModel.filtrarPorEspecie(filtro)
            }
        }
    }

    private fun setupFAB() {
        binding.fabNuevoRescate.setOnClickListener {
            val intent = Intent(requireContext(), NuevoRescateActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        viewModel.animales.observe(viewLifecycleOwner) { animales ->
            if (animales.isEmpty()) {
                binding.rvAnimales.visibility = View.GONE
                binding.llEmptyState.visibility = View.VISIBLE
            } else {
                binding.rvAnimales.visibility = View.VISIBLE
                binding.llEmptyState.visibility = View.GONE
                animalesAdapter.submitList(animales)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun loadData() {
        val refugioId = prefs.getString(Constants.KEY_REFUGIO_ID, "") ?: ""
        if (refugioId.isNotEmpty()) {
            viewModel.loadAnimales(refugioId)
        }
    }

    override fun onResume() {
        super.onResume()
        loadData() // Recargar cuando volvemos de crear un rescate
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}