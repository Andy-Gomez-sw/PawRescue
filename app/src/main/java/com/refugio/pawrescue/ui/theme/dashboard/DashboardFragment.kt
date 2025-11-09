package com.refugio.pawrescue.ui.theme.dashboard

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.refugio.pawrescue.R
import com.refugio.pawrescue.databinding.FragmentDashboardBinding
import com.refugio.pawrescue.ui.theme.animales.adapters.AnimalesAdapter
import com.refugio.pawrescue.ui.theme.auth.LoginActivity
import com.refugio.pawrescue.ui.theme.rescate.NuevoRescateActivity
import com.refugio.pawrescue.ui.theme.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var animalesAdapter: AnimalesAdapter
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setHasOptionsMenu(true) // <-- ELIMINA ESTA LÍNEA
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = requireContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

        // --- AÑADE ESTA LLAMADA ---
        setupToolbar()

        setupUIBasedOnRole()
        setupRecyclerView()
        observeViewModel()
        loadData()
    }

    // --- ELIMINA 'onCreateOptionsMenu' y 'onOptionsItemSelected' ---
    // override fun onCreateOptionsMenu(...) { ... }
    // override fun onOptionsItemSelected(...) { ... }

    // --- AÑADE ESTA NUEVA FUNCIÓN ---
    private fun setupToolbar() {
        // Tu Toolbar en el XML se llama 'toolbar', no 'dashboard_toolbar'
        binding.toolbar.inflateMenu(R.menu.dashboard_menu)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_profile -> {
                    try {
                        // Navega al perfil del usuario
                        // (Asegúrate de tener esta acción en nav_graph.xml)
                        findNavController().navigate(R.id.action_dashboardFragment_to_perfilFragment)
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Perfil próximamente", Toast.LENGTH_SHORT).show()
                    }
                    true // Indica que el clic fue manejado
                }
                R.id.action_logout -> {
                    logout()
                    true // Indica que el clic fue manejado
                }
                else -> false // No se manejó
            }
        }
    }


    private fun logout() {
        // (Tu función de logout... esta ya estaba perfecta)
        FirebaseAuth.getInstance().signOut()
        val rememberMe = prefs.getBoolean(Constants.KEY_REMEMBER_ME, false)
        prefs.edit().apply {
            remove(Constants.KEY_USER_ID)
            remove(Constants.KEY_USER_ROL)
            remove(Constants.KEY_REFUGIO_ID)
            remove("user_name")
            remove("refugio_name")
            if (!rememberMe) {
                remove(Constants.KEY_USER_EMAIL)
                remove(Constants.KEY_REMEMBER_ME)
            }
            apply()
        }
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun setupUIBasedOnRole() {
        // (Esta función tuya ya estaba bien y debería funcionar)
        val userRole = prefs.getString(Constants.KEY_USER_ROL, Constants.ROL_VOLUNTARIO)
        val userName = prefs.getString("user_name", "Usuario") ?: "Usuario"

        when (userRole) {
            Constants.ROL_ADMIN -> {
                binding.tvGreeting.text = "👋 Hola [Admin], $userName"
                binding.cardNewRescue.visibility = View.VISIBLE
                binding.cardChecklist.visibility = View.VISIBLE
                binding.cardVetUrgent.visibility = View.VISIBLE

                binding.cardNewRescue.setOnClickListener {
                    val intent = Intent(requireContext(), NuevoRescateActivity::class.java)
                    startActivity(intent)
                }
                binding.cardChecklist.setOnClickListener {
                    Toast.makeText(requireContext(), "Checklist (próximamente)", Toast.LENGTH_SHORT).show()
                }
                binding.cardVetUrgent.setOnClickListener {
                    Toast.makeText(requireContext(), "Veterinaria Urgente (próximamente)", Toast.LENGTH_SHORT).show()
                }
            }
            Constants.ROL_VOLUNTARIO -> {
                binding.tvGreeting.text = "👋 Hola [Voluntario], $userName"
                binding.cardNewRescue.visibility = View.GONE
                binding.cardChecklist.visibility = View.VISIBLE
                binding.cardVetUrgent.visibility = View.GONE

                binding.cardChecklist.setOnClickListener {
                    Toast.makeText(requireContext(), "Checklist (próximamente)", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                binding.tvGreeting.text = "👋 Hola, $userName"
                binding.cardNewRescue.visibility = View.GONE
                binding.cardChecklist.visibility = View.GONE
                binding.cardVetUrgent.visibility = View.GONE
            }
        }

        val refugioName = prefs.getString("refugio_name", "Refugio") ?: "Refugio"
        binding.tvRefugio.text = "Refugio: $refugioName"
    }

    private fun setupRecyclerView() {
        // (Esta función tuya ya estaba bien)
        animalesAdapter = AnimalesAdapter { animal ->
            try {
                val action = DashboardFragmentDirections
                    .actionDashboardFragmentToPerfilAnimalFragment(animal.id)
                findNavController().navigate(action)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al abrir perfil", Toast.LENGTH_SHORT).show()
            }
        }

        binding.rvAnimales.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = animalesAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        // (Esta función tuya ya estaba bien)
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

        viewModel.stats.observe(viewLifecycleOwner) { stats ->
            binding.tvActiveAnimals.text = stats.activeAnimals.toString()
            binding.tvCompleted.text = stats.completed.toString()
            binding.tvPending.text = stats.pending.toString()
            binding.tvAlerts.text = stats.alerts.toString()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun loadData() {
        // (Esta función tuya ya estaba bien)
        val userId = prefs.getString(Constants.KEY_USER_ID, "") ?: ""
        val refugioId = prefs.getString(Constants.KEY_REFUGIO_ID, "") ?: ""

        if (userId.isNotEmpty() && refugioId.isNotEmpty()) {
            viewModel.loadAnimales(userId, refugioId)
            viewModel.loadStats(userId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}