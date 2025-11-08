package com.refugio.pawrescue.ui.theme.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.refugio.pawrescue.data.model.Animal
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

    private val _animales = MutableLiveData<List<Animal>>()
    val animales: LiveData<List<Animal>> = _animales

    private val _stats = MutableLiveData<DashboardStats>()
    val stats: LiveData<DashboardStats> = _stats

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

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

    fun loadStats(voluntarioId: String) {
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