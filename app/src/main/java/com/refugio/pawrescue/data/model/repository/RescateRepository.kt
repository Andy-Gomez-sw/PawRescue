package com.refugio.pawrescue.data.model.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.refugio.pawrescue.data.model.Rescate
import com.refugio.pawrescue.ui.theme.utils.Constants
import kotlinx.coroutines.tasks.await

class RescateRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun saveRescate(rescate: Rescate): Result<String> {
        return try {
            val rescateRef = if (rescate.id.isEmpty()) {
                firestore.collection(Constants.COLLECTION_RESCATES).document()
            } else {
                firestore.collection(Constants.COLLECTION_RESCATES).document(rescate.id)
            }

            val rescateToSave = rescate.copy(id = rescateRef.id)
            rescateRef.set(rescateToSave).await()

            Result.success(rescateRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRescatesByVoluntario(voluntarioId: String): Result<List<Rescate>> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_RESCATES)
                .whereEqualTo("voluntarioId", voluntarioId)
                .orderBy("fechaRescate", Query.Direction.DESCENDING)
                .get()
                .await()

            val rescates = snapshot.documents.mapNotNull { it.toObject(Rescate::class.java) }
            Result.success(rescates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadRescatePhoto(rescateId: String, imageUri: Uri): Result<String> {
        return try {
            val fileName = "rescate_${rescateId}_${System.currentTimeMillis()}.jpg"
            val storageRef = storage.reference
                .child(Constants.STORAGE_RESCATES)
                .child(rescateId)
                .child(fileName)

            storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await()

            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadRescateVideo(rescateId: String, videoUri: Uri): Result<String> {
        return try {
            val fileName = "rescate_video_${rescateId}_${System.currentTimeMillis()}.mp4"
            val storageRef = storage.reference
                .child(Constants.STORAGE_RESCATES)
                .child(rescateId)
                .child(fileName)

            storageRef.putFile(videoUri).await()
            val downloadUrl = storageRef.downloadUrl.await()

            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}