package com.example.appurale3.presentation.executeroutine

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import kotlin.math.abs

class ShakeDetector(
    private val onShakeDetected: () -> Unit
) : SensorEventListener {

    private var lastDirection = 0
    private var shakeCount = 0

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        // Movimiento horizontal (izquierda-derecha)
        val x = event.values[0]

        // Ignorar movimientos pequeños
        if (abs(x) < 6) return

        val currentDirection =
            if (x > 0) 1 else -1

        // Detecta cambios izquierda-derecha
        if (
            lastDirection != 0 &&
            currentDirection != lastDirection
        ) {

            shakeCount++

            // Requiere varios movimientos
            if (shakeCount >= 4) {
                shakeCount = 0
                onShakeDetected()
            }
        }

        lastDirection = currentDirection
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int
    ) {
    }
}