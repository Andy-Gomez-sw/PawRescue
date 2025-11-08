package com.refugio.pawrescue

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.*
import com.google.firebase.FirebaseApp
import com.refugio.pawrescue.ui.theme.utils.Constants
import com.refugio.pawrescue.workers.CleanupWorker
import com.refugio.pawrescue.workers.SyncWorker
import java.util.concurrent.TimeUnit

class MyApplication : Application() {

    companion object {
        private const val CHANNEL_ID_GENERAL = "pawrescue_general"
        private const val CHANNEL_ID_ALERTS = "pawrescue_alerts"
        private const val CHANNEL_ID_SYNC = "pawrescue_sync"

        private const val SYNC_WORK_NAME = "pawrescue_sync"
        private const val CLEANUP_WORK_NAME = "pawrescue_cleanup"
    }

    override fun onCreate() {
        super.onCreate()

        // Inicializar Firebase
        FirebaseApp.initializeApp(this)

        // Crear canales de notificación
        createNotificationChannels()

        // Programar trabajos periódicos
        scheduleSyncWork()
        scheduleCleanupWork()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                "Notificaciones Generales",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones generales de PawRescue"
            }

            val alertsChannel = NotificationChannel(
                CHANNEL_ID_ALERTS,
                "Alertas Importantes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas importantes sobre animales"
            }

            val syncChannel = NotificationChannel(
                CHANNEL_ID_SYNC,
                "Sincronización",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Sincronización de datos"
            }

            notificationManager.createNotificationChannel(generalChannel)
            notificationManager.createNotificationChannel(alertsChannel)
            notificationManager.createNotificationChannel(syncChannel)
        }
    }

    private fun scheduleSyncWork() {
        // Restricciones: Solo con conexión a internet
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Trabajo periódico cada 15 minutos
        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            Constants.SYNC_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(Constants.WORK_TAG_SYNC)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                10,
                TimeUnit.SECONDS
            )
            .build()

        // Programar trabajo (KEEP = no duplicar si ya existe)
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )
    }

    private fun scheduleCleanupWork() {
        val constraints = Constraints.Builder()
            .setRequiresCharging(true) // Solo cuando está cargando
            .build()

        // Trabajo periódico cada 24 horas
        val cleanupWorkRequest = PeriodicWorkRequestBuilder<CleanupWorker>(
            24,
            TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            CLEANUP_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            cleanupWorkRequest
        )
    }
}