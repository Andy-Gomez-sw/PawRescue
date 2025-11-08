package com.refugio.pawrescue.data.model.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.refugio.pawrescue.data.model.Usuario
import com.refugio.pawrescue.ui.theme.utils.Constants
import kotlinx.coroutines.tasks.await
import java.util.*

class VoluntariosRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getVoluntarios(refugioId: String): Result<List<Usuario>> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_USUARIOS)
                .whereEqualTo("refugioId", refugioId)
                .orderBy("nombre", Query.Direction.ASCENDING)
                .get()
                .await()

            val voluntarios = snapshot.documents.mapNotNull {
                it.toObject(Usuario::class.java)
            }
            Result.success(voluntarios)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVoluntariosByRol(refugioId: String, rol: String): Result<List<Usuario>> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_USUARIOS)
                .whereEqualTo("refugioId", refugioId)
                .whereEqualTo("rol", rol)
                .orderBy("nombre", Query.Direction.ASCENDING)
                .get()
                .await()

            val voluntarios = snapshot.documents.mapNotNull {
                it.toObject(Usuario::class.java)
            }
            Result.success(voluntarios)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVoluntariosActivos(refugioId: String): Result<List<Usuario>> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_USUARIOS)
                .whereEqualTo("refugioId", refugioId)
                .whereEqualTo("activo", true)
                .orderBy("nombre", Query.Direction.ASCENDING)
                .get()
                .await()

            val voluntarios = snapshot.documents.mapNotNull {
                it.toObject(Usuario::class.java)
            }
            Result.success(voluntarios)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun actualizarVoluntario(
        voluntarioId: String,
        updates: Map<String, Any>
    ): Result<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_USUARIOS)
                .document(voluntarioId)
                .update(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleEstadoVoluntario(voluntarioId: String, activo: Boolean): Result<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_USUARIOS)
                .document(voluntarioId)
                .update("activo", activo)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun asignarAnimal(voluntarioId: String, animalId: String): Result<Unit> {
        return try {
            val voluntario = firestore.collection(Constants.COLLECTION_USUARIOS)
                .document(voluntarioId)
                .get()
                .await()
                .toObject(Usuario::class.java)

            val animalesActuales = voluntario?.animalesAsignados ?: emptyList()
            val nuevosAnimales = animalesActuales + animalId

            firestore.collection(Constants.COLLECTION_USUARIOS)
                .document(voluntarioId)
                .update("animalesAsignados", nuevosAnimales)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun desasignarAnimal(voluntarioId: String, animalId: String): Result<Unit> {
        return try {
            val voluntario = firestore.collection(Constants.COLLECTION_USUARIOS)
                .document(voluntarioId)
                .get()
                .await()
                .toObject(Usuario::class.java)

            val animalesActuales = voluntario?.animalesAsignados ?: emptyList()
            val nuevosAnimales = animalesActuales.filter { it != animalId }

            firestore.collection(Constants.COLLECTION_USUARIOS)
                .document(voluntarioId)
                .update("animalesAsignados", nuevosAnimales)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEstadisticasVoluntario(voluntarioId: String): Result<Map<String, Int>> {
        return try {
            // Obtener rescates
            val rescatesSnapshot = firestore.collection(Constants.COLLECTION_RESCATES)
                .whereEqualTo("voluntarioId", voluntarioId)
                .get()
                .await()

            // Obtener cuidados completados
            val cuidadosSnapshot = firestore.collection(Constants.COLLECTION_CUIDADOS)
                .whereEqualTo("voluntarioId", voluntarioId)
                .whereEqualTo("completado", true)
                .get()
                .await()

            // Obtener animales asignados
            val voluntario = firestore.collection(Constants.COLLECTION_USUARIOS)
                .document(voluntarioId)
                .get()
                .await()
                .toObject(Usuario::class.java)

            Result.success(
                mapOf(
                    "rescates" to rescatesSnapshot.size(),
                    "cuidados" to cuidadosSnapshot.size(),
                    "animalesAsignados" to (voluntario?.animalesAsignados?.size ?: 0)
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}