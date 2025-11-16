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

    fun cargarSolicitudes() {
        _isLoading.value = true

        viewModelScope.launch {
            val result = adopcionRepository.getSolicitudes()

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

    fun cargarSolicitudesByEstado(estado: String) {
        _isLoading.value = true

        viewModelScope.launch {
            val result = adopcionRepository.getSolicitudesByEstado(EstadoSolicitud.ENTREVISTA_PROGRAMADA) // <-- SOLUCIÓN: Es un Enum

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

    fun aprobarSolicitud(solicitudId: String, evaluadoPor: String) {
        viewModelScope.launch {
            val result = adopcionRepository.aprobarSolicitud(solicitudId, evaluadoPor)

            result.onSuccess {
                cargarSolicitudes()
            }.onFailure { exception ->
                _error.value = exception.message
            }
        }
    }

    fun rechazarSolicitud(solicitudId: String, evaluadoPor: String, motivo: String) {
        viewModelScope.launch {
            val result = adopcionRepository.rechazarSolicitud(solicitudId, evaluadoPor, motivo)

            result.onSuccess {
                cargarSolicitudes()
            }.onFailure { exception ->
                _error.value = exception.message
            }
        }
    }
}