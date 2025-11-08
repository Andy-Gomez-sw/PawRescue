package com.refugio.pawrescue.data.model.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.refugio.pawrescue.data.model.EstadoSolicitud
import com.refugio.pawrescue.data.model.SolicitudAdopcionEntity

@Dao
interface SolicitudAdopcionDao {
    @Query("SELECT * FROM solicitudes_adopcion ORDER BY fechaSolicitud DESC")
    fun getAllSolicitudes(): LiveData<List<SolicitudAdopcionEntity>>

    @Query("SELECT * FROM solicitudes_adopcion WHERE estado = :estado ORDER BY fechaSolicitud DESC")
    fun getSolicitudesByEstado(estado: EstadoSolicitud): LiveData<List<SolicitudAdopcionEntity>>

    @Query("SELECT * FROM solicitudes_adopcion WHERE id = :id")
    suspend fun getSolicitudById(id: String): SolicitudAdopcionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSolicitud(solicitud: SolicitudAdopcionEntity)

    @Update
    suspend fun updateSolicitud(solicitud: SolicitudAdopcionEntity)

    @Query("UPDATE solicitudes_adopcion SET estado = :nuevoEstado, synced = 0 WHERE id = :id")
    suspend fun updateEstadoSolicitud(id: String, nuevoEstado: EstadoSolicitud)
}