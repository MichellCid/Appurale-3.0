package com.example.appurale3.auth.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val auth : FirebaseAuth,
    private val db: FirebaseFirestore
) : AuthRepository {

    override val authState: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { fa ->
            trySend(fa.currentUser)
        }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser)
        awaitClose {
            auth.removeAuthStateListener(listener)
        }

    }.distinctUntilChanged()

    override suspend fun signIn(email: String, password: String): Result<Unit> =
        runCatching {
            auth.signInWithEmailAndPassword(email, password).await()
            Unit
        }



    override suspend fun register(email: String, password: String): Result<Unit> =
        runCatching {
            val result = auth.createUserWithEmailAndPassword(email, password).await()

            val userId = result.user?.uid ?: throw Exception("User null")

            val userMap = hashMapOf(
                "uid" to userId,
                "email" to email
            )

            db.collection("usuarios")
                .document(userId)
                .set(userMap)
                .await()

            Unit
        }

    override fun signOut() {
        auth.signOut()
    }

    override fun currentUser(): FirebaseUser? = auth.currentUser


}

