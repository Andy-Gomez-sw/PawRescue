package com.refugio.pawrescue.ui.theme.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.refugio.pawrescue.R
import com.refugio.pawrescue.ui.theme.auth.LoginActivity
import com.refugio.pawrescue.ui.theme.main.MainActivity
import com.refugio.pawrescue.ui.theme.public_user.PublicMainActivity
import com.refugio.pawrescue.ui.theme.utils.Constants

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private val splashTimeOut: Long = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)

        Handler(Looper.getMainLooper()).postDelayed({
            checkAuthentication()
        }, splashTimeOut)
    }

    private fun checkAuthentication() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = prefs.getString(Constants.KEY_USER_ID, null)

        val userRol = prefs.getString(Constants.KEY_USER_ROL, null)

        val intent = if (currentUser != null && !userId.isNullOrEmpty() && !userRol.isNullOrEmpty()) {

            if (userRol == "publico") {
                Intent(this, PublicMainActivity::class.java)
            } else {
                Intent(this, MainActivity::class.java)
            }

        } else {
            // Si no hay sesión válida o falta algún dato (ID o Rol), cerrar Firebase Auth y limpiar prefs
            if (currentUser == null || userId.isNullOrEmpty() || userRol.isNullOrEmpty()) {
                FirebaseAuth.getInstance().signOut()
                clearUserPreferences()
            }
            Intent(this, LoginActivity::class.java)
        }

        startActivity(intent)
        finish()
    }

    private fun clearUserPreferences() {
        prefs.edit().apply {
            remove(Constants.KEY_USER_ID)
            remove(Constants.KEY_USER_ROL)
            remove(Constants.KEY_REFUGIO_ID)
            val rememberMe = prefs.getBoolean(Constants.KEY_REMEMBER_ME, false)
            if (!rememberMe) {
                remove(Constants.KEY_USER_EMAIL)
                remove(Constants.KEY_REMEMBER_ME)
            }
            apply()
        }
    }
}