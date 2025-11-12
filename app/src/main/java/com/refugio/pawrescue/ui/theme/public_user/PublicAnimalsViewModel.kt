package com.refugio.pawrescue.ui.theme.public_user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.refugio.pawrescue.data.model.Animal
import com.refugio.pawrescue.ui.theme.utils.Constants
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PublicAnimalsViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _animales = MutableLiveData<List<Animal>>()
    val animales: LiveData<List<Animal>> = _animales

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var todosLosAnimales: List<Animal> = emptyList()

    init {
        loadAnimalesDisponibles()
    }

    fun loadAnimalesDisponibles() {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val snapshot = firestore.collection(Constants.COLLECTION_ANIMALES)
                    .whereEqualTo("estadoAdopcion", Constants.ADOPCION_DISPONIBLE)
                    .whereEqualTo("activo", true)
                    .get()
                    .await()

                val animalesList = snapshot.documents.mapNotNull {
                    it.toObject(Animal::class.java)
                }

                todosLosAnimales = animalesList
                _animales.value = animalesList

            } catch (e: Exception) {
                e.printStackTrace()
                _animales.value = emptyList()
            } finally {
                _isLoading.value = false
            }
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