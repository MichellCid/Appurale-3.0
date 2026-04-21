package com.example.appurale3.auth.presentation.addroutine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appurale3.data.models.Routine
import com.example.appurale3.data.repositories.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

import com.example.appurale3.data.models.Activity

data class AddRoutineUiState(
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val date: Date? = null,
    val hour: String = "",
    val duration: String = "",
    val soundUri: String = "",
    val activities: List<Activity> = emptyList(),  // ← Cambiado
    val currentActivityName: String = "",
    val currentActivityDescription: String = "",
    val currentActivityDuration: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class AddRoutineViewModel @Inject constructor(
    private val routineRepository: RoutineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddRoutineUiState())
    val uiState: StateFlow<AddRoutineUiState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun updateCategory(category: String) {
        _uiState.update { it.copy(category = category) }
    }

    fun updateDate(date: Date?) {
        _uiState.update { it.copy(date = date) }
    }

    fun updateHour(hour: String) {
        _uiState.update { it.copy(hour = hour) }
    }

    fun updateDuration(duration: String) {
        _uiState.update { it.copy(duration = duration) }
    }

    fun updateSoundUri(uri: String) {
        _uiState.update { it.copy(soundUri = uri) }
    }

    fun updateCurrentActivityName(name: String) {
        _uiState.update { it.copy(currentActivityName = name) }
    }

    fun updateCurrentActivityDescription(description: String) {
        _uiState.update { it.copy(currentActivityDescription = description) }
    }

    fun updateCurrentActivityDuration(duration: String) {
        _uiState.update { it.copy(currentActivityDuration = duration) }
    }

    fun addActivity() {
        val currentState = _uiState.value
        if (currentState.currentActivityName.isNotBlank()) {
            val newActivity = Activity(
                id = System.currentTimeMillis().toString(),
                name = currentState.currentActivityName,
                description = currentState.currentActivityDescription,
                duration = currentState.currentActivityDuration.toIntOrNull() ?: 0
            )
            _uiState.update {
                it.copy(
                    activities = it.activities + newActivity,
                    currentActivityName = "",
                    currentActivityDescription = "",
                    currentActivityDuration = ""
                )
            }
        }
    }

    fun removeActivity(activityId: String) {
        _uiState.update {
            it.copy(activities = it.activities.filter { act -> act.id != activityId })
        }
    }

    fun saveRoutine(userId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            val currentState = _uiState.value

            if (currentState.name.isBlank()) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "El nombre de la rutina es obligatorio")
                }
                return@launch
            }


            val routine = Routine(
                name = currentState.name,
                description = currentState.description,
                category = currentState.category,
                date = currentState.date,
                hour = currentState.hour,
                duration = currentState.duration.toIntOrNull() ?: 0,
                soundUri = currentState.soundUri,
                activities = currentState.activities,
                userId = userId
            )

            val result = routineRepository.saveRoutine(routine)

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(isLoading = false, successMessage = "Rutina guardada exitosamente")
                    }
                    onSuccess()
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error al guardar: ${exception.message}"
                        )
                    }
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}