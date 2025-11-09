package com.refugio.pawrescue.ui.theme.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.refugio.pawrescue.data.model.repository.DonacionesRepository // Asegúrate de que este import sea correcto según donde tengas tu repositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel // <-- ESTO YA ESTABA CORRECTO
class DonacionesViewModel @Inject constructor( // <-- ESTO YA ESTABA CORRECTO
    private val repository: DonacionesRepository // Hilt inyectará esto automáticamente
) : ViewModel() {

    private val _transacciones = MutableLiveData<List<Transaccion>>()
    val transacciones: LiveData<List<Transaccion>> = _transacciones

    private val _balance = MutableLiveData<Map<String, Double>>()
    val balance: LiveData<Map<String, Double>> = _balance

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        cargarTransacciones()
    }

    fun cargarTransacciones() {
        viewModelScope.launch {
            _isLoading.value = true
            // Aquí asumo que tu repositorio tiene métodos suspendidos que devuelven Result
            val result = repository.getTransacciones()
            result.onSuccess { lista ->
                _transacciones.value = lista
                actualizarBalance()
            }.onFailure { e ->
                _error.value = e.message
            }
            _isLoading.value = false
        }
    }

    private fun actualizarBalance() {
        viewModelScope.launch {
            val result = repository.getBalance()
            result.onSuccess { bal ->
                _balance.value = bal
            }
        }
    }

    fun eliminarTransaccion(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.eliminarTransaccion(id)
            result.onSuccess {
                cargarTransacciones() // Recargar la lista
            }.onFailure { e ->
                _error.value = "Error al eliminar: ${e.message}"
            }
            _isLoading.value = false
        }
    }

    // Agrega aquí otros métodos que necesites, como guardarTransaccion si la lógica está aquí
}