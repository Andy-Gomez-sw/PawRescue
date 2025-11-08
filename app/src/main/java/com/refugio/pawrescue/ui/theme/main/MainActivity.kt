package com.refugio.pawrescue.ui.theme.main

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.refugio.pawrescue.R
import com.refugio.pawrescue.databinding.ActivityMainBinding
import com.refugio.pawrescue.ui.theme.utils.Constants

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar SharedPreferences
        prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)

        // Configurar el NavController
        // Cambiado a navHostFragment (el ID correcto de tu XML)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        // --- ¡LÓGICA DE ROLES! ---
        setupBottomNavBasedOnRole()
    }

    private fun setupBottomNavBasedOnRole() {
        // 1. Limpiar cualquier menú anterior (importante)
        binding.bottomNavigation.menu.clear()

        // 2. Leer el rol guardado
        val userRole = prefs.getString(Constants.KEY_USER_ROL, Constants.ROL_VOLUNTARIO)

        if (userRole == Constants.ROL_ADMIN) {
            // El usuario es Admin
            binding.bottomNavigation.inflateMenu(R.menu.admin_bottom_menu)
        } else {
            // El usuario es Voluntario
            binding.bottomNavigation.inflateMenu(R.menu.voluntario_bottom_menu)
        }

        // También actualizado el ID del BottomNavigationView
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController)
    }

    // Permitir que el botón "Atrás" funcione con los fragments
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}