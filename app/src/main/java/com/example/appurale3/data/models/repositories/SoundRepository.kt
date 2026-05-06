package com.example.appurale3.data.repositories

import android.content.ContentResolver
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.provider.OpenableColumns
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun getDefaultSounds(): List<SoundItem> {
        val sounds = mutableListOf<SoundItem>()

        // Obtener sonidos de notificación del sistema
        val ringtoneManager = RingtoneManager(context)
        ringtoneManager.setType(RingtoneManager.TYPE_NOTIFICATION)

        val cursor = ringtoneManager.cursor
        if (cursor != null && cursor.count > 0) {
            var count = 0
            while (cursor.moveToNext() && count < 10) {
                val id = cursor.getInt(RingtoneManager.ID_COLUMN_INDEX)
                val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                val uri = ringtoneManager.getRingtoneUri(id)
                if (uri != null) {
                    sounds.add(SoundItem("system_$id", title, uri.toString()))
                    count++
                }
            }
            cursor.close()
        }

        // Si por algún motivo no hay sonidos del sistema, agregar opciones por defecto
        if (sounds.isEmpty()) {
            sounds.addAll(getFallbackSounds())
        }

        return sounds
    }

    private fun getFallbackSounds(): List<SoundItem> {
        // Usar los sonidos de raw si existen, sino sonidos vacíos
        val packageName = context.packageName
        return listOf(
            SoundItem("fallback_1", "Sonido 1", "android.resource://$packageName/raw/sound1"),
            SoundItem("fallback_2", "Sonido 2", "android.resource://$packageName/raw/sound2"),
            SoundItem("fallback_3", "Sonido 3", "android.resource://$packageName/raw/sound3"),
            SoundItem("fallback_4", "Sonido 4", "android.resource://$packageName/raw/sound4"),
            SoundItem("fallback_5", "Sonido 5", "android.resource://$packageName/raw/sound5")
        )
    }

    fun getCustomSounds(): List<SoundItem> {
        val sounds = mutableListOf<SoundItem>()
        val directory = File(context.filesDir, "sounds")
        if (directory.exists() && directory.isDirectory) {
            directory.listFiles()?.forEach { file ->
                sounds.add(SoundItem(file.name, file.name, file.absolutePath))
            }
        }
        return sounds
    }

    fun saveCustomSound(uri: Uri, contentResolver: ContentResolver): Result<String> {
        return try {
            val fileName = getFileName(uri, contentResolver) ?: "custom_sound_${System.currentTimeMillis()}"
            val directory = File(context.filesDir, "sounds")
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val file = File(directory, fileName)
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: throw Exception("No se pudo abrir el stream de entrada")

            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getFileName(uri: Uri, contentResolver: ContentResolver): String? {
        var name: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
            }
        }
        return name
    }
}

data class SoundItem(
    val id: String,
    val name: String,
    val uri: String
)
