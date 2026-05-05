package com.example.appurale3.auth.presentation.detailroutine

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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appurale3.data.models.Routine
import kotlinx.coroutines.delay

@Composable
fun ActivityProgressScreen(
    routine: Routine,
    currentIndex: Int,
    onNext: (Int) -> Unit,
    onFinish: () -> Unit,
    onBack: () -> Unit
) {

    val activity = routine.activities[currentIndex]
    val hasNext = currentIndex < routine.activities.lastIndex

    val totalTime = activity.duration * 60
    var timeLeft by remember { mutableStateOf(totalTime) }
    var isRunning by remember { mutableStateOf(false) }

    val timeProgress = 1f - (timeLeft / totalTime.toFloat())

    val animatedProgress by animateFloatAsState(
        targetValue = timeProgress,
        label = "progressAnimation"
    )

    LaunchedEffect(isRunning, timeLeft) {
        if (isRunning && timeLeft > 0) {
            delay(1000)
            timeLeft--
        }

        if (timeLeft == 0 && isRunning) {
            isRunning = false
            delay(500)

            if (hasNext) onNext(currentIndex + 1)
            else onFinish()
        }
    }

    LaunchedEffect(currentIndex) {
        timeLeft = activity.duration * 60
        isRunning = true
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
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
            text = "${timeLeft / 60}:${(timeLeft % 60).toString().padStart(2, '0')}",
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(onClick = { isRunning = true }) {
                Text("Iniciar")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = { isRunning = false }) {
                Text("Pausar")
            }
        }
    }
}