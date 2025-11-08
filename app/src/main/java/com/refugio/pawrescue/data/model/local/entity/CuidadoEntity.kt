package com.refugio.pawrescue.data.model.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "cuidados")
data class CuidadoEntity(
    @PrimaryKey
    val id: String,
    val animalId: String,
    val tipo: String,
    val descripcion: String,
    val completado: Boolean,
    val horaProgramada: Date?,
    val horaCompletado: Date?,
    val voluntarioId: String,
    val fotosEvidencia: List<String>,
    val observaciones: String,
    val sincronizado: Boolean = false,
    val fechaCreacionLocal: Date = Date()
)