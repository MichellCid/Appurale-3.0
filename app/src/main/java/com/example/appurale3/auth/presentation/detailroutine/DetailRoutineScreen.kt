package com.example.appurale3.presentation.detailroutine

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appurale3.data.models.Activity
import com.example.appurale3.data.models.Routine
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailRoutineScreen(
    routineId: String,
    onNavigateBack: () -> Unit,
    onNavigateToAddActivity: (String) -> Unit,
    onNavigateToEditActivity: (String, Activity) -> Unit,
    onNavigateToActivityProgress: (String, String) -> Unit,
    viewModel: DetailRoutineViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isEditing by remember { mutableStateOf(false) }
    var editedRoutine by remember { mutableStateOf(uiState.routine) }
    val context = LocalContext.current

    // Estados para el diálogo de confirmación de eliminación de actividad (CU-03)
    var showDeleteActivityDialog by remember { mutableStateOf(false) }
    var activityToDelete by remember { mutableStateOf<Activity?>(null) }

    // Estados para el diálogo de confirmación del checkbox (CU-09)
    var showCheckboxDialog by remember { mutableStateOf(false) }
    var pendingActivity by remember { mutableStateOf<Activity?>(null) }

    LaunchedEffect(routineId) {
        viewModel.loadRoutine(routineId)
    }

    // Ex-01: Mostrar error si ocurre al guardar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isEditing) {
                        OutlinedTextField(
                            value = editedRoutine?.name ?: "",
                            onValueChange = { editedRoutine = editedRoutine?.copy(name = it) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else {
                        Text(
                            text = uiState.routine?.name ?: "Detalle",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    // Botón Compartir (CU-18)
                    IconButton(onClick = {
                        compartirRutina(context, routineId)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir")
                    }

                    if (!isEditing) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = { viewModel.toggleDeleteDialog() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                    } else {
                        TextButton(
                            onClick = {
                                editedRoutine?.let {
                                    viewModel.updateRoutine(it) {
                                        isEditing = false
                                    }
                                }
                            }
                        ) {
                            Text("Guardar")
                        }
                        TextButton(onClick = { isEditing = false }) {
                            Text("Cancelar")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAddActivity(routineId) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
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
        } else if (uiState.routine == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No se encontró la rutina")
            }
        } else {
            val routine = uiState.routine!!

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tarjeta de información de la rutina
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!isEditing) {
                                if (routine.description.isNotEmpty()) {
                                    Text(
                                        text = routine.description,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (routine.category.isNotEmpty()) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.secondaryContainer
                                        ) {
                                            Text(
                                                text = routine.category,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    if (routine.duration > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.tertiaryContainer
                                        ) {
                                            Text(
                                                text = "⏱️ ${routine.duration} min",
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                if (routine.date != null || routine.hour.isNotEmpty()) {
                                    Text(
                                        text = "📅 ${routine.date?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) } ?: ""} ${routine.hour}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                // Modo edición
                                OutlinedTextField(
                                    value = editedRoutine?.description ?: "",
                                    onValueChange = { editedRoutine = editedRoutine?.copy(description = it) },
                                    label = { Text("Descripción") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 2
                                )

                                OutlinedTextField(
                                    value = editedRoutine?.category ?: "",
                                    onValueChange = { editedRoutine = editedRoutine?.copy(category = it) },
                                    label = { Text("Categoría") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = editedRoutine?.duration?.toString() ?: "",
                                    onValueChange = { editedRoutine = editedRoutine?.copy(duration = it.toIntOrNull() ?: 0) },
                                    label = { Text("Duración (minutos)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // Barra de progreso de la rutina
                item {
                    val total = routine.activities.size
                    val completed = routine.activities.count { it.completed }
                    val progress = if (total > 0) completed / total.toFloat() else 0f

                    Column {
                        Text(
                            text = "Progreso de la rutina",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "$completed de $total actividades completadas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Título de actividades
                item {
                    Text(
                        text = "Actividades",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Lista de actividades (CU-04)
                if (routine.activities.isEmpty()) {
                    // FA-01: Sin actividades
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("📝", style = MaterialTheme.typography.displayMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No hay actividades",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Toca el botón + para agregar",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    itemsIndexed(routine.activities) { index, activity ->
                        ActivityDetailItem(
                            activity = activity,
                            onToggleCompletion = {
                                // CU-09: Mostrar diálogo de confirmación antes de marcar
                                pendingActivity = activity
                                showCheckboxDialog = true
                            },
                            onEdit = {
                                onNavigateToEditActivity(routineId, activity)
                            },
                            onDelete = {
                                activityToDelete = activity
                                showDeleteActivityDialog = true
                            },
                            onClick = {
                                onNavigateToActivityProgress(routineId, index.toString())
                            }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    // Diálogo de confirmación para eliminar rutina (CU-07)
    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleDeleteDialog() },
            title = { Text("Eliminar rutina") },
            text = { Text("¿Estás seguro de que quieres eliminar esta rutina? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteRoutine(routineId, onNavigateBack) }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleDeleteDialog() }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de confirmación para eliminar actividad (CU-03)
    if (showDeleteActivityDialog && activityToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteActivityDialog = false
                activityToDelete = null
            },
            title = { Text("Eliminar actividad") },
            text = { Text("¿Estás seguro de que quieres eliminar \"${activityToDelete?.name}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        activityToDelete?.let { viewModel.removeActivity(it.id) }
                        showDeleteActivityDialog = false
                        activityToDelete = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteActivityDialog = false
                    activityToDelete = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de confirmación para marcar actividad como completada (CU-09)
    if (showCheckboxDialog && pendingActivity != null) {
        AlertDialog(
            onDismissRequest = {
                showCheckboxDialog = false
                pendingActivity = null
            },
            title = { Text("Marcar actividad") },
            text = { Text("¿Marcar \"${pendingActivity?.name}\" como cumplida?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // FA-01: Usuario confirma con "Aceptar"
                        try {
                            pendingActivity?.let { activity ->
                                viewModel.toggleActivityCompletion(activity.id)
                            }
                            showCheckboxDialog = false
                            pendingActivity = null
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error al marcar la actividad", Toast.LENGTH_SHORT).show()
                            showCheckboxDialog = false
                            pendingActivity = null
                        }
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        // FA-01: Usuario cancela la acción
                        showCheckboxDialog = false
                        pendingActivity = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ActivityDetailItem(
    activity: Activity,
    onToggleCompletion: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                // Checkbox para marcar completada (CU-09)
                Checkbox(
                    checked = activity.completed,
                    onCheckedChange = { onToggleCompletion() }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = activity.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (activity.completed) TextDecoration.LineThrough else null
                    )
                    // CU-11: Mostrar la NOTA (descripción)
                    if (activity.description.isNotEmpty()) {
                        Text(
                            text = "📝 ${activity.description}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (activity.duration > 0) {
                        Text(
                            text = "⏱️ ${activity.duration} min",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Botones de acción
            Row {
                // Botón Editar (CU-02)
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(20.dp))
                }
                // Botón Eliminar (CU-03) - con confirmación
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// Función para compartir rutina (CU-18)
fun compartirRutina(context: Context, rutinaId: String) {
    val link = "https://appurale.web.app/rutina/$rutinaId"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "📋 Mira mi rutina en Appurale: $link")
    }
    context.startActivity(Intent.createChooser(intent, "Compartir rutina"))
}