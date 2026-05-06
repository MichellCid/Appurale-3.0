package com.example.appurale3.auth.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appurale3.data.models.CalendarEvent
import com.example.appurale3.data.repositories.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class CalendarUiState(
    val isLoading: Boolean = true,
    val selectedDate: Date = Date(),
    val eventsByDate: Map<String, List<CalendarEvent>> = emptyMap(),
    val selectedDateEvents: List<CalendarEvent> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val routineRepository: RoutineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun loadEvents(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val routines = routineRepository.getRoutinesByUser(userId)
                val events = mutableListOf<CalendarEvent>()

                routines.forEach { routine ->
                    routine.activities.forEach { activity ->
                        // Crear evento para cada actividad con fecha de la rutina
                        val event = CalendarEvent(
                            id = activity.id,
                            title = activity.name,
                            description = activity.description,
                            date = routine.date,
                            hour = routine.hour,
                            duration = activity.duration,
                            routineId = routine.id,
                            routineName = routine.name,
                            isCompleted = activity.completed,
                            userId = userId
                        )
                        events.add(event)
                    }
                }

                // Organizar eventos por fecha
                val eventsByDate = events.groupBy { event ->
                    event.date?.let { dateFormat.format(it) } ?: "sin_fecha"
                }

                // Obtener eventos del día seleccionado
                val selectedDateKey = dateFormat.format(_uiState.value.selectedDate)
                val selectedDateEvents = eventsByDate[selectedDateKey] ?: emptyList()

                _uiState.update { it.copy(
                    isLoading = false,
                    eventsByDate = eventsByDate,
                    selectedDateEvents = selectedDateEvents
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar actividades: ${e.message}"
                )}
            }
        }
    }

    fun selectDate(date: Date) {
        val dateKey = dateFormat.format(date)
        val eventsForDate = _uiState.value.eventsByDate[dateKey] ?: emptyList()

        _uiState.update { it.copy(
            selectedDate = date,
            selectedDateEvents = eventsForDate
        )}
    }

    fun goToPreviousMonth() {
        val calendar = Calendar.getInstance()
        calendar.time = _uiState.value.selectedDate
        calendar.add(Calendar.MONTH, -1)
        selectDate(calendar.time)
    }

    fun goToNextMonth() {
        val calendar = Calendar.getInstance()
        calendar.time = _uiState.value.selectedDate
        calendar.add(Calendar.MONTH, 1)
        selectDate(calendar.time)
    }

    fun goToToday() {
        selectDate(Date())
    }

    fun toggleActivityCompletion(event: CalendarEvent) {
        viewModelScope.launch {
            try {
                val userId = event.userId
                val routines = routineRepository.getRoutinesByUser(userId)
                val routine = routines.find { it.id == event.routineId }

                if (routine != null) {
                    val updatedActivities = routine.activities.map { activity ->
                        if (activity.id == event.id) {
                            activity.copy(completed = !activity.completed)
                        } else {
                            activity
                        }
                    }
                    val updatedRoutine = routine.copy(activities = updatedActivities)
                    routineRepository.updateRoutine(updatedRoutine)

                    // Recargar eventos
                    loadEvents(userId)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error al actualizar actividad") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
