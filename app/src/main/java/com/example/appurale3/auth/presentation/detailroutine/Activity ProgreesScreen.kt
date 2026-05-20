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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appurale3.data.models.Routine
import com.example.appurale3.presentation.detailroutine.DetailRoutineViewModel
import kotlinx.coroutines.delay
import kotlin.concurrent.timer

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
    //viewModel: DetailRoutineViewModel = viewModel()
) {

    val activity = routine.activities.getOrNull(currentIndex) ?: return
    val hasNext = currentIndex < routine.activities.lastIndex

    val timers by viewModel.timers.collectAsState()
    val timer = timers[activity.id]

    //var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    //val timeLeft = viewModel.getTimeLeft(activity)
    //val totalMillis = activity.duration * 60 * 1000L

    /**val elapsed = if (timer?.isRunning == true && timer.startTime != null) {
        timer.accumulatedTime + (currentTime - timer.startTime)
    } else {
        timer?.accumulatedTime ?: 0L
    }**/

    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    val startTime = viewModel.getActivityStartTime(routine, currentIndex)

    val totalMillis = activity.duration * 60 * 1000L

    val elapsed = (currentTime - startTime).coerceAtLeast(0)

    val timeLeft = (totalMillis - elapsed).coerceAtLeast(0)

    //val timeLeft = (totalMillis - elapsed).coerceAtLeast(0)

    val minutes = (timeLeft / 1000) / 60
    val seconds = (timeLeft / 1000) % 60

    //val totalMillis = activity.duration * 60 * 1000L

    val progress = if (totalMillis > 0) {
        1f - (timeLeft / totalMillis.toFloat())
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


    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }


    LaunchedEffect(timeLeft == 0L) {
        if (timeLeft == 0L) {
            delay(500)
            if (hasNext) onNext(currentIndex + 1)
            else onFinish()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Detalles actividad", fontWeight = FontWeight.Bold)
                },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(activity.name, style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(16.dp))

            CircularProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier.size(160.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "$minutes:${seconds.toString().padStart(2, '0')}",
                style = MaterialTheme.typography.displayLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            /**Row {

                Button(onClick = {
                    viewModel.startTimer(activity.id, userId)
                }) {
                    Text("Iniciar")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(onClick = {
                    viewModel.pauseTimer(activity.id, userId)
                }) {
                    Text("Pausar")
                }
            }**/
        }
    }
}