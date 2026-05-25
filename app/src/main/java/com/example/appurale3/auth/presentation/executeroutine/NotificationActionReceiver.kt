package com.example.appurale3.presentation.executeroutine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.appurale3.MainActivity

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "ACTION_FINISH_ROUTINE" -> {
                // Cerrar la notificación
                NotificationManagerCompat.from(context).cancel(1001)

                // Guardar que la rutina fue finalizada
                val routineId = intent.getStringExtra("routineId")
                context.getSharedPreferences("routine_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("alarm_finished_$routineId", true)
                    .apply()

                // Abrir la app
                val appIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(appIntent)
            }
        }
    }
}