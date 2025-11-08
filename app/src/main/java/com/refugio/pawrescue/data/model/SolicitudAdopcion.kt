package com.refugio.pawrescue.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

enum class EstadoSolicitud {
    PENDIENTE, APROBADA, RECHAZADA, ENTREVISTA_PROGRAMADA
}

@Entity(tableName = "solicitudes_adopcion")
data class SolicitudAdopcion(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val animalId: String,
    val nombreSolicitante: String,
    val emailSolicitante: String,
    val telefonoSolicitante: String,
    val direccionSolicitante: String,
    val tipoVivienda: String, // Casa, Departamento, etc.
    val tienePatio: Boolean,
    val tieneOtrasMascotas: Boolean,
    val detallesOtrasMascotas: String = "",
    val motivoAdopcion: String,
    val fechaSolicitud: Date = Date(),
    var estado: EstadoSolicitud = EstadoSolicitud.PENDIENTE,
    var notasAdmin: String = "",
    var synced: Boolean = false
)