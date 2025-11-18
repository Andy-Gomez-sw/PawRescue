package com.refugio.pawrescue.ui.theme.auth

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.refugio.pawrescue.R
import com.refugio.pawrescue.databinding.ActivityLoginBinding
import com.refugio.pawrescue.ui.theme.public_user.PublicRegisterActivity
import com.refugio.pawrescue.ui.theme.main.MainActivity
import com.refugio.pawrescue.ui.theme.utils.Constants
import com.refugio.pawrescue.ui.theme.utils.NetworkUtils

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)

        setupUI()
        observeViewModel()
        checkRememberedUser()
        updateConnectionStatus()
    }

    private fun setupUI() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInputs(email, password)) {
                viewModel.login(email, password)
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            // TODO: Implementar recuperación de contraseña
            Toast.makeText(this, "Función en desarrollo", Toast.LENGTH_SHORT).show()
        }

        binding.tvRegistrarsePublico.setOnClickListener {
            startActivity(Intent(this, PublicRegisterActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginState.Loading -> showLoading(true)
                is LoginState.Success -> {
                    showLoading(false)
                    saveUserData(state.usuario)
                    navigateToMain()
                }
                is LoginState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                else -> showLoading(false)
            }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

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
            binding.tilPassword.error = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        return isValid
    }

    private fun saveUserData(usuario: com.refugio.pawrescue.data.model.Usuario) {
        prefs.edit().apply {
            putString(Constants.KEY_USER_ID, usuario.id)
            putString(Constants.KEY_USER_ROL, usuario.rol)
            putString(Constants.KEY_REFUGIO_ID, usuario.refugioId)
            putBoolean(Constants.KEY_REMEMBER_ME, binding.cbRememberMe.isChecked)

            // Guardar email solo si está "recordarme" activado
            if (binding.cbRememberMe.isChecked) {
                putString(Constants.KEY_USER_EMAIL, binding.etEmail.text.toString())
            } else {
                remove(Constants.KEY_USER_EMAIL)
            }

            apply()
        }
    }

    private fun checkRememberedUser() {
        val rememberMe = prefs.getBoolean(Constants.KEY_REMEMBER_ME, false)
        if (rememberMe) {
            val email = prefs.getString(Constants.KEY_USER_EMAIL, "")
            binding.etEmail.setText(email)
            binding.cbRememberMe.isChecked = true
        }
    }

    private fun updateConnectionStatus() {
        val isConnected = NetworkUtils.isNetworkAvailable(this)

        if (isConnected) {
            binding.viewConnectionIndicator.setBackgroundResource(R.drawable.bg_circle_green)
            binding.tvConnectionStatus.text = "ONLINE"
            binding.tvConnectionStatus.setTextColor(
                ContextCompat.getColor(this, R.color.badge_complete_text)
            )
            binding.llConnectionStatus.setBackgroundResource(R.drawable.bg_badge_complete)
        } else {
            binding.viewConnectionIndicator.setBackgroundColor(
                ContextCompat.getColor(this, R.color.status_warning)
            )
            binding.tvConnectionStatus.text = "OFFLINE"
            binding.tvConnectionStatus.setTextColor(
                ContextCompat.getColor(this, R.color.badge_pending_text)
            )
            binding.llConnectionStatus.setBackgroundResource(R.drawable.bg_badge_pending)
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !show
        binding.etEmail.isEnabled = !show
        binding.etPassword.isEnabled = !show
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}