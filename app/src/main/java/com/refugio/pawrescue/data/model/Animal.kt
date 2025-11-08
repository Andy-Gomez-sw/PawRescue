package com.refugio.pawrescue.data.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Animal(
    @DocumentId
    val id: String = "",
    val nombre: String = "",
    val especie: String = "", // "perro", "gato", "otro"
    val raza: String = "",
    val edad: String = "", // "cachorro", "adulto", "vejez"
    val sexo: String = "", // "macho", "hembra", "desconocido"
    val peso: Double = 0.0,
    val temperamento: String = "",
    val estadoSalud: String = "bueno", // "bueno", "regular", "malo"
    val vacunasCompletas: Boolean = false,
    val esterilizado: Boolean = false,
    val desparasitado: Boolean = false,
    val fotosPrincipales: List<String> = emptyList(),
    val videos: List<String> = emptyList(),
    val estadoAdopcion: String = "disponible", // "disponible", "en_proceso", "adoptado"
    val condicionesEspeciales: List<String> = emptyList(),

    // Información del rescate
    val fechaRescate: Date? = null,
    val ubicacionRescate: UbicacionRescate? = null,
    val rescatadoPor: String = "", // ID del voluntario
    val notasRescate: String = "",
    val prioridad: String = "media", // "baja", "media", "alta"

    // Metadata
    @ServerTimestamp
    val fechaCreacion: Date? = null,
    @ServerTimestamp
    val fechaActualizacion: Date? = null,
    val refugioId: String = "",
    val activo: Boolean = true
) : Parcelable

@Parcelize
data class UbicacionRescate(
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val direccion: String = "",
    val ciudad: String = "",
    val estado: String = ""
) : Parcelable