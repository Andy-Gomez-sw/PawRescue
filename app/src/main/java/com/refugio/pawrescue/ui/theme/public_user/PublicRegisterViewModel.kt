package com.refugio.pawrescue.ui.theme.public_user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.refugio.pawrescue.data.model.Usuario
import com.refugio.pawrescue.ui.theme.utils.Constants
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val userId: String) : RegisterState()
    data class Error(val message: String) : RegisterState()
}

class PublicRegisterViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _registerState = MutableLiveData<RegisterState>(RegisterState.Idle)
    val registerState: LiveData<RegisterState> = _registerState

    fun register(
        nombre: String,
        email: String,
        password: String,
        telefono: String
    ) {
        _registerState.value = RegisterState.Loading

        viewModelScope.launch {
            try {
                // 1. Crear usuario en Firebase Auth
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val userId = result.user?.uid ?: throw Exception("Error al crear usuario")

                // 2. Crear documento de usuario público en Firestore
                val usuarioPublico = Usuario(
                    id = userId,
                    nombre = nombre,
                    email = email,
                    telefono = telefono,
                    rol = "publico", // Rol especial para usuarios que solo adoptan
                    refugioId = "", // No pertenece a ningún refugio
                    activo = true,
                    fechaRegistro = Date(),
                    ultimaConexion = Date()
                )

                firestore.collection(Constants.COLLECTION_USUARIOS)
                    .document(userId)
                    .set(usuarioPublico)
                    .await()

                _registerState.value = RegisterState.Success(userId)

            } catch (e: Exception) {
                _registerState.value = RegisterState.Error(
                    e.message ?: "Error al registrar usuario"
                )
            }
        }
    }
}