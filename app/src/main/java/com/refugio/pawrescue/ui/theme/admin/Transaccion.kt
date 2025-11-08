package com.refugio.pawrescue.ui.theme.admin

import java.util.Date
import java.util.UUID

enum class TipoTransaccion {
    DONACION, GASTO
}

data class Transaccion(
    val id: String = UUID.randomUUID().toString(),
    val tipo: TipoTransaccion,
    val monto: Double,
    val descripcion: String,
    val fecha: Date = Date(),
    // Puedes agregar más campos si es necesario, como 'categoria', 'metodoPago', etc.
    val donante: String? = null // Opcional, solo para donaciones
)