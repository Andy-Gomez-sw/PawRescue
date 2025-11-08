package com.refugio.pawrescue.ui.theme.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.refugio.pawrescue.data.model.Usuario
import com.refugio.pawrescue.data.model.repository.AuthRepository
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

data class ProfileStats(
    val rescates: Int = 0,
    val cuidados: Int = 0,
    val animalesAsignados: Int = 0,
    val diasActivo: Int = 0
)

class ProfileViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _usuario = MutableLiveData<Usuario?>()
    val usuario: LiveData<Usuario?> = _usuario

    private val _stats = MutableLiveData<ProfileStats>()
    val stats: LiveData<ProfileStats> = _stats

    fun loadProfile(userId: String) {
        viewModelScope.launch {
            val usuarioData = authRepository.getUserData(userId)
            _usuario.value = usuarioData

            // Calcular estadísticas
            val diasActivo = calcularDiasActivo(usuarioData.fechaRegistro)
            _stats.value = ProfileStats(
                rescates = 15, // TODO: Obtener de Firestore
                cuidados = 48,
                animalesAsignados = usuarioData.animalesAsignados.size,
                diasActivo = diasActivo
            )
        }
    }

    private fun calcularDiasActivo(fechaRegistro: Date?): Int {
        return if (fechaRegistro != null) {
            val diff = Date().time - fechaRegistro.time
            TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()
        } else {
            0
        }
    }
}