package com.refugio.pawrescue.data.model.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.refugio.pawrescue.data.model.Animal
import com.refugio.pawrescue.ui.theme.utils.Constants
import kotlinx.coroutines.tasks.await
import java.util.*

class AnimalRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun getAnimalesByRefugio(refugioId: String): Result<List<Animal>> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_ANIMALES)
                .whereEqualTo("refugioId", refugioId)
                .whereEqualTo("activo", true)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get()
                .await()

            val animales = snapshot.documents.mapNotNull { it.toObject(Animal::class.java) }
            Result.success(animales)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAnimalesByVoluntario(voluntarioId: String, refugioId: String): Result<List<Animal>> {
        return try {
            // Primero obtener los IDs de animales asignados al voluntario
            val usuarioDoc = firestore.collection(Constants.COLLECTION_USUARIOS)
                .document(voluntarioId)
                .get()
                .await()

            val animalesAsignados = usuarioDoc.get("animalesAsignados") as? List<String> ?: emptyList()

            if (animalesAsignados.isEmpty()) {
                return Result.success(emptyList())
            }

            // Obtener los animales (Firestore tiene límite de 10 items en whereIn)
            val animales = mutableListOf<Animal>()
            animalesAsignados.chunked(10).forEach { chunk ->
                val snapshot = firestore.collection(Constants.COLLECTION_ANIMALES)
                    .whereIn("id", chunk)
                    .whereEqualTo("activo", true)
                    .get()
                    .await()

                animales.addAll(snapshot.documents.mapNotNull { it.toObject(Animal::class.java) })
            }

            Result.success(animales)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAnimalById(animalId: String): Result<Animal> {
        return try {
            val document = firestore.collection(Constants.COLLECTION_ANIMALES)
                .document(animalId)
                .get()
                .await()

            val animal = document.toObject(Animal::class.java)
                ?: throw Exception("Animal no encontrado")

            Result.success(animal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveAnimal(animal: Animal): Result<String> {
        return try {
            val animalRef = if (animal.id.isEmpty()) {
                firestore.collection(Constants.COLLECTION_ANIMALES).document()
            } else {
                firestore.collection(Constants.COLLECTION_ANIMALES).document(animal.id)
            }

            val animalToSave = animal.copy(
                id = animalRef.id,
                fechaActualizacion = Date()
            )

            animalRef.set(animalToSave).await()
            Result.success(animalRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAnimal(animalId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            val updatesWithTimestamp = updates.toMutableMap().apply {
                put("fechaActualizacion", Date())
            }

            firestore.collection(Constants.COLLECTION_ANIMALES)
                .document(animalId)
                .update(updatesWithTimestamp)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadAnimalPhoto(animalId: String, imageUri: Uri): Result<String> {
        return try {
            val fileName = "animal_${animalId}_${System.currentTimeMillis()}.jpg"
            val storageRef = storage.reference
                .child(Constants.STORAGE_ANIMALES)
                .child(animalId)
                .child(fileName)

            storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await()

            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadAnimalPhotos(animalId: String, imageUris: List<Uri>): Result<List<String>> {
        return try {
            val urls = mutableListOf<String>()

            imageUris.forEach { uri ->
                val result = uploadAnimalPhoto(animalId, uri)
                result.getOrNull()?.let { urls.add(it) }
            }

            Result.success(urls)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAnimal(animalId: String): Result<Unit> {
        return try {
            // Soft delete - solo marcamos como inactivo
            firestore.collection(Constants.COLLECTION_ANIMALES)
                .document(animalId)
                .update(
                    mapOf(
                        "activo" to false,
                        "fechaActualizacion" to Date()
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchAnimales(
        refugioId: String,
        especie: String? = null,
        estadoAdopcion: String? = null
    ): Result<List<Animal>> {
        return try {
            var query: Query = firestore.collection(Constants.COLLECTION_ANIMALES)
                .whereEqualTo("refugioId", refugioId)
                .whereEqualTo("activo", true)

            especie?.let {
                query = query.whereEqualTo("especie", it)
            }

            estadoAdopcion?.let {
                query = query.whereEqualTo("estadoAdopcion", it)
            }

            val snapshot = query.get().await()
            val animales = snapshot.documents.mapNotNull { it.toObject(Animal::class.java) }

            Result.success(animales)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}