package com.example.appurale3.auth.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appurale3.data.models.CalendarEvent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onNavigateToAddActivity: (String) -> Unit = {},
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(userId) {
        viewModel.loadEvents(userId)
    }

    val calendar = Calendar.getInstance()
    calendar.time = uiState.selectedDate

    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Calendario",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAddActivity(userId) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar actividad")
            }
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
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header del calendario con navegación de meses
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Navegación de meses
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.goToPreviousMonth() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Mes anterior")
                            }

                            Text(
                                text = monthYearFormat.format(uiState.selectedDate).replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            IconButton(onClick = { viewModel.goToNextMonth() }) {
                                Icon(Icons.Default.ArrowForward, contentDescription = "Mes siguiente")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Días de la semana
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf("L", "M", "M", "J", "V", "S", "D").forEach { day ->
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Grid de días del mes
                        CalendarGrid(
                            currentDate = uiState.selectedDate,
                            eventsByDate = uiState.eventsByDate,
                            onDateSelected = { viewModel.selectDate(it) }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Botón para ir a hoy
                        Button(
                            onClick = { viewModel.goToToday() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ir a hoy")
                        }
                    }
                }

                // Actividades del día seleccionado
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Actividades del día",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(uiState.selectedDate),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (uiState.selectedDateEvents.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "📅",
                                        style = MaterialTheme.typography.displayMedium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Sin actividades para hoy",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Toca el botón + para agregar una actividad",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(uiState.selectedDateEvents) { event ->
                                    CalendarEventItem(
                                        event = event,
                                        onToggleCompletion = { viewModel.toggleActivityCompletion(event) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    uiState.errorMessage?.let {
        android.widget.Toast.makeText(
            androidx.compose.ui.platform.LocalContext.current,
            it,
            android.widget.Toast.LENGTH_SHORT
        ).show()
        viewModel.clearError()
    }
}

@Composable
fun CalendarGrid(
    currentDate: Date,
    eventsByDate: Map<String, List<CalendarEvent>>,
    onDateSelected: (Date) -> Unit
) {
    val calendar = Calendar.getInstance()
    calendar.time = currentDate

    // Primer día del mes
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK) - 1 // Ajustar a lunes como primer día
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = Calendar.getInstance()
    today.set(Calendar.HOUR_OF_DAY, 0)
    today.set(Calendar.MINUTE, 0)
    today.set(Calendar.SECOND, 0)
    today.set(Calendar.MILLISECOND, 0)

    val rows = (firstDayOfMonth + daysInMonth + 6) / 7

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0 until 7) {
                    val dayNumber = row * 7 + col - firstDayOfMonth + 1
                    val isValidDay = dayNumber in 1..daysInMonth

                    if (isValidDay) {
                        calendar.set(Calendar.DAY_OF_MONTH, dayNumber)
                        val dateKey = dateFormat.format(calendar.time)
                        val hasEvents = eventsByDate[dateKey]?.isNotEmpty() == true
                        val isToday = calendar.timeInMillis == today.timeInMillis

                        DayCell(
                            day = dayNumber,
                            hasEvents = hasEvents,
                            isToday = isToday,
                            onClick = {
                                val selectedCalendar = Calendar.getInstance()
                                selectedCalendar.time = currentDate
                                selectedCalendar.set(Calendar.DAY_OF_MONTH, dayNumber)
                                onDateSelected(selectedCalendar.time)
                            }
                        )
                    } else {
                        Box(modifier = Modifier.weight(1f).padding(4.dp)) {
                            Text("", modifier = Modifier.size(36.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.DayCell(
    day: Int,
    hasEvents: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .padding(4.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (isToday) MaterialTheme.colorScheme.primary
                    else if (hasEvents) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday || hasEvents) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isToday -> MaterialTheme.colorScheme.onPrimary
                    hasEvents -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Composable
fun CalendarEventItem(
    event: CalendarEvent,
    onToggleCompletion: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = event.isCompleted,
                    onCheckedChange = { onToggleCompletion() }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (event.isCompleted) TextDecoration.LineThrough else null
                    )
                    if (event.routineName.isNotEmpty()) {
                        Text(
                            text = "📌 ${event.routineName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (event.description.isNotEmpty()) {
                        Text(
                            text = event.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (event.hour.isNotEmpty() || event.duration > 0) {
                        Text(
                            text = "⏰ ${event.hour} • ${event.duration} min",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (event.isCompleted) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Completada",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
