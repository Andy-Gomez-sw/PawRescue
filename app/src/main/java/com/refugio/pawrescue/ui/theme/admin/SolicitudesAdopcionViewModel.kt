package com.refugio.pawrescue.ui.theme.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.refugio.pawrescue.data.model.SolicitudAdopcion
import com.refugio.pawrescue.data.model.repository.AdopcionRepository
import java.util.Date
class SolicitudesAdopcionViewModel(
    private val adopcionRepository: AdopcionRepository
) : ViewModel() {

    // --- Lógica para CARGAR LA LISTA (El código que te faltaba) ---
    private val _solicitudes = MutableLiveData<List<SolicitudAdopcion>>()
    val solicitudes: LiveData<List<SolicitudAdopcion>> = _solicitudes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    init {
        cargarSolicitudes() // Carga las solicitudes al iniciar
    }

    fun cargarSolicitudes() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = adopcionRepository.getSolicitudes() // Usa la función de tu repo
            result.onSuccess { lista ->
                _solicitudes.value = lista
            }
            result.onFailure {
                _error.value = it.message ?: "Error al cargar solicitudes"
            }
            _isLoading.value = false
        }
    }

    // --- Lógica NUEVA (La que tú pegaste, ahora conectada) ---

    private val _solicitudSeleccionada = MutableLiveData<SolicitudAdopcion>()
    val solicitudSeleccionada: LiveData<SolicitudAdopcion> = _solicitudSeleccionada

    /**
     * El Fragment llama a esto cuando el admin toca un item de la lista
     */
    fun seleccionarSolicitud(solicitud: SolicitudAdopcion) {
        _solicitudSeleccionada.value = solicitud
    }

    /**
     * Llamado por GestionarSolicitudDialog
     */
    fun rechazarSolicitud(solicitudId: String, notas: String) {
        viewModelScope.launch {
            val adminId = "admin_actual" // TODO: Reemplaza esto con el ID del admin logueado
            val result = adopcionRepository.rechazarSolicitud(solicitudId, adminId, notas)

            result.onSuccess {
                cargarSolicitudes() // Refresca la lista
            }
            result.onFailure {
                _error.value = it.message ?: "Error al rechazar"
            }
        }
    }

    /**
     * Llamado por GestionarSolicitudDialog
     */
    fun agendarEntrevista(solicitudId: String, fechaCita: Date, notas: String) {
        viewModelScope.launch {
            val adminId = "admin_actual" // TODO: Reemplaza esto con el ID del admin logueado
            val result = adopcionRepository.agendarEntrevista(solicitudId, fechaCita, notas, adminId)

            result.onSuccess {
                cargarSolicitudes() // Refresca la lista
            }
            result.onFailure {
                _error.value = it.message ?: "Error al agendar"
            }
        }
    }
}