package com.example.appurale3.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appurale3.auth.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val loading: Boolean = false,
    val error : String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo : AuthRepository) : ViewModel() {


        private val _ui = MutableStateFlow(LoginUiState())

        val ui : StateFlow<LoginUiState> = _ui

        sealed interface Event{data object Success : Event}

        private val _event = MutableSharedFlow<Event>()

        val event : SharedFlow<Event> = _event.asSharedFlow()

    fun singIn(email: String, pass: String) {
        viewModelScope.launch {

            _ui.update{ current ->
                current.copy(
                    loading = true,
                    error = null
                )
            }

            val r = repo.signIn(email.trim(), pass)

            if (r.isSuccess){
                _ui.update { current ->
                    current.copy(
                        loading = false,
                    )

                }
                _event.emit(Event.Success)
            }else{
                _ui.update {
                        current -> current.copy(
                    loading = false,
                    error = r.exceptionOrNull()?.toReadable()
                )
                }
            }


        }
    }



    private fun Throwable.toReadable(): String =
        (this.message ?: "Error inesperado. Intenta de nuevo")




}