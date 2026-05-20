package com.example.appurale3.presentation.executeroutine

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appurale3.data.models.Activity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExecuteRoutineScreen(
    routineId: String,
    onNavigateBack: () -> Unit,
    viewModel: ExecuteRoutineViewModel = hiltViewModel()
) {

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // SENSOR
    val sensorManager = remember {
        context.getSystemService(
            SensorManager::class.java
        )
    }

    val accelerometer = remember {
        sensorManager?.getDefaultSensor(
            Sensor.TYPE_ACCELEROMETER
        )
    }

    val shakeDetector = remember {
        ShakeDetector {

            // Hace lo mismo que FINALIZAR RUTINA
            viewModel.finishRoutineAndStopAlarm()
            onNavigateBack()
        }
    }

    // Configurar callbacks
    LaunchedEffect(Unit) {

        viewModel.setOnPlayCompletionSound { soundUri ->
            viewModel.playCompletionSound(
                context,
                soundUri
            )
        }

        viewModel.setOnRoutineCompleted { soundUri, routineName ->

            viewModel.startAlarmSound(
                context,
                soundUri
            )

            viewModel.showCompletionNotification(
                context,
                routineName
            )
        }
    }

    // Libera recursos SOLO al salir de pantalla
    DisposableEffect(Unit) {
        onDispose {
            viewModel.releaseAllPlayers()
        }
    }

// Sensor únicamente cuando termina la rutina
    DisposableEffect(uiState.isCompleted) {

        if (uiState.isCompleted) {

            sensorManager?.registerListener(
                shakeDetector,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        onDispose {
            sensorManager?.unregisterListener(
                shakeDetector
            )
        }
    }

    LaunchedEffect(routineId) {
        viewModel.loadRoutine(
            routineId
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.routine?.name ?: "Ejecutar Rutina",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    if (!uiState.isCompleted && uiState.routine != null) {
                        TextButton(
                            onClick = { viewModel.completeRoutine() }
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Finalizar")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Finalizar")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.routine == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No se encontró la rutina")
            }
        } else if (uiState.isCompleted) {
            // Pantalla de rutina completada CON BOTÓN PARA DETENER ALARMA
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎉", style = MaterialTheme.typography.displayLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "¡Rutina completada!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Has completado todos los ejercicios",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Agita el teléfono de izquierda a derecha para detener la alarma",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // Botón para DETENER ALARMA y finalizar
                        Button(
                            onClick = {
                                viewModel.finishRoutineAndStopAlarm()
                                onNavigateBack()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Finalizar")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("FINALIZAR RUTINA")
                        }
                    }
                }
            }
        } else {
            val routine = uiState.routine!!
            val currentActivity = uiState.currentActivity
            val currentIndex = uiState.currentActivityIndex
            val timeRemaining = uiState.timeRemaining
            val isRunning = uiState.isRunning
            val isPaused = uiState.isPaused

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Progreso general
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Progreso de la rutina", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = uiState.routineProgress,
                                modifier = Modifier.fillMaxWidth().height(8.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${uiState.completedActivities}/${routine.activities.size} actividades completadas",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Actividad actual
                if (currentActivity != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Actividad actual", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(currentActivity.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                if (currentActivity.description.isNotEmpty()) {
                                    Text(currentActivity.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(formatTime(timeRemaining), style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                                Spacer(modifier = Modifier.height(16.dp))

                                // Dentro de la Card de "Actividad actual", después del temporizador

// Botones de control
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Botón Pausar/Reanudar
                                    Button(
                                        onClick = {
                                            if (isPaused || !isRunning) {
                                                viewModel.resumeActivity()
                                            } else {
                                                viewModel.pauseActivity()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        ),
                                        shape = RoundedCornerShape(50.dp)
                                    ) {
                                        Icon(
                                            if (isPaused || !isRunning) Icons.Default.PlayArrow else Icons.Default.Pause,
                                            contentDescription = if (isPaused || !isRunning) "Reanudar" else "Pausar"
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(if (isPaused || !isRunning) "Reanudar" else "Pausar")
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    // Botón Reiniciar
                                    Button(
                                        onClick = { viewModel.restartActivity() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        ),
                                        shape = RoundedCornerShape(50.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Reiniciar")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Reiniciar")
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    // Botón Siguiente / Terminar
                                    Button(
                                        onClick = {
                                            if (currentIndex + 1 >= routine.activities.size) {
                                                viewModel.completeRoutine()
                                            } else {
                                                viewModel.nextActivity()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        shape = RoundedCornerShape(50.dp)
                                    ) {
                                        Icon(Icons.Default.SkipNext, contentDescription = if (currentIndex + 1 >= routine.activities.size) "Terminar" else "Siguiente")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(if (currentIndex + 1 >= routine.activities.size) "Terminar" else "Siguiente")
                                    }
                                }
                            }
                        }
                    }
                }

                item { Text("Lista de actividades", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }

                itemsIndexed(routine.activities) { index, activity ->
                    ExecuteActivityItem(
                        activity = activity,
                        isCurrent = index == currentIndex,
                        isCompleted = index < currentIndex,
                        onClick = { if (index < currentIndex) viewModel.goToActivity(index) }
                    )
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun ExecuteActivityItem(
    activity: Activity,
    isCurrent: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = isCompleted) { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCurrent -> MaterialTheme.colorScheme.primaryContainer
                isCompleted -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(32.dp).padding(4.dp), contentAlignment = Alignment.Center) {
                when {
                    isCompleted -> Icon(Icons.Default.CheckCircle, contentDescription = "Completada", tint = MaterialTheme.colorScheme.primary)
                    isCurrent -> Icon(Icons.Default.PlayArrow, contentDescription = "En curso", tint = MaterialTheme.colorScheme.primary)
                    else -> Text((activity.duration / 60).toString(), style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(activity.name, style = MaterialTheme.typography.bodyLarge, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal)
                if (activity.duration > 0) {
                    Text("⏱️ ${activity.duration} min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (isCurrent) {
                Text("EN CURSO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun formatTime(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60
    return if (hours > 0) String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
    else String.format("%02d:%02d", minutes, remainingSeconds)
}