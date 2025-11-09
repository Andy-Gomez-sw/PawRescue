package com.refugio.pawrescue.ui.theme.public_user

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.refugio.pawrescue.databinding.ActivityPublicRegisterBinding
import com.refugio.pawrescue.ui.theme.auth.LoginActivity

class PublicRegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPublicRegisterBinding
    private val viewModel: PublicRegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPublicRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnRegistrar.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val passwordConfirm = binding.etPasswordConfirm.text.toString().trim()
            val telefono = binding.etTelefono.text.toString().trim()

            if (validateInputs(nombre, email, password, passwordConfirm, telefono)) {
                viewModel.register(nombre, email, password, telefono)
            }
        }

        binding.tvYaTienesCuenta.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validateInputs(
        nombre: String,
        email: String,
        password: String,
        passwordConfirm: String,
        telefono: String
    ): Boolean {
        var isValid = true

        if (nombre.isEmpty()) {
            binding.tilNombre.error = "El nombre es requerido"
            isValid = false
        } else {
            binding.tilNombre.error = null
        }

        if (email.isEmpty()) {
            binding.tilEmail.error = "El email es requerido"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Email inválido"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "La contraseña es requerida"
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "Mínimo 6 caracteres"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        if (passwordConfirm != password) {
            binding.tilPasswordConfirm.error = "Las contraseñas no coinciden"
            isValid = false
        } else {
            binding.tilPasswordConfirm.error = null
        }

        if (telefono.isEmpty()) {
            binding.tilTelefono.error = "El teléfono es requerido"
            isValid = false
        } else {
            binding.tilTelefono.error = null
        }

        return isValid
    }

    private fun observeViewModel() {
        viewModel.registerState.observe(this) { state ->
            when (state) {
                is RegisterState.Loading -> showLoading(true)
                is RegisterState.Success -> {
                    showLoading(false)
                    Toast.makeText(
                        this,
                        "Registro exitoso. Bienvenido!",
                        Toast.LENGTH_LONG
                    ).show()
                    navigateToPublicMain()
                }
                is RegisterState.Error -> {
                    showLoading(false)
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                else -> showLoading(false)
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnRegistrar.isEnabled = !show
    }

    private fun navigateToPublicMain() {
        val intent = Intent(this, PublicMainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}