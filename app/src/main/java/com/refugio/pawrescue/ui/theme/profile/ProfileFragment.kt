package com.refugio.pawrescue.ui.theme.profile

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.refugio.pawrescue.databinding.FragmentProfileBinding
import com.refugio.pawrescue.ui.theme.auth.LoginActivity
import com.refugio.pawrescue.ui.theme.utils.Constants

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = requireContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

        setupUI()
        observeViewModel()
        loadProfile()
    }

    private fun setupUI() {
        binding.btnEditarPerfil.setOnClickListener {
            Toast.makeText(requireContext(), "Función en desarrollo", Toast.LENGTH_SHORT).show()
        }

        binding.btnNotificaciones.setOnClickListener {
            Toast.makeText(requireContext(), "Función en desarrollo", Toast.LENGTH_SHORT).show()
        }

        binding.btnAyuda.setOnClickListener {
            Toast.makeText(requireContext(), "Función en desarrollo", Toast.LENGTH_SHORT).show()
        }

        binding.btnCerrarSesion.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.usuario.observe(viewLifecycleOwner) { usuario ->
            usuario?.let {
                binding.tvNombre.text = it.nombre
                binding.tvEmail.text = it.email
                binding.tvRol.text = getRolText(it.rol)
                binding.tvAvatarInitial.text = it.nombre.firstOrNull()?.toString()?.uppercase() ?: "U"

                binding.tvRefugioNombre.text = "Centro Canino MX"
                binding.tvRefugioInfo.text = "Toluca, México"
            }
        }

        viewModel.stats.observe(viewLifecycleOwner) { stats ->
            binding.tvRescatesCount.text = stats.rescates.toString()
            binding.tvCuidadosCount.text = stats.cuidados.toString()
            binding.tvAnimalesAsignadosCount.text = stats.animalesAsignados.toString()
            binding.tvDiasActivoCount.text = stats.diasActivo.toString()
        }
    }

    private fun loadProfile() {
        val userId = prefs.getString(Constants.KEY_USER_ID, "") ?: ""
        if (userId.isNotEmpty()) {
            viewModel.loadProfile(userId)
        }
    }

    private fun getRolText(rol: String): String {
        return when (rol) {
            Constants.ROL_ADMIN -> "👑 Administrador"
            Constants.ROL_COORDINADOR -> "⭐ Coordinador"
            Constants.ROL_VOLUNTARIO -> "🤝 Voluntario"
            else -> rol
        }
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setPositiveButton("Cerrar Sesión") { _, _ ->
                logout()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun logout() {
        // Cerrar sesión en Firebase
        FirebaseAuth.getInstance().signOut()

        // Limpiar preferencias
        prefs.edit().clear().apply()

        // Navegar a login
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}