package com.refugio.pawrescue.data.model.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.refugio.pawrescue.data.model.EstadoSolicitud
import java.util.Date

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromEstadoSolicitud(value: EstadoSolicitud?): String? {
        return value?.name
    }

    @TypeConverter
    fun toEstadoSolicitud(value: String?): EstadoSolicitud? {
        return value?.let { EstadoSolicitud.valueOf(it) }
    }
}