package com.refugio.pawrescue.data.model.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.refugio.pawrescue.data.model.SolicitudAdopcion
import com.refugio.pawrescue.ui.theme.utils.Constants
import kotlinx.coroutines.tasks.await
import java.util.*

class AdopcionRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun guardarSolicitud(solicitud: SolicitudAdopcion): Result<String> {
        return try {
            val solicitudRef = if (solicitud.id.isEmpty()) {
                firestore.collection(Constants.COLLECTION_ADOPCIONES).document()
            } else {
                firestore.collection(Constants.COLLECTION_ADOPCIONES).document(solicitud.id)
            }

            val solicitudToSave = solicitud.copy(
                id = solicitudRef.id,
                fechaSolicitud = Date()
            )

            solicitudRef.set(solicitudToSave).await()
            Result.success(solicitudRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSolicitudes(): Result<List<SolicitudAdopcion>> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_ADOPCIONES)
                .orderBy("fechaSolicitud", Query.Direction.DESCENDING)
                .get()
                .await()

            val solicitudes = snapshot.documents.mapNotNull {
                it.toObject(SolicitudAdopcion::class.java)
            }
            Result.success(solicitudes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSolicitudesByEstado(estado: String): Result<List<SolicitudAdopcion>> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_ADOPCIONES)
                .whereEqualTo("estado", estado)
                .orderBy("fechaSolicitud", Query.Direction.DESCENDING)
                .get()
                .await()

            val solicitudes = snapshot.documents.mapNotNull {
                it.toObject(SolicitudAdopcion::class.java)
            }
            Result.success(solicitudes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun aprobarSolicitud(solicitudId: String, evaluadoPor: String): Result<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_ADOPCIONES)
                .document(solicitudId)
                .update(
                    mapOf(
                        "estado" to "aprobada",
                        "evaluadoPor" to evaluadoPor,
                        "fechaActualizacion" to Date()
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rechazarSolicitud(
        solicitudId: String,
        evaluadoPor: String,
        motivo: String
    ): Result<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_ADOPCIONES)
                .document(solicitudId)
                .update(
                    mapOf(
                        "estado" to "rechazada",
                        "evaluadoPor" to evaluadoPor,
                        "notasEvaluacion" to motivo,
                        "fechaActualizacion" to Date()
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSolicitudById(solicitudId: String): Result<SolicitudAdopcion> {
        return try {
            val document = firestore.collection(Constants.COLLECTION_ADOPCIONES)
                .document(solicitudId)
                .get()
                .await()

            val solicitud = document.toObject(SolicitudAdopcion::class.java)
                ?: throw Exception("Solicitud no encontrada")

            Result.success(solicitud)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}