package com.refugio.pawrescue.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Rescate(
    val id: String = "",
    val animalId: String = "",
    val voluntarioId: String = "",
    val voluntarioNombre: String = "",
    val ubicacion: UbicacionRescate = UbicacionRescate(),
    val descripcion: String = "",
    val condicionInicial: String = "",
    val fotosRescate: List<String> = emptyList(),
    val videoRescate: String = "",
    val prioridad: String = "media",
    val necesitaVeterinario: Boolean = false,
    @ServerTimestamp
    val fechaRescate: Date? = null
)