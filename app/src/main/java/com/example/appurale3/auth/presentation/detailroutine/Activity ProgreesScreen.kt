package com.example.appurale3.auth.presentation.detailroutine

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appurale3.data.models.Routine
import com.example.appurale3.presentation.detailroutine.DetailRoutineViewModel
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityProgressScreen(
    routine: Routine,
    currentIndex: Int,
    userId: String,
    viewModel: DetailRoutineViewModel,
    onNext: (Int) -> Unit,
    onFinish: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val activity = routine.activities.getOrNull(currentIndex) ?: return
    val hasNext = currentIndex < routine.activities.lastIndex
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    val totalMillis = activity.duration * 60 * 1000L
    val startTime = viewModel.getActivityStartTime(routine, currentIndex)
    val elapsed = (currentTime - startTime).coerceAtLeast(0)
    val timeLeft = (totalMillis - elapsed).coerceAtLeast(0)

    val minutes = (timeLeft / 1000) / 60
    val seconds = (timeLeft / 1000) % 60

    val progress = if (totalMillis > 0) {
        (elapsed.toFloat() / totalMillis).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "progressAnimation"
    )

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }

    LaunchedEffect(activity.id) {
        viewModel.loadTimer(activity.id, userId)
    }

    LaunchedEffect(timeLeft == 0L) {
        if (timeLeft == 0L) {
            delay(500)
            viewModel.completeActivity(activity.id)
            if (hasNext) {
                onNext(currentIndex + 1)
            } else {
                onFinish()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("En progreso", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Título de la rutina
            Text(
                text = routine.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Progreso de la rutina
            val totalCompleted = routine.activities.take(currentIndex + 1).count { it.completed }
            val totalActivities = routine.activities.size
            val routineProgress = if (totalActivities > 0) totalCompleted.toFloat() / totalActivities else 0f

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Progreso de la rutina", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = routineProgress,
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        text = "$totalCompleted de $totalActivities actividades completadas",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Actividad actual
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Actividad actual", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(activity.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    if (activity.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(activity.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Temporizador
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier.size(180.dp),
                    strokeWidth = 8.dp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón PAUSAR/REANUDAR
            val timer = viewModel.timers.value[activity.id]
            val isRunning = timer?.isRunning == true

            Button(
                onClick = {
                    if (isRunning) {
                        viewModel.pauseTimer(activity.id, userId)
                    } else {
                        viewModel.startTimer(activity.id, userId)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text(if (isRunning) "Pausar" else "Reanudar")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ================================================================
            // BOTÓN SIGUIENTE ACTIVIDAD - AQUÍ ESTÁ
            // ================================================================
            Button(
                onClick = {
                    viewModel.completeActivity(activity.id)
                    if (hasNext) {
                        onNext(currentIndex + 1)
                    } else {
                        onFinish()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    if (hasNext) Icons.Default.SkipNext else Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (hasNext) "SIGUIENTE ACTIVIDAD" else "FINALIZAR",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Lista de actividades
            Text(
                text = "Lista de actividades",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            routine.activities.forEachIndexed { index, act ->
                val isCurrent = index == currentIndex
                val isCompleted = act.completed

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = act.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    isCompleted -> MaterialTheme.colorScheme.onSurfaceVariant
                                    isCurrent -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                            Text(
                                text = "${act.duration} min",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        when {
                            isCompleted -> Text("✓ COMPLETADA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            isCurrent -> Text("▶ EN CURSO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            else -> Text("⏳ PENDIENTE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}