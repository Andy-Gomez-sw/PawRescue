package com.refugio.pawrescue.ui.theme.main

import android.content.Context // <-- AÑADIDO
import android.content.SharedPreferences // <-- AÑADIDO
import android.os.Bundle
import android.util.Log // <-- AÑADIDO
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.refugio.pawrescue.R
import com.refugio.pawrescue.databinding.ActivityMainBinding
import com.refugio.pawrescue.ui.theme.utils.Constants // <-- AÑADIDO
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: SharedPreferences // <-- AÑADIDO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar SharedPreferences
        prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE) // <-- AÑADIDO

        setupNavigation()
    }

    private fun setupNavigation() {
        // --- LÓGICA AÑADIDA PARA CARGAR MENÚ POR ROL ---

        // 1. Lee el ROL que guardaste durante el Login
        val userRole = prefs.getString(Constants.KEY_USER_ROL, Constants.ROL_VOLUNTARIO)

        // 2. Limpia cualquier menú que ya estuviera inflado (importante)
        binding.bottomNavigation.menu.clear()

        // 3. Infla (carga) el menú CORRESPONDIENTE al rol
        // Asegúrate de tener los archivos 'R.menu.admin_bottom_menu' y 'R.menu.voluntario_bottom_menu' en tu carpeta res/menu
        if (userRole == Constants.ROL_ADMIN) {
            // El usuario es Admin
            binding.bottomNavigation.inflateMenu(R.menu.admin_bottom_menu)
            Log.d("MainActivity", "Rol detectado: ADMIN. Cargando menú de admin.")
        } else {
            // El usuario es Voluntario (o cualquier otra cosa)
            binding.bottomNavigation.inflateMenu(R.menu.voluntario_bottom_menu)
            Log.d("MainActivity", "Rol detectado: $userRole. Cargando menú de voluntario.")
        }

        // --- FIN DE LA LÓGICA AÑADIDA ---

        // 4. Tu código original para conectar el NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)
    }
}