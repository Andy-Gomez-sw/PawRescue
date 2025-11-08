package com.refugio.pawrescue.data.model.local.dao

import androidx.room.*
import com.refugio.pawrescue.data.model.local.entity.CuidadoEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface CuidadoDao {

    @Query("SELECT * FROM cuidados WHERE animalId = :animalId ORDER BY horaProgramada ASC")
    fun getCuidadosByAnimal(animalId: String): Flow<List<CuidadoEntity>>

    @Query("SELECT * FROM cuidados WHERE voluntarioId = :voluntarioId AND completado = 0 ORDER BY horaProgramada ASC")
    fun getCuidadosPendientesByVoluntario(voluntarioId: String): Flow<List<CuidadoEntity>>

    @Query("SELECT * FROM cuidados WHERE id = :cuidadoId")
    suspend fun getCuidadoById(cuidadoId: String): CuidadoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCuidado(cuidado: CuidadoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCuidados(cuidados: List<CuidadoEntity>)

    @Update
    suspend fun updateCuidado(cuidado: CuidadoEntity)

    @Delete
    suspend fun deleteCuidado(cuidado: CuidadoEntity)

    @Query("SELECT * FROM cuidados WHERE sincronizado = 0")
    suspend fun getCuidadosNoSincronizados(): List<CuidadoEntity>

    @Query("UPDATE cuidados SET sincronizado = 1 WHERE id = :cuidadoId")
    suspend fun marcarComoSincronizado(cuidadoId: String)

    @Query("DELETE FROM cuidados WHERE horaProgramada < :fecha")
    suspend fun deleteCuidadosAntiguos(fecha: Date)

    @Query("DELETE FROM cuidados")
    suspend fun deleteAll()
}