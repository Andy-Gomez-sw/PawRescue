package com.refugio.pawrescue.data.model.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.refugio.pawrescue.data.model.Cuidado
import com.refugio.pawrescue.ui.theme.utils.Constants
import kotlinx.coroutines.tasks.await
import java.util.*

class CuidadoRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun getCuidadosByAnimal(animalId: String): Result<List<Cuidado>> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_CUIDADOS)
                .whereEqualTo("animalId", animalId)
                .orderBy("horaProgramada", Query.Direction.ASCENDING)
                .get()
                .await()

            val cuidados = snapshot.documents.mapNotNull { it.toObject(Cuidado::class.java) }
            Result.success(cuidados)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCuidadosByAnimalAndDate(animalId: String, date: Date): Result<List<Cuidado>> {
        return try {
            val calendar = Calendar.getInstance().apply {
                time = date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            val startOfDay = calendar.time

            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val endOfDay = calendar.time

            val snapshot = firestore.collection(Constants.COLLECTION_CUIDADOS)
                .whereEqualTo("animalId", animalId)
                .whereGreaterThanOrEqualTo("horaProgramada", startOfDay)
                .whereLessThan("horaProgramada", endOfDay)
                .orderBy("horaProgramada", Query.Direction.ASCENDING)
                .get()
                .await()

            val cuidados = snapshot.documents.mapNotNull { it.toObject(Cuidado::class.java) }
            Result.success(cuidados)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCuidadosPendientesByVoluntario(voluntarioId: String): Result<List<Cuidado>> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_CUIDADOS)
                .whereEqualTo("voluntarioId", voluntarioId)
                .whereEqualTo("completado", false)
                .orderBy("horaProgramada", Query.Direction.ASCENDING)
                .get()
                .await()

            val cuidados = snapshot.documents.mapNotNull { it.toObject(Cuidado::class.java) }
            Result.success(cuidados)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveCuidado(cuidado: Cuidado): Result<String> {
        return try {
            val cuidadoRef = if (cuidado.id.isEmpty()) {
                firestore.collection(Constants.COLLECTION_CUIDADOS).document()
            } else {
                firestore.collection(Constants.COLLECTION_CUIDADOS).document(cuidado.id)
            }

            val cuidadoToSave = cuidado.copy(id = cuidadoRef.id)
            cuidadoRef.set(cuidadoToSave).await()

            Result.success(cuidadoRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completarCuidado(
        cuidadoId: String,
        observaciones: String = "",
        fotosEvidencia: List<String> = emptyList()
    ): Result<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_CUIDADOS)
                .document(cuidadoId)
                .update(
                    mapOf(
                        "completado" to true,
                        "horaCompletado" to Date(),
                        "observaciones" to observaciones,
                        "fotosEvidencia" to fotosEvidencia
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadEvidenciaPhoto(cuidadoId: String, imageUri: Uri): Result<String> {
        return try {
            val fileName = "cuidado_${cuidadoId}_${System.currentTimeMillis()}.jpg"
            val storageRef = storage.reference
                .child("cuidados")
                .child(cuidadoId)
                .child(fileName)

            storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await()

            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEstadisticasCuidados(animalId: String, dias: Int = 7): Result<Map<String, Int>> {
        return try {
            val calendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, -dias)
            }
            val fechaInicio = calendar.time

            val snapshot = firestore.collection(Constants.COLLECTION_CUIDADOS)
                .whereEqualTo("animalId", animalId)
                .whereGreaterThanOrEqualTo("horaProgramada", fechaInicio)
                .get()
                .await()

            val cuidados = snapshot.documents.mapNotNull { it.toObject(Cuidado::class.java) }

            val stats = mapOf(
                "total" to cuidados.size,
                "completados" to cuidados.count { it.completado },
                "pendientes" to cuidados.count { !it.completado }
            )

            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCuidado(cuidadoId: String): Result<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_CUIDADOS)
                .document(cuidadoId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}