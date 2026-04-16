package com.example.appurale3.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appurale3.auth.data.AuthRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(repo : AuthRepository) : ViewModel() {
    val user: StateFlow<FirebaseUser?> = (repo.authState ?: emptyFlow()).stateIn(
        viewModelScope,
        SharingStarted.Eagerly, null
    )
}