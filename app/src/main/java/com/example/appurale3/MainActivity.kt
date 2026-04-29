package com.example.appurale3

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.appurale3.auth.AuthViewModel
import com.example.appurale3.navigation.AppNaveHost
import com.example.appurale3.ui.theme.Appurale3Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var deepLinkRutinaId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        deepLinkRutinaId = extractRutinaId(intent)

        enableEdgeToEdge()

        setContent {
            Appurale3Theme {
                val authVM: AuthViewModel = hiltViewModel()
                val user by authVM.user.collectAsState()
                val navController = rememberNavController()




                AppNaveHost(
                    navController = navController,
                    startOnHome = user != null,
                    userId = user?.uid.orEmpty(),
                    deepLinkRutinaId = deepLinkRutinaId
                )
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