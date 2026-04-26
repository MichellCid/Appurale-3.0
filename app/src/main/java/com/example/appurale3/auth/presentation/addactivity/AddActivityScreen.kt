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
import androidx.compose.ui.text.input.TextFieldValue
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

    // CU-11: Campo descripción (nota)
    var description by remember { mutableStateOf(existingActivity?.description ?: "") }
    var duration by remember { mutableStateOf(existingActivity?.duration?.toString() ?: "") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val isEditing = existingActivity != null
    val MAX_CHARS = 500  // CU-11-CP-05: Límite de caracteres

    fun validateAndSave() {
        // CU-11-CP-02: Campo descripción es OPCIONAL - no validamos que tenga texto

        // Validar solo el nombre (obligatorio)
        if (name.isBlank()) {
            errorMessage = "El nombre de la actividad es obligatorio"
            showError = true
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            return
        }

        // CU-11-CP-05: Validar límite de caracteres (máximo 500)
        if (description.length > MAX_CHARS) {
            errorMessage = "La nota excede el límite de $MAX_CHARS caracteres"
            showError = true
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            return
        }

        // En producción, esto lo maneja Firestore automáticamente

        val activity = Activity(
            id = existingActivity?.id ?: UUID.randomUUID().toString(),
            name = name,
            description = description,  // CU-11: La nota se guarda aquí
            duration = duration.toIntOrNull() ?: 0,
            isCompleted = existingActivity?.isCompleted ?: false
        )

        try {
            if (isEditing) {
                // CU-11-CP-03 y CU-11-CP-04: Actualizar nota en edición
                viewModel.updateActivity(activity.id, activity)
                Toast.makeText(context, "Nota actualizada correctamente", Toast.LENGTH_SHORT).show()
            } else {
                // CU-11-CP-01: Guardar actividad con nota
                viewModel.addActivity(activity)
                Toast.makeText(context, "Actividad guardada correctamente", Toast.LENGTH_SHORT).show()
            }
            onNavigateBack()
        } catch (e: Exception) {
            // CU-11-CP-07: Error al guardar
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
                        onClick = { validateAndSave() },
                        enabled = name.isNotBlank()
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
            // Nombre de la actividad (obligatorio)
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
                isError = showError && name.isBlank()
            )

            // CU-11: Campo DESCRIPCIÓN (NOTA)
            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                    showError = false
                },
                label = { Text("Nota / Descripción") },
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

            // Duración
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
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Text(if (isEditing) "Actualizar actividad" else "Agregar actividad")
            }
        }
    }
}