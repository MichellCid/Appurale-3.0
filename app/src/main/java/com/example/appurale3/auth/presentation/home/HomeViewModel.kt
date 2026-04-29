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
import com.example.appurale3.data.repositories.RoutineRepository

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
    private val repo: com.example.appurale3.auth.data.AuthRepository,
    private val routineRepository: RoutineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("")  // ← NUEVO
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()  // ← NUEVO

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
        val email = currentUser?.email ?: ""

        _userName.value = name
        _userEmail.value = email  // ← NUEVO

        _uiState.update { it.copy(
            userName = name,
            userEmail = email
        )}
    }

    private fun loadTodayActivities() {
        viewModelScope.launch {
            val activities = listOf(
                TodayActivity("1", "Ir al gym", duration = 60, isCompleted = false),
                TodayActivity("2", "Estudiar", duration = 120, isCompleted = true),
                TodayActivity("3", "Leer", duration = 30, isCompleted = true)
            )

            _todayActivities.value = activities

            val completed = activities.count { it.isCompleted }
            val total = activities.size
            _dailyProgress.value = if (total > 0) completed.toFloat() / total else 0f

            _uiState.update { it.copy(
                dailyProgress = _dailyProgress.value,
                todayActivities = activities
            )}
        }
    }

    fun loadRoutines() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val userId = repo.currentUser()?.uid
                if (userId.isNullOrEmpty()) {
                    _isLoading.value = false
                    return@launch
                }

                val routines = routineRepository.getRoutinesByUser(userId)

                val routineUiModels = routines.map { routine ->
                    // Calcular cuántas actividades están completadas
                    val completedCount = routine.activities.count { it.completed }

                    RoutineUiModel(
                        id = routine.id,
                        name = routine.name,
                        description = routine.description,
                        category = routine.category,
                        totalActivities = routine.activities.size,
                        completedActivities = completedCount  // ← AHORA USA EL VALOR REAL
                    )
                }
                _routines.value = routineUiModels
                _uiState.update { it.copy(routines = routineUiModels) }
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    fun refreshRoutines() {
        loadRoutines()  
    }

    fun toggleActivityCompletion(activity: TodayActivity) {
        val updatedActivities = _todayActivities.value.map {
            if (it.id == activity.id) {
                it.copy(isCompleted = !it.isCompleted)
            } else it
        }

        _todayActivities.value = updatedActivities

        val completed = updatedActivities.count { it.isCompleted }
        val total = updatedActivities.size
        _dailyProgress.value = if (total > 0) completed.toFloat() / total else 0f
    }

    fun startRoutine(routine: RoutineUiModel) {
        // TODO: Navegar a la pantalla de ejecución de rutina
    }

    fun logout() {
        repo.signOut()
    }
}