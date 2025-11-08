package com.refugio.pawrescue.ui.theme.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.refugio.pawrescue.data.model.repository.DonacionesRepository
import kotlinx.coroutines.launch

class DonacionesViewModel : ViewModel() {

    private val donacionesRepository = DonacionesRepository()

    private val _transacciones = MutableLiveData<List<Transaccion>>()
    val transacciones: LiveData<List<Transaccion>> = _transacciones

    private val _balance = MutableLiveData<Map<String, Double>>()
    val balance: LiveData<Map<String, Double>> = _balance

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun cargarTransacciones() {
        _isLoading.value = true

        viewModelScope.launch {
            val result = donacionesRepository.getTransacciones()

            result.onSuccess { lista ->
                _transacciones.value = lista
                _error.value = null
                calcularBalance()
            }.onFailure { exception ->
                _error.value = exception.message
                _transacciones.value = emptyList()
            }

            _isLoading.value = false
        }
    }

    fun cargarTransaccionesByTipo(tipo: String) {
        _isLoading.value = true

        viewModelScope.launch {
            val result = donacionesRepository.getTransaccionesByTipo(tipo)

            result.onSuccess { lista ->
                _transacciones.value = lista
                _error.value = null
            }.onFailure { exception ->
                _error.value = exception.message
                _transacciones.value = emptyList()
            }

            _isLoading.value = false
        }
    }

    private fun calcularBalance() {
        viewModelScope.launch {
            val result = donacionesRepository.getBalance()

            result.onSuccess { balanceData ->
                _balance.value = balanceData
            }.onFailure { exception ->
                _error.value = exception.message
            }
        }
    }

    fun guardarTransaccion(transaccion: Transaccion) {
        viewModelScope.launch {
            val result = donacionesRepository.guardarTransaccion(transaccion)

            result.onSuccess {
                cargarTransacciones()
            }.onFailure { exception ->
                _error.value = exception.message
            }
        }
    }

    fun eliminarTransaccion(transaccionId: String) {
        viewModelScope.launch {
            val result = donacionesRepository.eliminarTransaccion(transaccionId)

            result.onSuccess {
                cargarTransacciones()
            }.onFailure { exception ->
                _error.value = exception.message
            }
        }
    }
}