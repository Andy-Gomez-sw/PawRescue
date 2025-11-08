package com.refugio.pawrescue.data.model.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.refugio.pawrescue.ui.theme.admin.Transaccion
import kotlinx.coroutines.tasks.await
import java.util.*

class DonacionesRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun guardarTransaccion(transaccion: Transaccion): Result<String> {
        return try {
            val transaccionRef = if (transaccion.id.isEmpty()) {
                firestore.collection("transacciones").document()
            } else {
                firestore.collection("transacciones").document(transaccion.id)
            }

            val transaccionToSave = transaccion.copy(
                id = transaccionRef.id,
                fecha = Date()
            )

            transaccionRef.set(transaccionToSave).await()
            Result.success(transaccionRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTransacciones(): Result<List<Transaccion>> {
        return try {
            val snapshot = firestore.collection("transacciones")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .await()

            val transacciones = snapshot.documents.mapNotNull {
                it.toObject(Transaccion::class.java)
            }
            Result.success(transacciones)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTransaccionesByTipo(tipo: String): Result<List<Transaccion>> {
        return try {
            val snapshot = firestore.collection("transacciones")
                .whereEqualTo("tipo", tipo)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .await()

            val transacciones = snapshot.documents.mapNotNull {
                it.toObject(Transaccion::class.java)
            }
            Result.success(transacciones)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTransaccionesByPeriodo(
        fechaInicio: Date,
        fechaFin: Date
    ): Result<List<Transaccion>> {
        return try {
            val snapshot = firestore.collection("transacciones")
                .whereGreaterThanOrEqualTo("fecha", fechaInicio)
                .whereLessThanOrEqualTo("fecha", fechaFin)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .await()

            val transacciones = snapshot.documents.mapNotNull {
                it.toObject(Transaccion::class.java)
            }
            Result.success(transacciones)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun eliminarTransaccion(transaccionId: String): Result<Unit> {
        return try {
            firestore.collection("transacciones")
                .document(transaccionId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun actualizarTransaccion(
        transaccionId: String,
        updates: Map<String, Any>
    ): Result<Unit> {
        return try {
            firestore.collection("transacciones")
                .document(transaccionId)
                .update(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBalance(): Result<Map<String, Double>> {
        return try {
            val snapshot = firestore.collection("transacciones")
                .get()
                .await()

            val transacciones = snapshot.documents.mapNotNull {
                it.toObject(Transaccion::class.java)
            }

            val ingresos = transacciones
                .filter { it.tipo == "ingreso" }
                .sumOf { it.monto }

            val egresos = transacciones
                .filter { it.tipo == "egreso" }
                .sumOf { it.monto }

            val balance = ingresos - egresos

            Result.success(
                mapOf(
                    "ingresos" to ingresos,
                    "egresos" to egresos,
                    "balance" to balance
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}