package com.refugio.pawrescue.data.model

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Usuario(
    @DocumentId
    val id: String = "",
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val rol: String = "voluntario", // "voluntario", "coordinador", "admin"
    val refugioId: String = "",
    val fotoPerfilUrl: String = "",
    val turno: String = "", // "mañana", "tarde", "noche", "fines_semana"
    val activo: Boolean = true,
    val animalesAsignados: List<String> = emptyList(), // IDs de animales
    val fechaRegistro: Date? = null,
    val ultimaConexion: Date? = null
)