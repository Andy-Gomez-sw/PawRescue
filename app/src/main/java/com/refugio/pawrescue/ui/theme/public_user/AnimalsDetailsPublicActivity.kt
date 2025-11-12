package com.refugio.pawrescue.ui.theme.public_user

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.refugio.pawrescue.R
import com.refugio.pawrescue.data.model.Animal
import com.refugio.pawrescue.databinding.ActivityAnimalDetailsPublicBinding

class AnimalDetailsPublicActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnimalDetailsPublicBinding
    private lateinit var animal: Animal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnimalDetailsPublicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        animal = intent.getParcelableExtra("animal") ?: run {
            finish()
            return
        }

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        binding.apply {
            toolbar.setNavigationOnClickListener { finish() }

            tvNombre.text = animal.nombre
            tvInfoBasica.text = "${animal.raza} • ${animal.sexo} • ${getEdadText(animal.edad)}"

            tvEstado.text = when (animal.estadoAdopcion) {
                "disponible" -> "❤️ Disponible para adopción"
                "en_proceso" -> "📋 En proceso de adopción"
                "adoptado" -> "🏠 Adoptado"
                else -> ""
            }

            // Cargar foto
            if (animal.fotosPrincipales.isNotEmpty()) {
                Glide.with(this@AnimalDetailsPublicActivity)
                    .load(animal.fotosPrincipales.first())
                    .placeholder(R.drawable.bg_gradient_green)
                    .into(ivAnimalPhoto)
            }

            // Características
            tvVacunas.text = if (animal.vacunasCompletas) "✅ Vacunado" else "❌ Sin vacunas"
            tvEsterilizado.text = if (animal.esterilizado) "✅ Esterilizado" else "❌ No esterilizado"
            tvPeso.text = "${animal.peso} kg"
            tvTemperamento.text = animal.temperamento.ifEmpty { "Amigable" }

            // Historia
            tvHistoria.text = animal.notasRescate.ifEmpty {
                "Este hermoso compañero está buscando un hogar lleno de amor. " +
                        "Es muy sociable y le encanta jugar. Sería perfecto para una familia."
            }
        }
    }

    private fun setupListeners() {
        binding.fabSolicitarAdopcion.setOnClickListener {
            verificarYSolicitar()
        }
    }

    private fun verificarYSolicitar() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Iniciar Sesión")
                .setMessage("Debes iniciar sesión para solicitar una adopción")
                .setPositiveButton("Iniciar Sesión") { _, _ ->
                    // TODO: Navegar a login
                    finish()
                }
                .setNegativeButton("Cancelar", null)
                .show()
            return
        }

        mostrarFormularioAdopcion()
    }

    private fun mostrarFormularioAdopcion() {
        val dialog = SolicitudAdopcionDialog.newInstance(animal.id)
        dialog.show(supportFragmentManager, "SolicitudAdopcionDialog")
    }

    private fun getEdadText(edad: String): String {
        return when (edad) {
            "cachorro" -> "Cachorro"
            "adulto" -> "Adulto"
            "vejez" -> "Mayor"
            else -> edad
        }
    }
}