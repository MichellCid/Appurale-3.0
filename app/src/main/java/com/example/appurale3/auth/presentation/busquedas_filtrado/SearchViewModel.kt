package com.example.appurale3.auth.presentation.busquedas_filtrado
/*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.util.copy
import com.example.appurale3.auth.data.AuthRepository
import com.example.appurale3.data.repositories.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val routineRepository: RoutineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val userId = repo.currentUser()?.uid ?: return@launch

            val routines = routineRepository.getRoutinesByUser(userId)

            // Simulación actividades (puedes reemplazar con repo real)
            val activities = routines.flatMap { it.activities }

            val categories = (routines.map { it.category } +
                    activities.map { it.category })
                .filter { it.isNotEmpty() }
                .distinct()

            _uiState.update {
                it.copy(
                    routines = routines,
                    activities = activities,
                    categories = categories
                )
            }
        }
    }
}

 */