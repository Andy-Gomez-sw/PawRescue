package com.refugio.pawrescue.data.model.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.refugio.pawrescue.data.model.SolicitudAdopcion
import com.refugio.pawrescue.data.model.EstadoSolicitud
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

            // CORREGIDO: Guardar 'estado' como String
            val solicitudToSave = solicitud.copy(
                id = solicitudRef.id,
                fechaSolicitud = Date(),
                estado = solicitud.estado, // El objeto
                estadoString = solicitud.estado.name.lowercase() // El string
            )

            solicitudRef.set(solicitudToSave).await()
            Result.success(solicitudRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSolicitudes(): Result<List<SolicitudAdopcion>> {
        // (Esta función se queda igual)
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

    // --- FUNCIÓN CRÍTICA CORREGIDA ---
    suspend fun getSolicitudesByEstado(estado: EstadoSolicitud, refugioId: String): Result<List<SolicitudAdopcion>> {
        return try {
            // Si no hay refugioId, no buscar nada (prevención)
            if (refugioId.isEmpty()) {
                return Result.success(emptyList())
            }

            val snapshot = firestore.collection(Constants.COLLECTION_ADOPCIONES)
                // CORRECCIÓN 1: Buscar por String (el .name del Enum)
                .whereEqualTo("estado", estado.name)
                // CORRECCIÓN 2: Filtrar por el Refugio del Admin
                .whereEqualTo("refugioId", refugioId)
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
    // --- FIN DE LA FUNCIÓN CRÍTICA ---


    // --- OTRAS FUNCIONES CORREGIDAS (Para que guarden String) ---

    suspend fun aprobarSolicitud(solicitudId: String, evaluadoPor: String): Result<Unit> {
        return try {
            val nuevoEstado = EstadoSolicitud.APROBADA
            firestore.collection(Constants.COLLECTION_ADOPCIONES)
                .document(solicitudId)
                .update(
                    mapOf(
                        "estado" to nuevoEstado.name, // <-- CORREGIDO a String
                        "estadoString" to nuevoEstado.name.lowercase(),
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
            val nuevoEstado = EstadoSolicitud.RECHAZADA
            firestore.collection(Constants.COLLECTION_ADOPCIONES)
                .document(solicitudId)
                .update(
                    mapOf(
                        "estado" to nuevoEstado.name, // <-- CORREGIDO a String
                        "estadoString" to nuevoEstado.name.lowercase(),
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

    suspend fun agendarEntrevista(
        solicitudId: String,
        fechaCita: Date,
        notas: String,
        evaluadoPor: String
    ): Result<Unit> {
        return try {
            val nuevoEstado = EstadoSolicitud.ENTREVISTA_PROGRAMADA
            val updates = mapOf(
                "estado" to nuevoEstado.name, // <-- CORREGIDO a String
                "estadoString" to nuevoEstado.name.lowercase(),
                "citaProgramada" to fechaCita,
                "notasEvaluacion" to notas,
                "evaluadoPor" to evaluadoPor,
                "fechaActualizacion" to Date()
            )
            firestore.collection(Constants.COLLECTION_ADOPCIONES)
                .document(solicitudId)
                .update(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- FIN DE FUNCIONES MODIFICADAS ---

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