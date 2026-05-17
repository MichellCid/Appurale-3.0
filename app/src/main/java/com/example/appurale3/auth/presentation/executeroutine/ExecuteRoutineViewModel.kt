package com.example.appurale3.presentation.executeroutine

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val isCompleted: Boolean = false,  // ← AGREGADO
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
    private var mediaPlayer: MediaPlayer? = null

    fun playRoutineSound(context: Context, soundUri: String?) {
        try {
            mediaPlayer?.release()

            val uri = if (!soundUri.isNullOrEmpty()) {
                Uri.parse(soundUri)
            } else {
                android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
            }

            mediaPlayer = MediaPlayer.create(context, uri)
            mediaPlayer?.start()
            mediaPlayer?.setOnCompletionListener {
                mediaPlayer?.release()
                mediaPlayer = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
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
        _uiState.update {
            it.copy(
                isRunning = false,
                isCompleted = true,
                routineProgress = 1f,
                completedActivities = _uiState.value.routine?.activities?.size ?: 0
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        releaseMediaPlayer()
    }
}