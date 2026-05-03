package com.example.appurale3.data.service

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.example.appurale3.MainActivity
import com.example.appurale3.R

class ActivityForegroundService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val name = intent?.getStringExtra("ACTIVITY_NAME") ?: "Rutina en curso"
        val duration = intent?.getLongExtra("DURATION_MS", 0L) ?: 0L

        createNotificationChannel()

        // El cronómetro en la notificación funciona aunque el CPU duerma
        val notification = NotificationCompat.Builder(this, "activity_channel")
            .setContentTitle("Appurale: $name en curso")
            .setContentText("Tu actividad ha comenzado")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Usa tus iconos de res
            .setUsesChronometer(true)
            .setOngoing(true)
            .build()

        startForeground(1, notification)

        // Lógica para enviar notificación de fin tras la duración
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            sendFinishNotification(name)
            stopSelf()
        }, duration)

        return START_NOT_STICKY
    }

    private fun sendFinishNotification(name: String) {
        val notification = NotificationCompat.Builder(this, "activity_channel")
            .setContentTitle("¡Actividad terminada!")
            .setContentText("Has completado $name")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(2, notification)
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "activity_channel",
                "Actividades",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}