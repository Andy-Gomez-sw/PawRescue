package com.refugio.pawrescue.data.model.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.refugio.pawrescue.data.model.Usuario
import com.refugio.pawrescue.ui.theme.utils.Constants
import kotlinx.coroutines.tasks.await
import java.util.Date

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun login(email: String, password: String): Result<Usuario> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: throw Exception("User ID not found")

            val usuario = getUserData(userId)

            // Actualizar última conexión
            updateLastConnection(userId)

            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserData(userId: String): Usuario {
        return try {
            val document = firestore.collection(Constants.COLLECTION_USUARIOS)
                .document(userId)
                .get()
                .await()

            document.toObject(Usuario::class.java) ?: throw Exception("Usuario no encontrado")
        } catch (e: Exception) {
            throw Exception("Error al obtener datos del usuario: ${e.message}")
        }
    }

    private suspend fun updateLastConnection(userId: String) {
        try {
            firestore.collection(Constants.COLLECTION_USUARIOS)
                .document(userId)
                .update("ultimaConexion", Date())
                .await()
        } catch (e: Exception) {
            // No crashear la app si esto falla
            e.printStackTrace()
        }
    }

    suspend fun register(
        email: String,
        password: String,
        nombre: String,
        telefono: String,
        refugioId: String
    ): Result<Usuario> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: throw Exception("User ID not found")

            val usuario = Usuario(
                id = userId,
                nombre = nombre,
                email = email,
                telefono = telefono,
                refugioId = refugioId,
                rol = Constants.ROL_VOLUNTARIO, // Asigna rol por defecto
                fechaRegistro = Date(),
                ultimaConexion = Date(),
                activo = true
            )

            // Guardar usuario en Firestore
            firestore.collection(Constants.COLLECTION_USUARIOS)
                .document(userId)
                .set(usuario)
                .await()

            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }
}

