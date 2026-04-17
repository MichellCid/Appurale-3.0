package com.example.appurale3.auth.presentation.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.appurale3.auth.data.AuthRepository
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    val email: String = repo.currentUser()?.email.orEmpty()
    val uid: String = repo.currentUser()?.uid.orEmpty()

    fun logout() {
        repo.signOut()
    }
}