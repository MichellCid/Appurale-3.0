package com.example.appurale3.auth.presentation.sound

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appurale3.data.repositories.SoundItem
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SoundPickerScreen(
    onSoundSelected: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SoundPickerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedSoundId by remember { mutableStateOf<String?>(null) }

    // Permisos
    val permissionState = rememberPermissionState(Manifest.permission.READ_MEDIA_AUDIO)
    val permissionStateOld = rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE)

    val hasPermission = if (android.os.Build.VERSION.SDK_INT >= 33) {
        permissionState.status.isGranted
    } else {
        permissionStateOld.status.isGranted
    }

    // Launcher para selector de archivos
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.addCustomSound(
                uri = uri,
                contentResolver = context.contentResolver,  // ← Pasar ContentResolver
                onSuccess = { soundUri ->
                    onSoundSelected(soundUri)
                    onNavigateBack()
                }
            )
        }
    }

    // Solicitar permisos
    LaunchedEffect(Unit) {
        if (!hasPermission) {
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                permissionState.launchPermissionRequest()
            } else {
                permissionStateOld.launchPermissionRequest()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seleccionar sonido") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Botón para agregar sonido personalizado
            Button(
                onClick = {
                    if (hasPermission) {
                        audioPickerLauncher.launch("audio/*")
                    } else {
                        if (android.os.Build.VERSION.SDK_INT >= 33) {
                            permissionState.launchPermissionRequest()
                        } else {
                            permissionStateOld.launchPermissionRequest()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
                Spacer(modifier = Modifier.weight(0.5f))
                Text("Agregar nuevo sonido")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Sonidos predeterminados",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.defaultSounds) { sound ->
                        SoundItemCard(
                            sound = sound,
                            isSelected = selectedSoundId == sound.id,
                            onSelect = {
                                selectedSoundId = sound.id
                                onSoundSelected(sound.uri)
                                onNavigateBack()
                            },
                            onPreview = {
                                viewModel.previewSound(sound.uri)
                            }
                        )
                    }
                }
            }
        }
    }

    uiState.errorMessage?.let {
        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        viewModel.clearError()
    }
}

@Composable
fun SoundItemCard(
    sound: SoundItem,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onPreview: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = sound.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (sound.id.startsWith("custom_")) {
                    Text(
                        text = "📁 Personalizado",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row {
                IconButton(onClick = onPreview) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Vista previa")
                }
                if (isSelected) {
                    Icon(Icons.Default.Check, contentDescription = "Seleccionado")
                }
            }
        }
    }
}