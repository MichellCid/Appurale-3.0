package com.example.appurale3.auth.presentation.sound

import android.content.ContentResolver
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appurale3.data.repositories.SoundItem
import com.example.appurale3.data.repositories.SoundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SoundPickerUiState(
    val defaultSounds: List<SoundItem> = emptyList(),
    val customSounds: List<SoundItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPlaying: String? = null  // ID del sonido que se está reproduciendo
)

@HiltViewModel
class SoundPickerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val soundRepository: SoundRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SoundPickerUiState())
    val uiState: StateFlow<SoundPickerUiState> = _uiState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingId: String? = null

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

    fun addCustomSound(uri: Uri, contentResolver: ContentResolver, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = soundRepository.saveCustomSound(uri, contentResolver)

            result.fold(
                onSuccess = { savedPath ->
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

    fun previewSound(soundId: String, soundUri: String) {
        // Si ya se está reproduciendo el mismo sonido, detenerlo
        if (currentPlayingId == soundId && mediaPlayer?.isPlaying == true) {
            stopSound()
            return
        }

        // Detener cualquier reproducción actual
        stopSound()

        if (soundUri.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Este sonido no está disponible"
            )
            return
        }

        try {
            _uiState.value = _uiState.value.copy(isPlaying = soundId)
            currentPlayingId = soundId

            val uri = try {
                // Intentar convertir a Uri
                if (soundUri.startsWith("android.resource://")) {
                    Uri.parse(soundUri)
                } else if (soundUri.startsWith("content://")) {
                    Uri.parse(soundUri)
                } else {
                    // Si es una ruta de archivo
                    Uri.parse("file://$soundUri")
                }
            } catch (e: Exception) {
                null
            }

            mediaPlayer = MediaPlayer().apply {
                try {
                    if (uri != null) {
                        setDataSource(context, uri)
                    } else {
                        setDataSource(soundUri)
                    }
                    prepare()
                    start()
                    setOnCompletionListener {
                        stopSound()
                    }
                    setOnErrorListener { _, what, extra ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "No se pudo reproducir el sonido",
                            isPlaying = null
                        )
                        stopSound()
                        false
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Error al reproducir: ${e.message}",
                        isPlaying = null
                    )
                    stopSound()
                }
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Error al reproducir: ${e.message}",
                isPlaying = null
            )
        }
    }

    private fun stopSound() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
        currentPlayingId = null
        _uiState.value = _uiState.value.copy(isPlaying = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        stopSound()
    }
}