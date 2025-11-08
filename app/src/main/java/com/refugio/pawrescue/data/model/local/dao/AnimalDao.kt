package com.refugio.pawrescue.data.model.local.dao

import androidx.room.*
import com.refugio.pawrescue.data.model.local.entity.AnimalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimalDao {

    @Query("SELECT * FROM animales WHERE refugioId = :refugioId ORDER BY fechaActualizacionLocal DESC")
    fun getAnimalesByRefugio(refugioId: String): Flow<List<AnimalEntity>>

    @Query("SELECT * FROM animales WHERE id = :animalId")
    suspend fun getAnimalById(animalId: String): AnimalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimal(animal: AnimalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimales(animales: List<AnimalEntity>)

    @Update
    suspend fun updateAnimal(animal: AnimalEntity)

    @Delete
    suspend fun deleteAnimal(animal: AnimalEntity)

    @Query("DELETE FROM animales WHERE id = :animalId")
    suspend fun deleteAnimalById(animalId: String)

    @Query("SELECT * FROM animales WHERE sincronizado = 0")
    suspend fun getAnimalesNoSincronizados(): List<AnimalEntity>

    @Query("UPDATE animales SET sincronizado = 1 WHERE id = :animalId")
    suspend fun marcarComoSincronizado(animalId: String)

    @Query("DELETE FROM animales")
    suspend fun deleteAll()
}