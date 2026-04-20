package com.example.appurale3.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.example.appurale3.data.models.TodayActivity
import com.example.appurale3.data.models.RoutineUiModel
data class HomeUiState(
    val isLoading: Boolean = true,
    val userName: String = "",
    val userEmail: String = "",
    val dailyProgress: Float = 0f,
    val todayActivities: List<TodayActivity> = emptyList(),
    val routines: List<RoutineUiModel> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: com.example.appurale3.auth.data.AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Exponer valores individuales para mejor rendimiento en Compose
    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _todayActivities = MutableStateFlow<List<TodayActivity>>(emptyList())
    val todayActivities: StateFlow<List<TodayActivity>> = _todayActivities.asStateFlow()

    private val _dailyProgress = MutableStateFlow(0f)
    val dailyProgress: StateFlow<Float> = _dailyProgress.asStateFlow()

    private val _routines = MutableStateFlow<List<RoutineUiModel>>(emptyList())
    val routines: StateFlow<List<RoutineUiModel>> = _routines.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadUserData()
        loadTodayActivities()
        loadRoutines()
    }

    private fun loadUserData() {
        val currentUser = repo.currentUser()
        val name = currentUser?.displayName ?:
        currentUser?.email?.substringBefore("@") ?: "Usuario"
        _userName.value = name
        _uiState.update { it.copy(
            userName = name,
            userEmail = currentUser?.email.orEmpty()
        )}
    }

    private fun loadTodayActivities() {
        // Datos de ejemplo - Aquí irá la conexión con Firestore
        viewModelScope.launch {
            _isLoading.value = true

            // TODO: Cargar desde Firestore las actividades de hoy
            // Por ahora usamos datos de ejemplo basados en tu imagen
            val activities = listOf(
                TodayActivity("1", "Ir al gym", duration = 60, isCompleted = false),
                TodayActivity("2", "Estudiar", duration = 120, isCompleted = true),
                TodayActivity("3", "Leer", duration = 30, isCompleted = true)
            )

            _todayActivities.value = activities

            // Calcular progreso
            val completed = activities.count { it.isCompleted }
            val total = activities.size
            _dailyProgress.value = if (total > 0) completed.toFloat() / total else 0f

            _uiState.update { it.copy(
                dailyProgress = _dailyProgress.value,
                todayActivities = activities
            )}

            _isLoading.value = false
        }
    }

    private fun loadRoutines() {
        viewModelScope.launch {
            // TODO: Cargar rutinas desde Firestore
            // Por ahora datos de ejemplo
            val routinesList = listOf(
                RoutineUiModel(
                    id = "1",
                    name = "Rutina Matutina",
                    description = "Ejercicios para empezar el día",
                    category = "Ejercicio",
                    totalActivities = 5,
                    completedActivities = 2
                ),
                RoutineUiModel(
                    id = "2",
                    name = "Estudio Nocturno",
                    description = "Repaso de temas importantes",
                    category = "Estudio",
                    totalActivities = 3,
                    completedActivities = 0
                )
            )

            _routines.value = routinesList
            _uiState.update { it.copy(routines = routinesList) }
        }
    }

    fun toggleActivityCompletion(activity: TodayActivity) {
        val updatedActivities = _todayActivities.value.map {
            if (it.id == activity.id) {
                it.copy(isCompleted = !it.isCompleted)
            } else it
        }

        _todayActivities.value = updatedActivities

        // Recalcular progreso
        val completed = updatedActivities.count { it.isCompleted }
        val total = updatedActivities.size
        _dailyProgress.value = if (total > 0) completed.toFloat() / total else 0f

        // TODO: Guardar en Firestore el cambio
    }

    fun startRoutine(routine: RoutineUiModel) {
        // TODO: Navegar a la pantalla de ejecución de rutina
    }

    fun logout() {
        repo.signOut()
    }
}