package com.example.appurale3.auth.presentation.addroutine

import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appurale3.auth.presentation.sound.SoundPickerScreen
import com.example.appurale3.data.models.Activity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRoutineScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    viewModel: AddRoutineViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showCategoryMenu by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isCustomCategory by remember { mutableStateOf(false) }
    var customCategoryText by remember { mutableStateOf("") }
    var showSoundPicker by remember { mutableStateOf(false) }  // ← MOVIDO AQUÍ (antes del Scaffold)

    val categories = listOf("Trabajo", "Estudio", "Ejercicio", "Salud", "Personal", "Otro")
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val MAX_CHARS_DESCRIPTION = 500

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Agregar Rutina",
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
                        onClick = { viewModel.saveRoutine(userId, onNavigateBack) }
                    ) {
                        Text("Guardar", fontWeight = FontWeight.Medium)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta de información de la rutina
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Nombre de la rutina
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = viewModel::updateName,
                        label = { Text("Nombre de la rutina") },
                        placeholder = { Text("Ej: Rutina Matutina") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Descripción
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = { newValue ->
                            if (newValue.length <= MAX_CHARS_DESCRIPTION) {
                                viewModel.updateDescription(newValue)
                            }
                        },
                        label = { Text("Descripción") },
                        placeholder = { Text("Describe tu rutina...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        isError = uiState.description.length > MAX_CHARS_DESCRIPTION,
                        supportingText = {
                            Text(
                                text = "${uiState.description.length}/$MAX_CHARS_DESCRIPTION caracteres",
                                color = if (uiState.description.length > MAX_CHARS_DESCRIPTION)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )

                    // Categoría
                    Column {
                        OutlinedTextField(
                            value = if (isCustomCategory) customCategoryText else uiState.category,
                            onValueChange = { newValue ->
                                if (isCustomCategory) {
                                    customCategoryText = newValue
                                    viewModel.updateCategory(newValue)
                                } else {
                                    viewModel.updateCategory(newValue)
                                }
                            },
                            label = { Text("Categoría") },
                            placeholder = { Text("Seleccionar o escribir categoría") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isCustomCategory = false
                                    showCategoryMenu = true
                                },
                            trailingIcon = {
                                IconButton(
                                    onClick = { showCategoryMenu = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Seleccionar", modifier = Modifier.size(20.dp))
                                }
                            }
                        )

                        DropdownMenu(
                            expanded = showCategoryMenu,
                            onDismissRequest = { showCategoryMenu = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        isCustomCategory = false
                                        viewModel.updateCategory(category)
                                        showCategoryMenu = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("✏️ Escribir categoría personalizada") },
                                onClick = {
                                    isCustomCategory = true
                                    customCategoryText = ""
                                    viewModel.updateCategory("")
                                    showCategoryMenu = false
                                }
                            )
                        }
                    }

                    // Fecha y hora
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.date?.let { dateFormat.format(it) } ?: "",
                            onValueChange = {},
                            label = { Text("Fecha") },
                            placeholder = { Text("DD/MM/AAAA") },
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showDatePicker = true },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = { showDatePicker = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Seleccionar fecha",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        )

                        OutlinedTextField(
                            value = uiState.hour,
                            onValueChange = viewModel::updateHour,
                            label = { Text("Hora") },
                            placeholder = { Text("HH:MM") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Duración
                    OutlinedTextField(
                        value = uiState.duration,
                        onValueChange = viewModel::updateDuration,
                        label = { Text("Duración") },
                        placeholder = { Text("Minutos") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Sonido
                    OutlinedTextField(
                        value = if (uiState.soundUri.isNotEmpty()) {
                            "✅ Sonido seleccionado"
                        } else "",
                        onValueChange = {},
                        label = { Text("Sonido") },
                        placeholder = { Text("Seleccionar sonido") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSoundPicker = true },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(
                                onClick = { showSoundPicker = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Text("🔊", fontSize = MaterialTheme.typography.bodyLarge.fontSize)
                            }
                        }
                    )
                }
            }

            // Sección de actividades
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Actividades",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    if (uiState.activities.isNotEmpty()) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.activities.forEach { activity ->
                                ActivityRow(
                                    activity = activity,
                                    onRemove = { viewModel.removeActivity(activity.id) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Button(
                        onClick = { /* TODO: Navegar a pantalla de agregar actividad */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Add,
                            contentDescription = "Agregar",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar actividades")
                    }

                    if (uiState.activities.isEmpty()) {
                        Text(
                            text = "💡 Puedes guardar la rutina sin actividades y agregarlas después",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // Botones Guardar y Cancelar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.saveRoutine(userId, onNavigateBack) },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (uiState.isLoading) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar")
                    }
                }

                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancelar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // SoundPicker - MOVIDO FUERA DEL SCAFFOLD
    if (showSoundPicker) {
        SoundPickerScreen(
            onSoundSelected = { soundUri ->
                viewModel.updateSoundUri(soundUri)
                showSoundPicker = false
                Toast.makeText(context, "Sonido seleccionado", Toast.LENGTH_SHORT).show()
            },
            onNavigateBack = { showSoundPicker = false }
        )
    }

    // DatePicker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Date(millis)
                            viewModel.updateDate(date)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        }
    }
}

@Composable
fun ActivityRow(
    activity: Activity,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = activity.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                if (activity.description.isNotEmpty()) {
                    Text(
                        text = activity.description,
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

            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}