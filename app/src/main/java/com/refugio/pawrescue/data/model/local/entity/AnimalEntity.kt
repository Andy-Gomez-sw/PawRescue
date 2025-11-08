package com.refugio.pawrescue.data.model.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "animales")
data class AnimalEntity(
    @PrimaryKey
    val id: String,
    val nombre: String,
    val especie: String,
    val raza: String,
    val edad: String,
    val sexo: String,
    val peso: Double,
    val estadoSalud: String,
    val vacunasCompletas: Boolean,
    val esterilizado: Boolean,
    val fotosPrincipales: List<String>,
    val estadoAdopcion: String,
    val fechaRescate: Date?,
    val refugioId: String,
    val sincronizado: Boolean = false,
    val fechaActualizacionLocal: Date = Date()
)