package com.example.appurale3

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.appurale3.auth.AuthViewModel
import com.example.appurale3.navigation.AppNaveHost
import com.example.appurale3.ui.theme.AppTheme
import com.example.appurale3.ui.theme.Appurale3Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var deepLinkRutinaId: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        deepLinkRutinaId = extractRutinaId(intent)

        enableEdgeToEdge()

        val sharedPreferences = getSharedPreferences("app_settings", Context.MODE_PRIVATE)

        setContent {

            val systemIsDark = isSystemInDarkTheme()

            val initialTheme = remember {
                val savedTheme = sharedPreferences.getString("selected_theme", null)
                if (savedTheme != null) {
                    try {
                        AppTheme.valueOf(savedTheme)
                    } catch (e: Exception) {
                        if (systemIsDark) AppTheme.DARK else AppTheme.LIGHT
                    }
                } else {
                    if (systemIsDark) AppTheme.DARK else AppTheme.LIGHT
                }
            }

            var currentTheme by remember { mutableStateOf(initialTheme) }
            Appurale3Theme(theme = currentTheme) {
                val authVM: AuthViewModel = hiltViewModel()
                val user by authVM.user.collectAsState()
                val navController = rememberNavController()


                AppNaveHost(
                    navController = navController,
                    startOnHome = user != null,
                    userId = user?.uid.orEmpty(),
                    deepLinkRutinaId = deepLinkRutinaId,
                    currentTheme = currentTheme,
                    onThemeChange = { nuevoTema ->
                        currentTheme = nuevoTema
                        sharedPreferences.edit()
                            .putString("selected_theme", nuevoTema.name)
                            .apply()
                    }
                )
            }
        }

        // Verificar si se abrió desde notificación
        intent?.let {
            if (it.getBooleanExtra("openRoutine", false)) {
                val routineId = it.getStringExtra("routineId")
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)


        deepLinkRutinaId = extractRutinaId(intent)
    }

    private fun extractRutinaId(intent: Intent?): String? {
        val data: Uri? = intent?.data

        val idQuery = data?.getQueryParameter("id")
        if (!idQuery.isNullOrEmpty()) return idQuery

        return data?.lastPathSegment
    }
}