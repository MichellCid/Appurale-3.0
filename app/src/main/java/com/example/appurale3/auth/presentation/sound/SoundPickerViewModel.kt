package com.example.appurale3.auth.presentation.sound

import android.content.ContentResolver
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appurale3.data.repositories.SoundItem
import com.example.appurale3.data.repositories.SoundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SoundPickerUiState(
    val defaultSounds: List<SoundItem> = emptyList(),
    val customSounds: List<SoundItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class SoundPickerViewModel @Inject constructor(
    private val soundRepository: SoundRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SoundPickerUiState())
    val uiState: StateFlow<SoundPickerUiState> = _uiState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null

    init {
        loadSounds()
    }

    private fun loadSounds() {
        _uiState.value = _uiState.value.copy(
            defaultSounds = soundRepository.getDefaultSounds(),
            customSounds = emptyList(),
            isLoading = false
        )
    }

    // Recibe ContentResolver como parámetro (desde la UI)
    fun addCustomSound(uri: Uri, contentResolver: ContentResolver, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = soundRepository.saveCustomSound(uri, contentResolver)

            result.fold(
                onSuccess = { savedPath ->
                    // Recargar la lista de sonidos personalizados
                    loadSounds()
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess(savedPath)
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al guardar el sonido: ${exception.message}"
                    )
                }
            )
        }
    }

    fun previewSound(uriString: String) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(uriString)
                prepare()
                start()
                setOnCompletionListener {
                    release()
                    mediaPlayer = null
                }
                setOnErrorListener { _, what, extra ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "No se pudo reproducir el sonido"
                    )
                    false
                }
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "No se pudo reproducir el sonido: ${e.message}"
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}