package com.refugio.pawrescue.ui.theme.admin

import java.util.Date
import java.util.UUID

data class Transaccion(
    val id: String = UUID.randomUUID().toString(),
    val tipo: TipoTransaccion,
    val concepto: String = "",
    val monto: Double = 0.0,
    val fecha: Date = Date(),
    val categoria: String = "",
    val descripcion: String = "",
    val donante: String? = null
)