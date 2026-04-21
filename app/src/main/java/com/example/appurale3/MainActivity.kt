package com.example.appurale3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.appurale3.auth.AuthViewModel
import com.example.appurale3.navigation.AppNaveHost
import com.example.appurale3.ui.theme.Appurale3Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Appurale3Theme {
                val authVM: AuthViewModel = hiltViewModel()
                val user by authVM.user.collectAsState()
                val navController = rememberNavController()

                AppNaveHost(
                    navController = navController,
                    startOnHome = user != null,
                    userId = user?.uid.orEmpty()
                )
            }
        }
    }
}