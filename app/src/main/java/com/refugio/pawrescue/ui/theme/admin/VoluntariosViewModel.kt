package com.refugio.pawrescue.ui.theme.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.refugio.pawrescue.data.model.Usuario
import com.refugio.pawrescue.data.model.repository.VoluntariosRepository
import kotlinx.coroutines.launch

class VoluntariosViewModel : ViewModel() {

    private val voluntariosRepository = VoluntariosRepository()

    private val _voluntarios = MutableLiveData<List<Usuario>>()
    val voluntarios: LiveData<List<Usuario>> = _voluntarios

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun cargarVoluntarios(refugioId: String) {
        _isLoading.value = true

        viewModelScope.launch {
            val result = voluntariosRepository.getVoluntarios(refugioId)

            result.onSuccess { lista ->
                _voluntarios.value = lista
                _error.value = null
            }.onFailure { exception ->
                _error.value = exception.message
                _voluntarios.value = emptyList()
            }

            _isLoading.value = false
        }
    }

    fun cargarVoluntariosByRol(refugioId: String, rol: String) {
        _isLoading.value = true

        viewModelScope.launch {
            val result = voluntariosRepository.getVoluntariosByRol(refugioId, rol)

            result.onSuccess { lista ->
                _voluntarios.value = lista
                _error.value = null
            }.onFailure { exception ->
                _error.value = exception.message
                _voluntarios.value = emptyList()
            }

            _isLoading.value = false
        }
    }

    fun cargarVoluntariosActivos(refugioId: String) {
        _isLoading.value = true

        viewModelScope.launch {
            val result = voluntariosRepository.getVoluntariosActivos(refugioId)

            result.onSuccess { lista ->
                _voluntarios.value = lista
                _error.value = null
            }.onFailure { exception ->
                _error.value = exception.message
                _voluntarios.value = emptyList()
            }

            _isLoading.value = false
        }
    }

    fun actualizarVoluntario(voluntarioId: String, updates: Map<String, Any>) {
        viewModelScope.launch {
            val result = voluntariosRepository.actualizarVoluntario(voluntarioId, updates)

            result.onSuccess {
                // Recargar lista
                val refugioId = _voluntarios.value?.firstOrNull()?.refugioId ?: ""
                if (refugioId.isNotEmpty()) {
                    cargarVoluntarios(refugioId)
                }
            }.onFailure { exception ->
                _error.value = exception.message
            }
        }
    }

    fun toggleEstadoVoluntario(voluntarioId: String, activo: Boolean) {
        viewModelScope.launch {
            val result = voluntariosRepository.toggleEstadoVoluntario(voluntarioId, activo)

            result.onSuccess {
                // Recargar lista
                val refugioId = _voluntarios.value?.firstOrNull()?.refugioId ?: ""
                if (refugioId.isNotEmpty()) {
                    cargarVoluntarios(refugioId)
                }
            }.onFailure { exception ->
                _error.value = exception.message
            }
        }
    }

    fun asignarAnimal(voluntarioId: String, animalId: String) {
        viewModelScope.launch {
            val result = voluntariosRepository.asignarAnimal(voluntarioId, animalId)

            result.onSuccess {
                // Recargar lista
                val refugioId = _voluntarios.value?.firstOrNull()?.refugioId ?: ""
                if (refugioId.isNotEmpty()) {
                    cargarVoluntarios(refugioId)
                }
            }.onFailure { exception ->
                _error.value = exception.message
            }
        }
    }

    fun desasignarAnimal(voluntarioId: String, animalId: String) {
        viewModelScope.launch {
            val result = voluntariosRepository.desasignarAnimal(voluntarioId, animalId)

            result.onSuccess {
                // Recargar lista
                val refugioId = _voluntarios.value?.firstOrNull()?.refugioId ?: ""
                if (refugioId.isNotEmpty()) {
                    cargarVoluntarios(refugioId)
                }
            }.onFailure { exception ->
                _error.value = exception.message
            }
        }
    }
}