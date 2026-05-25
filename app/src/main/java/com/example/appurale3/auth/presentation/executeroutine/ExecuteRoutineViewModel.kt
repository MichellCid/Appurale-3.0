package com.example.appurale3.presentation.executeroutine

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appurale3.MainActivity
import com.example.appurale3.data.models.Activity
import com.example.appurale3.data.models.Routine
import com.example.appurale3.data.repositories.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExecuteRoutineUiState(
    val routine: Routine? = null,
    val isLoading: Boolean = false,
    val currentActivity: Activity? = null,
    val currentActivityIndex: Int = 0,
    val timeRemaining: Int = 0,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false,
    val routineProgress: Float = 0f,
    val completedActivities: Int = 0,
    val errorMessage: String? = null
)

@HiltViewModel
class ExecuteRoutineViewModel @Inject constructor(
    private val routineRepository: RoutineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExecuteRoutineUiState())
    val uiState: StateFlow<ExecuteRoutineUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var alarmMediaPlayer: MediaPlayer? = null
    private var completionMediaPlayer: MediaPlayer? = null

    private var onPlayCompletionSound: ((String?) -> Unit)? = null
    private var onRoutineCompleted: ((String?, String) -> Unit)? = null
    private var onStopAlarmSound: (() -> Unit)? = null

    fun setOnPlayCompletionSound(callback: (String?) -> Unit) {
        onPlayCompletionSound = callback
    }

    fun setOnRoutineCompleted(callback: (String?, String) -> Unit) {
        onRoutineCompleted = callback
    }

    fun setOnStopAlarmSound(callback: () -> Unit) {
        onStopAlarmSound = callback
    }

    fun playCompletionSound(context: Context, soundUri: String?) {
        try {
            completionMediaPlayer?.release()

            val uri = if (!soundUri.isNullOrEmpty()) {
                Uri.parse(soundUri)
            } else {
                android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
            }

            completionMediaPlayer = MediaPlayer.create(context, uri)
            completionMediaPlayer?.start()
            completionMediaPlayer?.setOnCompletionListener {
                completionMediaPlayer?.release()
                completionMediaPlayer = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startAlarmSound(context: Context, soundUri: String?) {
        try {
            stopAlarmSound()

            val uri = if (!soundUri.isNullOrEmpty()) {
                Uri.parse(soundUri)
            } else {
                android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
            }

            alarmMediaPlayer = MediaPlayer.create(context, uri).apply {
                isLooping = true
                start()
                setOnErrorListener { _, _, _ ->
                    stopAlarmSound()
                    false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopAlarmSound() {
        alarmMediaPlayer?.apply {
            stop()
            release()
        }
        alarmMediaPlayer = null
        onStopAlarmSound?.invoke()
    }

    fun stopCompletionSound() {
        completionMediaPlayer?.release()
        completionMediaPlayer = null
    }

    fun releaseAllPlayers() {
        stopAlarmSound()
        stopCompletionSound()
    }
    // Noti
    fun showCompletionNotification(context: Context, routineName: String) {
        val channelId = "routine_completion_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Finalización de rutinas",
                NotificationManager.IMPORTANCE_DEFAULT  // ← IMPORTANCE_DEFAULT (sin sonido)
            ).apply {
                description = "Notificaciones cuando se completa una rutina"
                enableVibration(false)      // ← Sin vibración
                setSound(null, null)        // ← Sin sonido
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent para abrir la app
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("routineId", _uiState.value.routine?.id)
            putExtra("openRoutine", true)
        }
        val pendingContentIntent = PendingIntent.getActivity(
            context, 0, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent para FINALIZAR RUTINA
        val finishIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "ACTION_FINISH_ROUTINE"
            putExtra("routineId", _uiState.value.routine?.id)
        }
        val pendingFinishIntent = PendingIntent.getBroadcast(
            context, 1, finishIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Construir notificación - COMPLETAMENTE SILENCIOSA
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🎉 Rutina completada")
            .setContentText("¡Has completado la rutina $routineName!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("¡Felicidades! Has completado la rutina $routineName"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingContentIntent)
            .setSilent(true)  // ← SILENCIOSA
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "FINALIZAR",
                pendingFinishIntent
            )
            .build()

        notificationManager.notify(1001, notification)
    }
    fun loadRoutine(routineId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val routine = routineRepository.getRoutineById(routineId)
                if (routine != null && routine.activities.isNotEmpty()) {
                    val firstActivity = routine.activities.firstOrNull()
                    _uiState.update {
                        it.copy(
                            routine = routine,
                            isLoading = false,
                            currentActivityIndex = 0,
                            currentActivity = firstActivity,
                            timeRemaining = firstActivity?.duration?.times(60) ?: 0,
                            isRunning = true,
                            isPaused = false,
                            isCompleted = false,
                            routineProgress = 0f,
                            completedActivities = 0
                        )
                    }
                    startTimer()
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message)
                }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val currentState = _uiState.value

                if (!currentState.isRunning || currentState.isPaused || currentState.isCompleted) {
                    continue
                }

                if (currentState.timeRemaining <= 1) {
                    val newIndex = currentState.currentActivityIndex + 1
                    val routine = currentState.routine

                    if (routine != null && newIndex < routine.activities.size) {
                        onPlayCompletionSound?.invoke(routine.soundUri)

                        val nextActivity = routine.activities[newIndex]
                        _uiState.update {
                            it.copy(
                                currentActivityIndex = newIndex,
                                currentActivity = nextActivity,
                                timeRemaining = nextActivity.duration * 60,
                                completedActivities = newIndex,
                                routineProgress = newIndex.toFloat() / routine.activities.size
                            )
                        }
                    } else {
                        onRoutineCompleted?.invoke(routine?.soundUri, routine?.name ?: "")
                        _uiState.update {
                            it.copy(
                                isRunning = false,
                                isCompleted = true,
                                routineProgress = 1f,
                                completedActivities = routine?.activities?.size ?: 0
                            )
                        }
                        timerJob?.cancel()
                    }
                } else {
                    _uiState.update {
                        it.copy(timeRemaining = currentState.timeRemaining - 1)
                    }
                }
            }
        }
    }

    fun pauseActivity() {
        _uiState.update { it.copy(isPaused = true, isRunning = false) }
        timerJob?.cancel()
    }

    fun resumeActivity() {
        _uiState.update { it.copy(isPaused = false, isRunning = true) }
        startTimer()
    }

    fun restartActivity() {
        val currentState = _uiState.value
        val currentActivity = currentState.routine?.activities?.getOrNull(currentState.currentActivityIndex)
        if (currentActivity != null) {
            _uiState.update {
                it.copy(
                    timeRemaining = currentActivity.duration * 60,
                    isRunning = true,
                    isPaused = false
                )
            }
            startTimer()
        }
    }

    fun nextActivity() {
        timerJob?.cancel()
        val currentState = _uiState.value
        val routine = currentState.routine

        if (routine != null && currentState.currentActivityIndex + 1 < routine.activities.size) {
            onPlayCompletionSound?.invoke(routine.soundUri)

            val nextActivity = routine.activities[currentState.currentActivityIndex + 1]
            _uiState.update {
                it.copy(
                    currentActivityIndex = currentState.currentActivityIndex + 1,
                    currentActivity = nextActivity,
                    timeRemaining = nextActivity.duration * 60,
                    isRunning = true,
                    isPaused = false,
                    completedActivities = currentState.currentActivityIndex + 1,
                    routineProgress = (currentState.currentActivityIndex + 1).toFloat() / routine.activities.size
                )
            }
            startTimer()
        } else {
            completeRoutine()
        }
    }

    fun goToActivity(index: Int) {
        timerJob?.cancel()
        val currentState = _uiState.value
        val routine = currentState.routine

        if (routine != null && index >= 0 && index < currentState.currentActivityIndex) {
            val activity = routine.activities[index]
            _uiState.update {
                it.copy(
                    currentActivityIndex = index,
                    currentActivity = activity,
                    timeRemaining = activity.duration * 60,
                    isRunning = true,
                    isPaused = false,
                    completedActivities = index,
                    routineProgress = index.toFloat() / routine.activities.size
                )
            }
            startTimer()
        }
    }

    fun completeRoutine() {
        timerJob?.cancel()
        val routine = _uiState.value.routine

        onRoutineCompleted?.invoke(routine?.soundUri, routine?.name ?: "")

        _uiState.update {
            it.copy(
                isRunning = false,
                isCompleted = true,
                routineProgress = 1f,
                completedActivities = routine?.activities?.size ?: 0
            )
        }
    }

    fun finishRoutineAndStopAlarm() {
        stopAlarmSound()
    }

    fun checkIfFinishedByNotification(context: Context, routineId: String) {
        val prefs = context.getSharedPreferences("routine_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("alarm_finished_$routineId", false)) {
            prefs.edit().remove("alarm_finished_$routineId").apply()
            finishRoutineAndStopAlarm()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        releaseAllPlayers()
    }
}