package com.refugio.pawrescue.ui.theme.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.refugio.pawrescue.data.model.Usuario
import com.refugio.pawrescue.data.model.repository.VoluntariosRepository
import kotlinx.coroutines.launch

// --- MODIFICACIÓN CLAVE AQUÍ ---
// Ahora acepta el repositorio como un parámetro
class VoluntariosViewModel(
    private val voluntariosRepository: VoluntariosRepository
) : ViewModel() {

    // --- FIN DE LA MODIFICACIÓN ---

    // ELIMINAMOS ESTA LÍNEA: private val voluntariosRepository = VoluntariosRepository()

    private val _voluntarios = MutableLiveData<List<Usuario>>()
    val voluntarios: LiveData<List<Usuario>> = _voluntarios

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // El resto de tu código estaba perfecto y se queda igual
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
        // ... (tu código se queda igual)
    }

    fun cargarVoluntariosActivos(refugioId: String) {
        // ... (tu código se queda igual)
    }

    fun actualizarVoluntario(voluntarioId: String, updates: Map<String, Any>) {
        // ... (tu código se queda igual)
    }

    fun toggleEstadoVoluntario(voluntarioId: String, activo: Boolean) {
        // ... (tu código se queda igual)
    }

    fun asignarAnimal(voluntarioId: String, animalId: String) {
        // ... (tu código se queda igual)
    }

    fun desasignarAnimal(voluntarioId: String, animalId: String) {
        // ... (tu código se queda igual)
    }
}