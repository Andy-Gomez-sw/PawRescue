package com.refugio.pawrescue.workers

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.refugio.pawrescue.data.model.repository.AnimalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UploadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val TAG = "UploadWorker"
    private val animalRepository = AnimalRepository()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {

        val animalId = inputData.getString(KEY_ANIMAL_ID) ?: return@withContext Result.failure()
        val photoUriString = inputData.getString(KEY_PHOTO_URI) ?: return@withContext Result.failure()

        Log.d(TAG, "Subiendo foto para animal: $animalId")

        try {
            val photoUri = Uri.parse(photoUriString)
            val result = animalRepository.uploadAnimalPhoto(animalId, photoUri)

            result.onSuccess { downloadUrl ->
                Log.d(TAG, "Foto subida exitosamente: $downloadUrl")

                val outputData = workDataOf(
                    KEY_DOWNLOAD_URL to downloadUrl
                )

                Result.success(outputData)
            }.onFailure { error ->
                Log.e(TAG, "Error al subir foto: ${error.message}")
                Result.retry()
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error en UploadWorker: ${e.message}", e)
            Result.failure()
        }
    }

    companion object {
        const val KEY_ANIMAL_ID = "animal_id"
        const val KEY_PHOTO_URI = "photo_uri"
        const val KEY_DOWNLOAD_URL = "download_url"
    }
}