package com.refugio.pawrescue.ui.theme.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.refugio.pawrescue.data.model.Animal
import com.refugio.pawrescue.data.model.SolicitudAdopcion // <-- AÑADIR IMPORT
import com.refugio.pawrescue.data.model.EstadoSolicitud // <-- AÑADIR IMPORT
import com.refugio.pawrescue.data.model.repository.AdopcionRepository // <-- AÑADIR IMPORT
import com.refugio.pawrescue.data.model.repository.AnimalRepository
import com.refugio.pawrescue.data.model.repository.CuidadoRepository
import kotlinx.coroutines.launch

data class DashboardStats(
    val activeAnimals: Int = 0,
    val completed: Int = 0,
    val pending: Int = 0,
    val alerts: Int = 0
)

class DashboardViewModel : ViewModel() {

    private val animalRepository = AnimalRepository()
    private val cuidadoRepository = CuidadoRepository()
    private val adopcionRepository = AdopcionRepository() // <-- AÑADIR REPOSITORIO

    // LiveData para Animales (para Voluntarios)
    private val _animales = MutableLiveData<List<Animal>>()
    val animales: LiveData<List<Animal>> = _animales

    // --- AÑADIR NUEVO LIVEDATA ---
    // LiveData para Solicitudes (para Admin)
    private val _solicitudesPendientes = MutableLiveData<List<SolicitudAdopcion>>()
    val solicitudesPendientes: LiveData<List<SolicitudAdopcion>> = _solicitudesPendientes
    // --- FIN DE LIVEDATA ---

    private val _stats = MutableLiveData<DashboardStats>()
    val stats: LiveData<DashboardStats> = _stats

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Esta función se queda igual (para Voluntarios)
    fun loadAnimales(voluntarioId: String, refugioId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = animalRepository.getAnimalesByVoluntario(voluntarioId, refugioId)
            result.onSuccess { animalesList ->
                _animales.value = animalesList
            }.onFailure {
                _animales.value = emptyList()
            }
            _isLoading.value = false
        }
    }

    // --- AÑADIR NUEVA FUNCIÓN ---
    fun loadSolicitudesPendientes(refugioId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            // Buscamos todas las solicitudes PENDIENTES del refugio
            // (Asumiendo que el Admin ve todas las del refugio)
            val result = adopcionRepository.getSolicitudesByEstado(EstadoSolicitud.PENDIENTE)

            result.onSuccess { solicitudesList ->
                // Filtramos por refugioId (si el repositorio no lo hace)
                // Si tu getSolicitudesByEstado ya filtra por refugio, puedes quitar el .filter
                _solicitudesPendientes.value = solicitudesList.filter { it.animalId.isNotEmpty() } // Un filtro simple para asegurar que tiene animal
            }.onFailure {
                _solicitudesPendientes.value = emptyList()
            }
            _isLoading.value = false
        }
    }
    // --- FIN DE NUEVA FUNCIÓN ---


    fun loadStats(voluntarioId: String) {
        // (Esta función se queda igual)
        viewModelScope.launch {
            val animalesResult = _animales.value?.size ?: 0
            val cuidadosResult = cuidadoRepository.getCuidadosPendientesByVoluntario(voluntarioId)

            cuidadosResult.onSuccess { cuidados ->
                val completed = cuidados.count { it.completado }
                val pending = cuidados.count { !it.completado }

                _stats.value = DashboardStats(
                    activeAnimals = animalesResult,
                    completed = completed,
                    pending = pending,
                    alerts = 0 // TODO: Calculate real alerts
                )
            }
        }
    }
}