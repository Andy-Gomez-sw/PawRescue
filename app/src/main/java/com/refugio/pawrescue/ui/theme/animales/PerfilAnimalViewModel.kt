package com.refugio.pawrescue.ui.theme.animales

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.refugio.pawrescue.data.model.Animal
import com.refugio.pawrescue.data.model.repository.AnimalRepository
import kotlinx.coroutines.launch

class PerfilAnimalViewModel : ViewModel() {

    private val animalRepository = AnimalRepository()

    private val _animal = MutableLiveData<Animal?>()
    val animal: LiveData<Animal?> = _animal

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadAnimal(animalId: String) {
        _isLoading.value = true

        viewModelScope.launch {
            val result = animalRepository.getAnimalById(animalId)

            result.onSuccess { animalData ->
                _animal.value = animalData
            }.onFailure {
                _animal.value = null
            }

            _isLoading.value = false
        }
    }
}