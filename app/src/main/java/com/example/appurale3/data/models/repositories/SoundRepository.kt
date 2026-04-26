package com.example.appurale3.data.repositories

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundRepository @Inject constructor(
    private val context: Context
) {

    fun getDefaultSounds(): List<SoundItem> {
        // Usar sonidos de los recursos raw de la app
        val rawResources = listOf(
            "android.resource://${context.packageName}/raw/sound1" to "Sonido Suave",
            "android.resource://${context.packageName}/raw/sound2" to "Timbre Clásico",
            "android.resource://${context.packageName}/raw/sound3" to "Alarma Elegante",
            "android.resource://${context.packageName}/raw/sound4" to "Campana",
            "android.resource://${context.packageName}/raw/sound5" to "Piano"
        )

        return rawResources.mapIndexed { index, (uri, name) ->
            SoundItem("default_$index", name, uri)
        }
    }

    fun getCustomSoundsDir(): File? {
        val dir = File(context.filesDir, "custom_sounds")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return if (dir.exists()) dir else null
    }

    fun isValidAudioFile(uri: Uri, contentResolver: ContentResolver): Boolean {
        return try {
            val mimeType = contentResolver.getType(uri)
            mimeType?.startsWith("audio/") == true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun saveCustomSound(uri: Uri, contentResolver: ContentResolver): Result<String> {
        return try {
            val soundsDir = File(context.filesDir, "custom_sounds")
            if (!soundsDir.exists()) soundsDir.mkdirs()

            val fileName = "custom_sound_${System.currentTimeMillis()}.mp3"
            val destFile = File(soundsDir, fileName)

            contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            Result.success(destFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
data class SoundItem(
    val id: String,
    val name: String,
    val uri: String
)