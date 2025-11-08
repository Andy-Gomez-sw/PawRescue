package com.refugio.pawrescue.ui.theme.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.refugio.pawrescue.data.model.Usuario
import com.refugio.pawrescue.data.model.repository.AuthRepository
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val usuario: Usuario) : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _loginState = MutableLiveData<LoginState>(LoginState.Idle)
    val loginState: LiveData<LoginState> = _loginState

    fun login(email: String, password: String) {
        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            val result = authRepository.login(email, password)

            result.onSuccess { usuario ->
                _loginState.value = LoginState.Success(usuario)
            }.onFailure { exception ->
                _loginState.value = LoginState.Error(
                    exception.message ?: "Error al iniciar sesión"
                )
            }
        }
    }
}