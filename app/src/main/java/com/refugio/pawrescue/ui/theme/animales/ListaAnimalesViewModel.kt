package com.refugio.pawrescue.ui.theme.animales

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.refugio.pawrescue.data.model.Animal
import com.refugio.pawrescue.data.model.repository.AnimalRepository
import kotlinx.coroutines.launch

class ListaAnimalesViewModel : ViewModel() {

    private val animalRepository = AnimalRepository()

    private val _animales = MutableLiveData<List<Animal>>()
    val animales: LiveData<List<Animal>> = _animales

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var todosLosAnimales: List<Animal> = emptyList()

    fun loadAnimales(refugioId: String) {
        _isLoading.value = true

        viewModelScope.launch {
            val result = animalRepository.getAnimalesByRefugio(refugioId)

            result.onSuccess { animalesList ->
                todosLosAnimales = animalesList
                _animales.value = animalesList
            }.onFailure {
                _animales.value = emptyList()
            }

            _isLoading.value = false
        }
    }

    fun filtrarPorEspecie(especie: String?) {
        _animales.value = if (especie == null) {
            todosLosAnimales
        } else {
            todosLosAnimales.filter { it.especie == especie }
        }
    }
}