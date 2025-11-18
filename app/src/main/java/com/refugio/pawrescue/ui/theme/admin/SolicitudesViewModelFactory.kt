package com.refugio.pawrescue.ui.theme.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.refugio.pawrescue.data.model.repository.AdopcionRepository

/**
 * Factory para crear una instancia de SolicitudesAdopcionViewModel.
 *
 * Esta clase es necesaria porque SolicitudesAdopcionViewModel tiene un constructor
 * que requiere una dependencia (AdopcionRepository), y el sistema de Android
 * no sabe cómo proporcionar esa dependencia por sí mismo.
 *
 * Esta factory le dice al sistema: "Oye, cuando necesites crear un
 * SolicitudesAdopcionViewModel, primero crea un AdopcionRepository y pásaselo".
 */
class SolicitudesViewModelFactory(
    private val adopcionRepository: AdopcionRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Comprueba si la clase que se pide crear es SolicitudesAdopcionViewModel
        if (modelClass.isAssignableFrom(SolicitudesAdopcionViewModel::class.java)) {
            // Si lo es, crea una instancia pasándole el repositorio y la devuelve.
            // La supresión de "UNCHECKED_CAST" es segura aquí por la comprobación anterior.
            @Suppress("UNCHECKED_CAST")
            return SolicitudesAdopcionViewModel(adopcionRepository) as T
        }
        // Si se pide crear cualquier otro tipo de ViewModel, lanza una excepción.
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
