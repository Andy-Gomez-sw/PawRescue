package com.refugio.pawrescue.ui.theme.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.refugio.pawrescue.data.model.repository.VoluntariosRepository

class VoluntariosViewModelFactory(
    private val repository: VoluntariosRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VoluntariosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VoluntariosViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}