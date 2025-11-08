package com.refugio.pawrescue.data.model.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.refugio.pawrescue.data.model.EstadoSolicitud
import com.refugio.pawrescue.data.model.SolicitudAdopcion

@Dao
interface SolicitudAdopcionDao {
    @Query("SELECT * FROM solicitudes_adopcion ORDER BY fechaSolicitud DESC")
    fun getAllSolicitudes(): LiveData<List<SolicitudAdopcion>>

    @Query("SELECT * FROM solicitudes_adopcion WHERE estado = :estado ORDER BY fechaSolicitud DESC")
    fun getSolicitudesByEstado(estado: EstadoSolicitud): LiveData<List<SolicitudAdopcion>>

    @Query("SELECT * FROM solicitudes_adopcion WHERE id = :id")
    suspend fun getSolicitudById(id: String): SolicitudAdopcion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSolicitud(solicitud: SolicitudAdopcion)

    @Update
    suspend fun updateSolicitud(solicitud: SolicitudAdopcion)

    @Query("UPDATE solicitudes_adopcion SET estado = :nuevoEstado, synced = 0 WHERE id = :id")
    suspend fun updateEstadoSolicitud(id: String, nuevoEstado: EstadoSolicitud)
}