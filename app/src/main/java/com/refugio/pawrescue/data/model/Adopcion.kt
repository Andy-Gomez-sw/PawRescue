package com.refugio.pawrescue.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class SolicitudAdopcion(
    val id: String = "",
    val animalId: String = "",
    val animalNombre: String = "",

    // Datos del solicitante
    val solicitanteNombre: String = "",
    val solicitanteEmail: String = "",
    val solicitanteTelefono: String = "",
    val solicitanteEdad: Int = 0,
    val solicitanteDireccion: String = "",

    // Información del hogar
    val tipoVivienda: String = "", // "casa_propia", "casa_renta", "departamento"
    val tieneJardin: Boolean = false,
    val espacioDisponible: String = "",
    val otrosAnimales: String = "",
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

    // Estado
    val estado: String = "pendiente", // "pendiente", "aprobada", "rechazada", "en_revision"
    val puntuacionAutomatica: Int = 0,
    val citaProgramada: Date? = null,

    @ServerTimestamp
    val fechaSolicitud: Date? = null,
    val evaluadoPor: String? = null,
    val notasEvaluacion: String = ""
)

data class Seguimiento(
    val id: String = "",
    val animalId: String = "",
    val adopcionId: String = "",
    val adoptanteNombre: String = "",
    val fechaAdopcion: Date? = null,
    val reportes: List<ReporteSeguimiento> = emptyList(),
    val visitasDomiciliarias: List<VisitaDomiciliaria> = emptyList(),
    val estadoGeneral: String = "excelente" // "excelente", "bueno", "regular", "problemas"
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
