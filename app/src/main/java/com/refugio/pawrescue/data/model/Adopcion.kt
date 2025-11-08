package com.refugio.pawrescue.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date
import java.util.UUID

enum class EstadoSolicitud {
    PENDIENTE,
    APROBADA,
    RECHAZADA,
    ENTREVISTA_PROGRAMADA
}

// Versión para Room (Base de datos local)
@Entity(tableName = "solicitudes_adopcion")
data class SolicitudAdopcionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val animalId: String,
    val nombreSolicitante: String,
    val emailSolicitante: String,
    val telefonoSolicitante: String,
    val direccionSolicitante: String,
    val tipoVivienda: String,
    val tienePatio: Boolean,
    val tieneOtrasMascotas: Boolean,
    val detallesOtrasMascotas: String = "",
    val motivoAdopcion: String,
    val fechaSolicitud: Date = Date(),
    var estado: EstadoSolicitud = EstadoSolicitud.PENDIENTE,
    var notasAdmin: String = "",
    var synced: Boolean = false
)

// Versión para Firestore (Base de datos en la nube)
data class SolicitudAdopcion(
    val id: String = "",
    val animalId: String = "",
    val animalNombre: String = "",

    // Datos del solicitante (compatibilidad con ambas versiones)
    val solicitanteNombre: String = "",
    val nombreSolicitante: String = solicitanteNombre, // Alias
    val solicitanteEmail: String = "",
    val emailSolicitante: String = solicitanteEmail, // Alias
    val solicitanteTelefono: String = "",
    val telefonoSolicitante: String = solicitanteTelefono, // Alias
    val solicitanteEdad: Int = 0,
    val solicitanteDireccion: String = "",
    val direccionSolicitante: String = solicitanteDireccion, // Alias

    // Información del hogar
    val tipoVivienda: String = "",
    val tieneJardin: Boolean = false,
    val tienePatio: Boolean = tieneJardin, // Alias
    val espacioDisponible: String = "",
    val otrosAnimales: String = "",
    val tieneOtrasMascotas: Boolean = otrosAnimales.isNotEmpty(),
    val detallesOtrasMascotas: String = otrosAnimales,
    val numeroPersonas: Int = 0,
    val hayNinos: Boolean = false,
    val edadesNinos: String = "",

    // Situación económica
    val empleoEstable: Boolean = false,
    val ingresoMensual: String = "",
    val capacidadGastosVet: Boolean = false,

    // Experiencia
    val experienciaMascotas: String = "",
    val motivoAdopcion: String = "",

    // Documentos
    val documentosAdjuntos: List<String> = emptyList(),

    // Estado (manejado por enum o string según contexto)
    val estado: EstadoSolicitud = EstadoSolicitud.PENDIENTE,
    val estadoString: String = estado.name.lowercase(),
    val puntuacionAutomatica: Int = 0,
    val citaProgramada: Date? = null,

    @ServerTimestamp
    val fechaSolicitud: Date? = null,
    val evaluadoPor: String? = null,
    val notasEvaluacion: String = "",
    val notasAdmin: String = notasEvaluacion, // Alias

    var synced: Boolean = false
) {
    // Método de conversión para Entity
    fun toEntity(): SolicitudAdopcionEntity {
        return SolicitudAdopcionEntity(
            id = id,
            animalId = animalId,
            nombreSolicitante = solicitanteNombre,
            emailSolicitante = solicitanteEmail,
            telefonoSolicitante = solicitanteTelefono,
            direccionSolicitante = solicitanteDireccion,
            tipoVivienda = tipoVivienda,
            tienePatio = tieneJardin,
            tieneOtrasMascotas = otrosAnimales.isNotEmpty(),
            detallesOtrasMascotas = otrosAnimales,
            motivoAdopcion = motivoAdopcion,
            fechaSolicitud = fechaSolicitud ?: Date(),
            estado = estado,
            notasAdmin = notasEvaluacion,
            synced = synced
        )
    }
}

data class Seguimiento(
    val id: String = "",
    val animalId: String = "",
    val adopcionId: String = "",
    val adoptanteNombre: String = "",
    val fechaAdopcion: Date? = null,
    val reportes: List<ReporteSeguimiento> = emptyList(),
    val visitasDomiciliarias: List<VisitaDomiciliaria> = emptyList(),
    val estadoGeneral: String = "excelente"
)

data class ReporteSeguimiento(
    val fecha: Date = Date(),
    val semana: Int = 0,
    val descripcion: String = "",
    val fotos: List<String> = emptyList(),
    val calificacion: Int = 5
)

data class VisitaDomiciliaria(
    val fecha: Date = Date(),
    val realizadoPor: String = "",
    val observaciones: String = "",
    val fotos: List<String> = emptyList(),
    val aprobada: Boolean = true
)