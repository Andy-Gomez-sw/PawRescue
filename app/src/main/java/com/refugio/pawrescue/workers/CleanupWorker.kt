package com.refugio.pawrescue.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.refugio.pawrescue.data.model.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class CleanupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val TAG = "CleanupWorker"
    private val database = AppDatabase.getDatabase(context)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {

        Log.d(TAG, "Iniciando limpieza de datos antiguos...")

        try {
            // Limpiar cuidados de más de 30 días
            val calendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, -30)
            }
            val fechaLimite = calendar.time

            database.cuidadoDao().deleteCuidadosAntiguos(fechaLimite)

            Log.d(TAG, "Limpieza completada")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error en limpieza: ${e.message}", e)
            Result.failure()
        }
    }
}