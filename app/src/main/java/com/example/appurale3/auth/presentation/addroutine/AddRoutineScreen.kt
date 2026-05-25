package com.example.appurale3.auth.presentation.addroutine

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appurale3.auth.presentation.sound.SoundPickerScreen
import com.example.appurale3.data.models.Activity

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
    var showTimePicker by remember { mutableStateOf(false) }
    var isCustomCategory by remember { mutableStateOf(false) }
    var customCategoryText by remember { mutableStateOf("") }
    var showSoundPicker by remember { mutableStateOf(false) }

    // Estados para el selector manual de fecha
    var selectedYear by remember { mutableStateOf(2026) }
    var selectedMonth by remember { mutableStateOf(5) }  // 0-11, mayo = 4
    var selectedDay by remember { mutableStateOf(25) }

    val categories = listOf("Trabajo", "Estudio", "Ejercicio", "Salud", "Personal", "Otro")
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
                                IconButton(onClick = { showCategoryMenu = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Seleccionar")
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

                    // ==================== FECHA Y HORA ====================
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // FECHA - con selector manual (sin problemas de zona horaria)
                        OutlinedTextField(
                            value = uiState.date,
                            onValueChange = {},
                            label = { Text("Fecha") },
                            placeholder = { Text("DD/MM/AAAA") },
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    // Cargar fecha actual al abrir selector
                                    if (uiState.date.isNotEmpty()) {
                                        try {
                                            val parts = uiState.date.split("/")
                                            selectedDay = parts[0].toInt()
                                            selectedMonth = parts[1].toInt() - 1
                                            selectedYear = parts[2].toInt()
                                        } catch (e: Exception) { }
                                    }
                                    showDatePicker = true
                                },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Seleccionar fecha")
                                }
                            }
                        )

                        // HORA
                        OutlinedTextField(
                            value = uiState.hour,
                            onValueChange = {},
                            label = { Text("Hora") },
                            placeholder = { Text("HH:MM") },
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showTimePicker = true },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showTimePicker = true }) {
                                    Text("🕐", fontSize = MaterialTheme.typography.bodyLarge.fontSize)
                                }
                            }
                        )
                    }
                    // =====================================================

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
                        value = if (uiState.soundUri.isNotEmpty()) "✅ Sonido seleccionado" else "",
                        onValueChange = {},
                        label = { Text("Sonido") },
                        placeholder = { Text("Seleccionar sonido") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSoundPicker = true },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showSoundPicker = true }) {
                                Text("🔊", fontSize = MaterialTheme.typography.bodyLarge.fontSize)
                            }
                        }
                    )
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (uiState.isLoading) {
                        androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
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

    // SoundPicker
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

    // ==================== SELECTOR MANUAL DE FECHA (SIN PROBLEMAS DE ZONA HORARIA) ====================
    if (showDatePicker) {
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text("Seleccionar fecha") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Selector de día
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Día", style = MaterialTheme.typography.labelSmall)
                            IconButton(onClick = { if (selectedDay < 31) selectedDay++ }) {
                                Text("▲", fontSize = MaterialTheme.typography.headlineSmall.fontSize)
                            }
                            Text(selectedDay.toString(), style = MaterialTheme.typography.headlineMedium)
                            IconButton(onClick = { if (selectedDay > 1) selectedDay-- }) {
                                Text("▼", fontSize = MaterialTheme.typography.headlineSmall.fontSize)
                            }
                        }

                        Spacer(modifier = Modifier.width(32.dp))

                        // Selector de mes
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Mes", style = MaterialTheme.typography.labelSmall)
                            IconButton(onClick = { if (selectedMonth < 11) selectedMonth++ }) {
                                Text("▲", fontSize = MaterialTheme.typography.headlineSmall.fontSize)
                            }
                            Text((selectedMonth + 1).toString(), style = MaterialTheme.typography.headlineMedium)
                            IconButton(onClick = { if (selectedMonth > 0) selectedMonth-- }) {
                                Text("▼", fontSize = MaterialTheme.typography.headlineSmall.fontSize)
                            }
                        }

                        Spacer(modifier = Modifier.width(32.dp))

                        // Selector de año
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Año", style = MaterialTheme.typography.labelSmall)
                            IconButton(onClick = { selectedYear++ }) {
                                Text("▲", fontSize = MaterialTheme.typography.headlineSmall.fontSize)
                            }
                            Text(selectedYear.toString(), style = MaterialTheme.typography.headlineMedium)
                            IconButton(onClick = { selectedYear-- }) {
                                Text("▼", fontSize = MaterialTheme.typography.headlineSmall.fontSize)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val dateString = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
                        viewModel.updateDate(dateString)
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
        )
    }

    // ==================== SELECTOR DE HORA ====================
    if (showTimePicker) {
        val currentHour = if (uiState.hour.isNotEmpty() && uiState.hour.contains(":")) {
            uiState.hour.split(":")[0].toIntOrNull() ?: 12
        } else 12

        val currentMinute = if (uiState.hour.isNotEmpty() && uiState.hour.contains(":")) {
            uiState.hour.split(":")[1].toIntOrNull() ?: 0
        } else 0

        val timePickerState = rememberTimePickerState(
            initialHour = currentHour,
            initialMinute = currentMinute,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Seleccionar hora") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val hour = timePickerState.hour.toString().padStart(2, '0')
                        val minute = timePickerState.minute.toString().padStart(2, '0')
                        viewModel.updateHour("$hour:$minute")
                        showTimePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ActivityRow(
    activity: Activity,
    onRemove: () -> Unit,
    onToggleActive: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if(activity.active)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (activity.active) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    textDecoration = if (!activity.active) TextDecoration.LineThrough else null
                )
                if (activity.description.isNotEmpty()) {
                    Text(activity.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (activity.duration > 0) {
                    Text("⏱️ ${activity.duration} min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onToggleActive,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activity.active) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primary,
                        contentColor = if (activity.active) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(text = if (activity.active) "Desactivar" else "Activar", style = MaterialTheme.typography.labelMedium)
                }

                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}