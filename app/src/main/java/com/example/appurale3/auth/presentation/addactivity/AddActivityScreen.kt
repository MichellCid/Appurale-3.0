package com.example.appurale3.presentation.addactivity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.appurale3.data.models.Activity
import com.example.appurale3.data.receiver.ActivityAlarmReceiver
import com.example.appurale3.presentation.detailroutine.DetailRoutineViewModel
import java.util.Calendar
import java.util.Date
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActivityScreen(
    routineId: String,
    viewModel: DetailRoutineViewModel,
    existingActivity: Activity? = null,
    onNavigateBack: () -> Unit

) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(existingActivity?.name ?: "") }
    var description by remember { mutableStateOf(existingActivity?.description ?: "") }
    var duration by remember { mutableStateOf(existingActivity?.duration?.toString() ?: "") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val isEditing = existingActivity != null
    val MAX_CHARS = 500


    fun convertToMillis(date: Date?, hour: String): Long {
        return try {
            if (date == null || hour.isEmpty()) return System.currentTimeMillis()

            val calendar = Calendar.getInstance()
            calendar.time = date


            val parts = hour.split(":")
            val hourInt = parts[0].toInt()
            val minuteInt = parts[1].toInt()

            calendar.set(Calendar.HOUR_OF_DAY, hourInt)
            calendar.set(Calendar.MINUTE, minuteInt)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            calendar.timeInMillis

        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    fun calculateStartTime(): Long {
        val routine = viewModel.routine.value ?: return System.currentTimeMillis()

        val startTime = convertToMillis(routine.date, routine.hour)

        var startTimeAccumulated = startTime

        val currentActivities = viewModel.activities.value

        currentActivities.forEach { act ->
            startTimeAccumulated += (act.duration * 60 * 1000)
        }

        return startTimeAccumulated
    }



    fun scheduleActivity(context: Context, activity: Activity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Calculamos cuánto dura ESTA actividad en ms para la notificación de fin
        val durationMs = (activity.duration.toLong()) * 60 * 1000

        // Calculamos CUÁNDO debe empezar basándonos en la rutina y actividades previas
        val calculatedStart = calculateStartTime()

        val intent = Intent(context, ActivityAlarmReceiver::class.java).apply {
            putExtra("ACTIVITY_NAME", activity.name)
            putExtra("DURATION_MS", durationMs)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            activity.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            // Programamos el inicio exacto
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calculatedStart,
                pendingIntent
            )
        } catch (e: SecurityException) {
            Toast.makeText(context, "Error de permisos de alarma", Toast.LENGTH_LONG).show()
        }
    }


    // Función de validación
    fun validateAndSave() {
        // CU-01/CP-02: Validación de campos vacíos
        if (name.isBlank()) {
            errorMessage = "El nombre de la actividad es obligatorio"
            showError = true
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            return
        }

        // Validar límite de caracteres
        if (description.length > MAX_CHARS) {
            errorMessage = "La nota excede el límite de $MAX_CHARS caracteres"
            showError = true
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            return
        }

        // Crear actividad
        val activity = Activity(
            id = existingActivity?.id ?: UUID.randomUUID().toString(),
            name = name,
            description = description,
            duration = duration.toIntOrNull() ?: 0,
            completed = existingActivity?.completed ?: false
        )

        try {
            if (isEditing) {
                viewModel.updateActivity(activity.id, activity)
                Toast.makeText(context, "Actividad actualizada correctamente", Toast.LENGTH_SHORT).show()
            } else {

                scheduleActivity(context, activity)

                viewModel.addActivity(activity)
                Toast.makeText(context, "Actividad guardada correctamente", Toast.LENGTH_SHORT).show()
            }
            onNavigateBack()
        } catch (e: Exception) {
            errorMessage = "Error al guardar los datos"
            showError = true
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Editar Actividad" else "Agregar Actividad",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { validateAndSave() }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Guardar")
                        Text("Guardar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Campo Nombre (OBLIGATORIO)
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    showError = false
                },
                label = { Text("Nombre de la actividad *") },
                placeholder = { Text("Ej: Correr 5km") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = showError && name.isBlank(),
                supportingText = {
                    if (showError && name.isBlank()) {
                        Text(
                            text = "⚠️ Este campo es obligatorio",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            )

            // Campo Descripción/Nota (OPCIONAL)
            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                    showError = false
                },
                label = { Text("Nota / Descripción (opcional)") },
                placeholder = { Text("Escribe una nota o descripción...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                isError = showError && description.length > MAX_CHARS,
                supportingText = {
                    if (description.length > MAX_CHARS) {
                        Text(
                            text = "⚠️ Límite: ${description.length}/$MAX_CHARS caracteres",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    } else {
                        Text(
                            text = "${description.length}/$MAX_CHARS caracteres",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            )

            // Duración (OPCIONAL)
            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it.filter { it.isDigit() } },
                label = { Text("Duración (minutos)") },
                placeholder = { Text("30") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón guardar
            Button(
                onClick = { validateAndSave() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditing) "Actualizar actividad" else "Agregar actividad")
            }
        }
    }
}