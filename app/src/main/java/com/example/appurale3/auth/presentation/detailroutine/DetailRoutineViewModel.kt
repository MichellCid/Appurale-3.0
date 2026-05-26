package com.example.appurale3.presentation.detailroutine

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appurale3.data.models.Activity
import com.example.appurale3.data.models.ActivityTimer
import com.example.appurale3.data.models.Routine
import com.example.appurale3.data.models.repositories.TimerRepository
import com.example.appurale3.data.repositories.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.UUID

data class DetailRoutineUiState(
    val routine: Routine? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showDeleteDialog: Boolean = false
)

@HiltViewModel
class DetailRoutineViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val timerRepository: TimerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailRoutineUiState())
    val uiState: StateFlow<DetailRoutineUiState> = _uiState.asStateFlow()

    val routine: StateFlow<Routine?> = _uiState.asStateFlow().map { it.routine }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _uiState.value.routine)

    val activities: StateFlow<List<Activity>> = _uiState.asStateFlow().map { it.routine?.activities ?: emptyList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _uiState.value.routine?.activities ?: emptyList())

    private val _timers = MutableStateFlow<Map<String, ActivityTimer>>(emptyMap())
    val timers: StateFlow<Map<String, ActivityTimer>> = _timers

    /**fun loadRoutine(routineId: String, userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val routines = routineRepository.getRoutinesByUser(userId)
                val routine = routines.find { it.id == routineId }
                _uiState.update {
                    it.copy(routine = routine, isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message, isLoading = false)
                }
            }
        }
    }
    **/

    fun loadRoutine(routineId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val routine = routineRepository.getRoutineById(routineId)

                _uiState.update {
                    it.copy(routine = routine, isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message, isLoading = false)
                }
            }
        }
    }

    fun updateRoutine(updatedRoutine: Routine, onSuccess: () -> Unit = {}) {
        // Actualizar UI inmediatamente
        _uiState.update { it.copy(routine = updatedRoutine, isLoading = false) }

        // Guardar en Firestore en segundo plano
        viewModelScope.launch {
            routineRepository.updateRoutine(updatedRoutine)
        }
        onSuccess()
    }

    fun deleteRoutine(routineId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = routineRepository.deleteRoutine(routineId)

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, showDeleteDialog = false) }
                    onSuccess()
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(errorMessage = exception.message, isLoading = false, showDeleteDialog = false)
                    }
                }
            )
        }
    }

    fun addActivity(activity: Activity) {
        val currentRoutine = _uiState.value.routine ?: return
        val updatedActivities = currentRoutine.activities + activity
        val updatedRoutine = currentRoutine.copy(activities = updatedActivities)
        updateRoutine(updatedRoutine)
    }

    fun removeActivity(activityId: String) {
        val currentRoutine = _uiState.value.routine ?: return
        val updatedActivities = currentRoutine.activities.filter { it.id != activityId }
        val updatedRoutine = currentRoutine.copy(activities = updatedActivities)
        updateRoutine(updatedRoutine)
    }

    fun updateActivity(activityId: String, updatedActivity: Activity) {
        val currentRoutine = _uiState.value.routine ?: return
        val updatedActivities = currentRoutine.activities.map {
            if (it.id == activityId) updatedActivity else it
        }
        val updatedRoutine = currentRoutine.copy(activities = updatedActivities)
        updateRoutine(updatedRoutine)
    }

    fun toggleActivityCompletion(activityId: String) {
        val currentRoutine = _uiState.value.routine ?: return

        val updatedActivities = currentRoutine.activities.map { activity ->
            if (activity.id == activityId) {
                activity.copy(completed = !activity.completed)  // ← CAMBIADO
            } else {
                activity
            }
        }

        val updatedRoutine = currentRoutine.copy(activities = updatedActivities)
        _uiState.update { it.copy(routine = updatedRoutine) }

        viewModelScope.launch {
            routineRepository.updateRoutine(updatedRoutine)
        }
    }

    fun toggleDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = !it.showDeleteDialog) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun loadTimer(activityId: String, userId: String) {
        viewModelScope.launch {
            val existingTimer = timerRepository.getTimer(userId, activityId)

            val timer = if (existingTimer != null) {
                existingTimer
            } else {

                ActivityTimer(
                    activityId = activityId,
                    startTime = System.currentTimeMillis(),
                    accumulatedTime = 0L,
                    isRunning = true
                ).also {
                    timerRepository.saveTimer(userId, it)
                }
            }

            _timers.value = _timers.value + (activityId to timer)
        }
    }

    fun startTimer(activityId: String, userId: String) {
        val now = System.currentTimeMillis()
        val current = _timers.value[activityId]

        val updated = ActivityTimer(
            activityId = activityId,
            startTime = now,
            accumulatedTime = current?.accumulatedTime ?: 0L,
            isRunning = true
        )

        _timers.value = _timers.value + (activityId to updated)

        viewModelScope.launch {
            timerRepository.saveTimer(userId, updated)
        }
    }

    fun pauseTimer(activityId: String, userId: String) {
        val now = System.currentTimeMillis()
        val current = _timers.value[activityId] ?: return

        val elapsed = if (current.startTime != null) {
            now - current.startTime
        } else 0L

        val updated = current.copy(
            accumulatedTime = current.accumulatedTime + elapsed,
            startTime = null,
            isRunning = false
        )

        _timers.value = _timers.value + (activityId to updated)

        viewModelScope.launch {
            timerRepository.saveTimer(userId, updated)
        }
    }

    fun getTimeLeft(activity: Activity, now: Long): Long {
        val timer = _timers.value[activity.id]

        val totalMillis = activity.duration * 60 * 1000L
        //val now = System.currentTimeMillis()

        val elapsed = if (timer?.isRunning == true && timer.startTime != null) {
            timer.accumulatedTime + (now - timer.startTime)
        } else {
            timer?.accumulatedTime ?: 0L
        }

        return (totalMillis - elapsed).coerceAtLeast(0)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun getRoutineStartTime(hour: String): Long {
        val today = java.time.LocalDate.now()
        val time = java.time.LocalTime.parse(hour)

        return java.time.LocalDateTime.of(today, time)
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getActivityStartTime(
        routine: Routine,
        currentIndex: Int
    ): Long {

        val routineStart = getRoutineStartTime(routine.hour)

        val previousDurations = routine.activities
            .take(currentIndex)
            .sumOf { it.duration * 60 * 1000L }

        return routineStart + previousDurations
    }

    fun completeActivity(activityId: String) {
        val currentRoutine = _uiState.value.routine ?: return

        val updatedActivities = currentRoutine.activities.map { activity ->
            if (activity.id == activityId) {
                activity.copy(completed = true)
            } else {
                activity
            }
        }

        val updatedRoutine = currentRoutine.copy(activities = updatedActivities)

        // Actualizar UI inmediatamente
        _uiState.update { it.copy(routine = updatedRoutine) }

        // Guardar en Firestore
        viewModelScope.launch {
            routineRepository.updateRoutine(updatedRoutine)
        }
    }

    fun restartTimer(activityId: String, userId: String) {
        val updated = ActivityTimer(
            activityId = activityId,
            startTime = System.currentTimeMillis(),
            accumulatedTime = 0L,
            isRunning = true
        )

        _timers.value = _timers.value + (activityId to updated)

        viewModelScope.launch {
            timerRepository.saveTimer(userId, updated)
        }
    }

}