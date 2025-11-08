package com.refugio.pawrescue.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Cuidado(
    @DocumentId
    val id: String = "",
    val animalId: String = "",
    val tipo: String = "", // "alimentacion", "medicamento", "paseo", "revision", "limpieza"
    val descripcion: String = "",
    val completado: Boolean = false,
    val horaProgramada: Date? = null,
    val horaCompletado: Date? = null,
    val voluntarioId: String = "",
    val voluntarioNombre: String = "",
    val fotosEvidencia: List<String> = emptyList(),
    val observaciones: String = "",

    // Específico para alimentación
    val tipoAlimento: String? = null,
    val cantidad: Double? = null,
    val unidad: String? = null, // "gramos", "ml", "tazas"

    // Específico para medicamento
    val nombreMedicamento: String? = null,
    val dosis: String? = null,

    @ServerTimestamp
    val fechaCreacion: Date? = null
)