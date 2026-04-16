package com.example.appurale3.auth.data

import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton

class FirestoreService @Inject constructor() {
    private val db = FirebaseFirestore.getInstance()

    fun getDb() = db
}