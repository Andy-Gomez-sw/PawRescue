package com.refugio.pawrescue.data.model.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.refugio.pawrescue.data.model.Rescate
import com.refugio.pawrescue.data.model.local.dao.AnimalDao
import com.refugio.pawrescue.data.model.local.dao.CuidadoDao
import com.refugio.pawrescue.data.model.local.entity.AnimalEntity
import com.refugio.pawrescue.data.model.local.entity.CuidadoEntity

@Database(
    entities = [AnimalEntity::class, CuidadoEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun animalDao(): AnimalDao
    abstract fun cuidadoDao(): CuidadoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pawrescue_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}