package com.example.appurale3.auth.presentation.login


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onGoToRegister : () -> Unit,
    onLoggedIn : () -> Unit,
    vm : LoginViewModel = hiltViewModel()
){
    val ui by vm.ui.collectAsState()

    var email by rememberSaveable { mutableStateOf("") }
    var pass by rememberSaveable { mutableStateOf("") }


    LaunchedEffect(vm) {
        vm.event.collect{ event ->
            when(event){
                LoginViewModel.Event.Success -> onLoggedIn()
            }
        }
    }

    Scaffold (
        topBar = {
            TopAppBar(
                title = {Text(text = "Login")}
            )

        }
    ){ paddingValues ->

        Column(
            Modifier.fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ){
            OutlinedTextField(
                value = email,
                onValueChange = {email = it},
                label = {Text(text = "Email")},
                singleLine = true,
                enabled = !ui.loading,
                modifier = Modifier.fillMaxWidth()


            )

            OutlinedTextField(
                value = pass,
                onValueChange = {pass = it},
                label = {Text(text = "Contraseña")},
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                enabled = !ui.loading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer( Modifier.height(12.dp))

            Button(

                enabled = !ui.loading && email.isNotBlank() && pass.isNotBlank(),
                onClick = { vm.singIn(email, pass) },
                modifier = Modifier.fillMaxWidth()
            ){
                if(ui.loading){
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                }else{
                    Text(text = "Entrar")
                }

            }

            ui.error?.let {
                Text(text = it,
                color= Color.Red,
                modifier = Modifier.padding(top = 8.dp)
                )
            }

            TextButton(
                onClick = onGoToRegister,
                enabled = !ui.loading,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(text = "Crear Cuenta")
            }

        }

    }


}
