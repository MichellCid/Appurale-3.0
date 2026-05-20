package com.example.appurale3.presentation.executeroutine

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.SystemClock
import kotlin.math.abs

class ShakeDetector(
    private val onShakeDetected: () -> Unit
) : SensorEventListener {

    private var lastDirection = 0
    private var shakeCount = 0
    private var lastShakeTime = 0L
    private var alreadyTriggered = false

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        // Evita ejecutar otra vez
        if (alreadyTriggered) return

        val x = event.values[0]

        // Ignora movimientos pequeños
        if (abs(x) < 9) return

        val currentDirection =
            if (x > 0) 1 else -1

        val currentTime =
            SystemClock.elapsedRealtime()

        // Si pasa mucho tiempo reinicia conteo
        if (currentTime - lastShakeTime > 1000) {
            shakeCount = 0
        }

        if (
            lastDirection != 0 &&
            currentDirection != lastDirection
        ) {

            shakeCount++
            lastShakeTime = currentTime

            // Debe moverse izquierda-derecha varias veces
            if (shakeCount >= 5) {

                alreadyTriggered = true

                onShakeDetected()
            }
        }

        lastDirection = currentDirection
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int
    ) {}
}