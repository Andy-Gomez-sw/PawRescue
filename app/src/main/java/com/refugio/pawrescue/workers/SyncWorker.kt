package com.refugio.pawrescue.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.refugio.pawrescue.data.model.local.AppDatabase
import com.refugio.pawrescue.data.model.Animal
import com.refugio.pawrescue.data.model.Cuidado
import com.refugio.pawrescue.data.model.repository.AnimalRepository
import com.refugio.pawrescue.data.model.repository.CuidadoRepository
import com.refugio.pawrescue.ui.theme.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val TAG = "SyncWorker"
    private val database = AppDatabase.getDatabase(context)
    private val animalRepository = AnimalRepository()
    private val cuidadoRepository = CuidadoRepository()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {

        Log.d(TAG, "Iniciando sincronización...")

        // Verificar conexión a internet
        if (!NetworkUtils.isNetworkAvailable(applicationContext)) {
            Log.d(TAG, "Sin conexión a internet, reintentando más tarde")
            return@withContext Result.retry()
        }

        try {
            // Sincronizar animales no sincronizados
            val animalesSyncResult = syncAnimales()

            // Sincronizar cuidados no sincronizados
            val cuidadosSyncResult = syncCuidados()

            if (animalesSyncResult && cuidadosSyncResult) {
                Log.d(TAG, "Sincronización completada exitosamente")
                Result.success()
            } else {
                Log.d(TAG, "Sincronización parcial, algunos datos no se pudieron sincronizar")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en sincronización: ${e.message}", e)
            Result.failure()
        }
    }

    private suspend fun syncAnimales(): Boolean {
        return try {
            val animalesNoSincronizados = database.animalDao().getAnimalesNoSincronizados()

            Log.d(TAG, "Sincronizando ${animalesNoSincronizados.size} animales")

            animalesNoSincronizados.forEach { animalEntity ->
                // Convertir entity a model
                val animal = Animal(
                    id = animalEntity.id,
                    nombre = animalEntity.nombre,
                    especie = animalEntity.especie,
                    raza = animalEntity.raza,
                    edad = animalEntity.edad,
                    sexo = animalEntity.sexo,
                    peso = animalEntity.peso,
                    estadoSalud = animalEntity.estadoSalud,
                    vacunasCompletas = animalEntity.vacunasCompletas,
                    esterilizado = animalEntity.esterilizado,
                    fotosPrincipales = animalEntity.fotosPrincipales,
                    estadoAdopcion = animalEntity.estadoAdopcion,
                    fechaRescate = animalEntity.fechaRescate,
                    refugioId = animalEntity.refugioId
                )

                // Intentar subir a Firebase
                val result = animalRepository.saveAnimal(animal)

                result.onSuccess {
                    // Marcar como sincronizado en la BD local
                    database.animalDao().marcarComoSincronizado(animalEntity.id)
                    Log.d(TAG, "Animal ${animalEntity.id} sincronizado")
                }.onFailure { error ->
                    Log.e(TAG, "Error al sincronizar animal ${animalEntity.id}: ${error.message}")
                }
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error en syncAnimales: ${e.message}", e)
            false
        }
    }

    private suspend fun syncCuidados(): Boolean {
        return try {
            val cuidadosNoSincronizados = database.cuidadoDao().getCuidadosNoSincronizados()

            Log.d(TAG, "Sincronizando ${cuidadosNoSincronizados.size} cuidados")

            cuidadosNoSincronizados.forEach { cuidadoEntity ->
                // Convertir entity a model
                val cuidado = Cuidado(
                    id = cuidadoEntity.id,
                    animalId = cuidadoEntity.animalId,
                    tipo = cuidadoEntity.tipo,
                    descripcion = cuidadoEntity.descripcion,
                    completado = cuidadoEntity.completado,
                    horaProgramada = cuidadoEntity.horaProgramada,
                    horaCompletado = cuidadoEntity.horaCompletado,
                    voluntarioId = cuidadoEntity.voluntarioId,
                    fotosEvidencia = cuidadoEntity.fotosEvidencia,
                    observaciones = cuidadoEntity.observaciones
                )

                // Intentar subir a Firebase
                val result = cuidadoRepository.saveCuidado(cuidado)

                result.onSuccess {
                    // Marcar como sincronizado
                    database.cuidadoDao().marcarComoSincronizado(cuidadoEntity.id)
                    Log.d(TAG, "Cuidado ${cuidadoEntity.id} sincronizado")
                }.onFailure { error ->
                    Log.e(TAG, "Error al sincronizar cuidado ${cuidadoEntity.id}: ${error.message}")
                }
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error en syncCuidados: ${e.message}", e)
            false
        }
    }
}