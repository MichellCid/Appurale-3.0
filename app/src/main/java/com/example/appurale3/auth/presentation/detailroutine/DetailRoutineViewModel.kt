package com.example.appurale3.presentation.detailroutine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appurale3.data.models.Activity
import com.example.appurale3.data.models.Routine
import com.example.appurale3.data.repositories.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val routineRepository: RoutineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailRoutineUiState())
    val uiState: StateFlow<DetailRoutineUiState> = _uiState.asStateFlow()

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

        // Actualizar el estado de la actividad
        val updatedActivities = currentRoutine.activities.map { activity ->
            if (activity.id == activityId) {
                activity.copy(isCompleted = !activity.isCompleted)
            } else {
                activity
            }
        }

        val updatedRoutine = currentRoutine.copy(activities = updatedActivities)

        // Actualizar UI inmediatamente
        _uiState.update { it.copy(routine = updatedRoutine) }

        // Guardar en Firestore en segundo plano
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

}