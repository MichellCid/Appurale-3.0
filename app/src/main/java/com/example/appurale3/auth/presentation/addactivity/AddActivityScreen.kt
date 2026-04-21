package com.example.appurale3.presentation.addactivity

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
import com.example.appurale3.data.models.Activity
import com.example.appurale3.presentation.detailroutine.DetailRoutineViewModel
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

    val isEditing = existingActivity != null

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
                        onClick = {
                            // VALIDACIÓN DE CAMPOS OBLIGATORIOS (CU-01/CP-02)
                            if (name.isBlank()) {
                                // Mostrar error de campo vacío
                                Toast.makeText(context, "Campos vacíos", Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }

                            // Si pasa validación, guardar (CU-01/CP-01)
                            val activity = Activity(
                                id = existingActivity?.id ?: UUID.randomUUID().toString(),
                                name = name,
                                description = description,
                                duration = duration.toIntOrNull() ?: 0,
                                isCompleted = existingActivity?.isCompleted ?: false
                            )
                            if (isEditing) {
                                viewModel.updateActivity(activity.id, activity)
                            } else {
                                viewModel.addActivity(activity)
                            }
                            onNavigateBack()
                        },
                        enabled = name.isNotBlank()  // Esto ya valida que el nombre no esté vacío
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre de la actividad *") },
                placeholder = { Text("Ej: Correr 5km") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                placeholder = { Text("Describe la actividad...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it.filter { it.isDigit() } },
                label = { Text("Duración (minutos)") },
                placeholder = { Text("30") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // VALIDACIÓN DE CAMPOS OBLIGATORIOS
                    if (name.isBlank()) {
                        Toast.makeText(context, "Campos vacíos", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val activity = Activity(
                        id = existingActivity?.id ?: UUID.randomUUID().toString(),
                        name = name,
                        description = description,
                        duration = duration.toIntOrNull() ?: 0,
                        isCompleted = existingActivity?.isCompleted ?: false
                    )
                    if (isEditing) {
                        viewModel.updateActivity(activity.id, activity)
                    } else {
                        viewModel.addActivity(activity)
                    }
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Text(if (isEditing) "Actualizar actividad" else "Agregar actividad")
            }
        }
    }
}