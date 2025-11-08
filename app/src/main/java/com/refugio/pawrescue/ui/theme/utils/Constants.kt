package com.refugio.pawrescue.ui.theme.utils

object Constants {
    // Firestore Collections
    const val COLLECTION_ANIMALES = "animales"
    const val COLLECTION_USUARIOS = "usuarios"
    const val COLLECTION_CUIDADOS = "cuidados"
    const val COLLECTION_RESCATES = "rescates"
    const val COLLECTION_ADOPCIONES = "adopciones"
    const val COLLECTION_SEGUIMIENTOS = "seguimientos"
    const val COLLECTION_DONACIONES = "donaciones"
    const val COLLECTION_REFUGIOS = "refugios"

    // Storage Paths
    const val STORAGE_ANIMALES = "animales"
    const val STORAGE_RESCATES = "rescates"
    const val STORAGE_ADOPCIONES = "adopciones"
    const val STORAGE_USUARIOS = "usuarios"

    // SharedPreferences Keys
    const val PREFS_NAME = "PawRescuePrefs"
    const val KEY_USER_ID = "user_id"
    const val KEY_USER_ROL = "user_rol"
    const val KEY_REFUGIO_ID = "refugio_id"
    const val KEY_REMEMBER_ME = "remember_me"
    const val KEY_USER_EMAIL = "user_email"

    // User Roles
    const val ROL_VOLUNTARIO = "voluntario"
    const val ROL_COORDINADOR = "coordinador"
    const val ROL_ADMIN = "admin"


    // Animal Types
    const val TIPO_PERRO = "perro"
    const val TIPO_GATO = "gato"
    const val TIPO_AVE = "ave"
    const val TIPO_OTRO = "otro"

    // Animal Ages
    const val EDAD_CACHORRO = "cachorro"
    const val EDAD_ADULTO = "adulto"
    const val EDAD_VEJEZ = "vejez"

    // Health Status
    const val SALUD_BUENO = "bueno"
    const val SALUD_REGULAR = "regular"
    const val SALUD_MALO = "malo"

    // Adoption Status
    const val ADOPCION_DISPONIBLE = "disponible"
    const val ADOPCION_EN_PROCESO = "en_proceso"
    const val ADOPCION_ADOPTADO = "adoptado"

    // Priority Levels
    const val PRIORIDAD_BAJA = "baja"
    const val PRIORIDAD_MEDIA = "media"
    const val PRIORIDAD_ALTA = "alta"

    // Care Types
    const val CUIDADO_ALIMENTACION = "alimentacion"
    const val CUIDADO_MEDICAMENTO = "medicamento"
    const val CUIDADO_PASEO = "paseo"
    const val CUIDADO_REVISION = "revision"
    const val CUIDADO_LIMPIEZA = "limpieza"

    // Request Codes
    const val REQUEST_CAMERA_PERMISSION = 100
    const val REQUEST_LOCATION_PERMISSION = 101
    const val REQUEST_STORAGE_PERMISSION = 102
    const val REQUEST_IMAGE_CAPTURE = 103
    const val REQUEST_IMAGE_PICK = 104

    // WorkManager Tags
    const val WORK_TAG_SYNC = "sync_work"
    const val WORK_TAG_UPLOAD = "upload_work"

    // Time Constants
    const val SYNC_INTERVAL_MINUTES = 15L
    const val CACHE_EXPIRATION_HOURS = 24L

    // Image Quality
    const val IMAGE_QUALITY = 80
    const val MAX_IMAGE_WIDTH = 1920
    const val MAX_IMAGE_HEIGHT = 1080


}