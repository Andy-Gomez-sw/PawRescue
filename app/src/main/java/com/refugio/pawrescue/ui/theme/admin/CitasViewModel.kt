package com.refugio.pawrescue.ui.theme.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.refugio.pawrescue.data.model.SolicitudAdopcion
import com.refugio.pawrescue.data.model.repository.AdopcionRepository
import kotlinx.coroutines.launch
import com.refugio.pawrescue.data.model.EstadoSolicitud

class CitasViewModel : ViewModel() {

    private val adopcionRepository = AdopcionRepository()

    private val _solicitudes = MutableLiveData<List<SolicitudAdopcion>>()
    val solicitudes: LiveData<List<SolicitudAdopcion>> = _solicitudes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Dejamos esta función por si la usas en otro lado
    fun cargarSolicitudes() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = adopcionRepository.getSolicitudes() // Esta no filtra
            result.onSuccess { lista ->
                _solicitudes.value = lista
                _error.value = null
            }.onFailure { exception ->
                _error.value = exception.message
                _solicitudes.value = emptyList()
            }
            _isLoading.value = false
        }
    }

    // --- FUNCIÓN MODIFICADA ---
    // Ahora también necesita el refugioId
    fun cargarSolicitudesByEstado(estado: EstadoSolicitud, refugioId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            // CORRECCIÓN: Pasamos el refugioId
            val result = adopcionRepository.getSolicitudesByEstado(estado, refugioId)

            result.onSuccess { lista ->
                _solicitudes.value = lista
                _error.value = null
            }.onFailure { exception ->
                _error.value = exception.message
                _solicitudes.value = emptyList()
            }
            _isLoading.value = false
        }
    }
    // --- FIN DE LA FUNCIÓN ---


    // (El resto de funciones se quedan igual)
    fun aprobarSolicitud(solicitudId: String, evaluadoPor: String) {
        viewModelScope.launch {
            val result = adopcionRepository.aprobarSolicitud(solicitudId, evaluadoPor)
            // ... (el resto de la función)
        }
    }

    fun rechazarSolicitud(solicitudId: String, evaluadoPor: String, motivo: String) {
        viewModelScope.launch {
            val result = adopcionRepository.rechazarSolicitud(solicitudId, evaluadoPor, motivo)
            // ... (el resto de la función)
        }
    }
}