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
import com.refugio.pawrescue.data.model.SolicitudAdopcion // <-- AÑADIR IMPORT
import com.refugio.pawrescue.databinding.FragmentDashboardBinding
import com.refugio.pawrescue.ui.theme.admin.SolicitudesPendientesAdapter // <-- AÑADIR IMPORT
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
    private lateinit var prefs: SharedPreferences

    // Dos adapters, uno para cada rol
    private lateinit var animalesAdapter: AnimalesAdapter // Para Voluntarios
    private lateinit var solicitudesAdapter: SolicitudesPendientesAdapter // Para Admin

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

        setupToolbar()
        setupRecyclerViews() // <-- CAMBIADO A PLURAL
        observeViewModel()
        loadData()

        // setupUIBasedOnRole se llama al final, después de cargar los datos
        // para que sepa qué rol mostrar.
    }

    private fun setupToolbar() {
        binding.toolbar.inflateMenu(R.menu.dashboard_menu)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_profile -> {
                    try {
                        findNavController().navigate(R.id.action_dashboardFragment_to_perfilFragment)
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Perfil próximamente", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.action_logout -> {
                    logout()
                    true
                }
                else -> false
            }
        }
    }

    private fun logout() {
        // (Tu función de logout... se queda igual)
        FirebaseAuth.getInstance().signOut()
        // ... (resto de tu código de logout)
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun setupUIBasedOnRole() {
        val userRole = prefs.getString(Constants.KEY_USER_ROL, Constants.ROL_VOLUNTARIO)
        val userName = prefs.getString("user_name", "Usuario") ?: "Usuario"

        val refugioName = prefs.getString("refugio_name", "Refugio") ?: "Refugio"
        binding.tvRefugio.text = "Refugio: $refugioName"

        when (userRole) {
            Constants.ROL_ADMIN -> {
                binding.tvGreeting.text = "👋 Hola [Admin], $userName"

                // Mostrar acciones de Admin
                binding.cardNewRescue.visibility = View.VISIBLE
                binding.cardChecklist.visibility = View.VISIBLE
                binding.cardVetUrgent.visibility = View.VISIBLE

                // --- GESTIÓN DE SECCIONES POR ROL ---
                binding.llSolicitudesPendientesSection.visibility = View.VISIBLE
                binding.llAnimalesAsignadosSection.visibility = View.GONE

                // (Listeners de acciones rápidas)
                binding.cardNewRescue.setOnClickListener {
                    startActivity(Intent(requireContext(), NuevoRescateActivity::class.java))
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

                // Mostrar acciones de Voluntario
                binding.cardNewRescue.visibility = View.GONE
                binding.cardChecklist.visibility = View.VISIBLE
                binding.cardVetUrgent.visibility = View.GONE

                // --- GESTIÓN DE SECCIONES POR ROL ---
                binding.llSolicitudesPendientesSection.visibility = View.GONE
                binding.llAnimalesAsignadosSection.visibility = View.VISIBLE

                binding.cardChecklist.setOnClickListener {
                    Toast.makeText(requireContext(), "Checklist (próximamente)", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                // (Rol por defecto o desconocido)
                binding.tvGreeting.text = "👋 Hola, $userName"
                binding.cardNewRescue.visibility = View.GONE
                binding.cardChecklist.visibility = View.GONE
                binding.cardVetUrgent.visibility = View.GONE
                binding.llSolicitudesPendientesSection.visibility = View.GONE
                binding.llAnimalesAsignadosSection.visibility = View.GONE
            }
        }
    }

    // --- RENOMBRADA A PLURAL ---
    private fun setupRecyclerViews() {
        // 1. Configurar adapter de Animales (para Voluntarios)
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

        // 2. Configurar adapter de Solicitudes (para Admin)
        solicitudesAdapter = SolicitudesPendientesAdapter { solicitud ->
            // Al hacer clic, abrimos el diálogo para gestionar la solicitud
            abrirDialogGestionar(solicitud)
        }
        binding.rvSolicitudes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = solicitudesAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        // Observador para la lista de Animales (Voluntario)
        viewModel.animales.observe(viewLifecycleOwner) { animales ->
            if (animales.isEmpty()) {
                binding.rvAnimales.visibility = View.GONE
                binding.llAnimalesEmptyState.visibility = View.VISIBLE
            } else {
                binding.rvAnimales.visibility = View.VISIBLE
                binding.llAnimalesEmptyState.visibility = View.GONE
                animalesAdapter.submitList(animales)
            }
        }

        // --- AÑADIR NUEVO OBSERVADOR ---
        // Observador para la lista de Solicitudes (Admin)
        viewModel.solicitudesPendientes.observe(viewLifecycleOwner) { solicitudes ->
            if (solicitudes.isEmpty()) {
                binding.rvSolicitudes.visibility = View.GONE
                binding.llSolicitudesEmptyState.visibility = View.VISIBLE
            } else {
                binding.rvSolicitudes.visibility = View.VISIBLE
                binding.llSolicitudesEmptyState.visibility = View.GONE
                solicitudesAdapter.submitList(solicitudes)
            }
        }
        // --- FIN DE NUEVO OBSERVADOR ---

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
        val userId = prefs.getString(Constants.KEY_USER_ID, "") ?: ""
        val refugioId = prefs.getString(Constants.KEY_REFUGIO_ID, "") ?: ""
        val userRole = prefs.getString(Constants.KEY_USER_ROL, Constants.ROL_VOLUNTARIO) // <-- Leer rol

        if (refugioId.isNotEmpty()) {
            // Decidir qué datos cargar según el rol
            if (userRole == Constants.ROL_ADMIN) {
                // Si es Admin, cargar solicitudes pendientes
                viewModel.loadSolicitudesPendientes(refugioId)
                // (Opcional) El admin también puede tener animales asignados
                // viewModel.loadAnimales(userId, refugioId)
            } else {
                // Si es Voluntario, cargar sus animales y stats
                viewModel.loadAnimales(userId, refugioId)
                viewModel.loadStats(userId)
            }
        }

        // Llamar a setupUIBasedOnRole aquí, ahora que sabemos qué cargar
        setupUIBasedOnRole()
    }

    // --- AÑADIR NUEVA FUNCIÓN ---
    private fun abrirDialogGestionar(solicitud: SolicitudAdopcion) {
        // Aquí usamos el Navigation Component para abrir el DialogFragment
        // Asegúrate de tener una acción en tu 'nav_graph.xml' que vaya
        // de 'dashboardFragment' a 'gestionarSolicitudDialog'
        try {
            val action = DashboardFragmentDirections
                .actionDashboardFragmentToGestionarSolicitudDialog(solicitud.id)
            findNavController().navigate(action)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al abrir gestión: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}