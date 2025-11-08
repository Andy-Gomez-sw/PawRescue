package com.refugio.pawrescue

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.refugio.pawrescue.databinding.ActivityMainBinding // <-- Importa tu binding

class MainActivity : AppCompatActivity() {

    // 1. Declara la variable de binding
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. Infla el layout usando ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)

        // 3. Establece la vista usando la raíz del binding
        setContentView(binding.root)

        // Ahora puedes acceder a tus vistas, por ejemplo:
        // binding.miBoton.setOnClickListener { ... }
        // binding.miTextView.text = "Hola Mundo"
    }
}

// 4. BORRA todo lo que estaba aquí abajo (borra las funciones Greeting y GreetingPreview)
