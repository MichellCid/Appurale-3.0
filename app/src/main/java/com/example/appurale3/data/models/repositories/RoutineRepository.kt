package com.example.appurale3.data.repositories

import com.example.appurale3.data.models.Routine
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val routinesCollection = firestore.collection("routines")

    suspend fun saveRoutine(routine: Routine): Result<String> = try {
        val docRef = if (routine.id.isEmpty()) {
            routinesCollection.document()
        } else {
            routinesCollection.document(routine.id)
        }
        val id = docRef.id
        docRef.set(routine.copy(id = id)).await()
        Result.success(id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getRoutinesByUser(userId: String): List<Routine> = try {
        println("🔍 getRoutinesByUser - userId: $userId")
        val snapshot = routinesCollection
            .whereEqualTo("userId", userId)
            //.orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()

        println("🔍 Snapshot size: ${snapshot.documents.size}")
        snapshot.documents.forEach { doc ->
            println("   📄 Documento ID: ${doc.id}")
            println("   📄 Datos: ${doc.data}")
        }

        val routines = snapshot.documents.mapNotNull { it.toObject<Routine>() }
        println("🔍 Routines convertidas: ${routines.size}")
        routines
    } catch (e: Exception) {
        println("❌ Error en getRoutinesByUser: ${e.message}")
        e.printStackTrace()
        emptyList()
    }

    suspend fun updateRoutine(routine: Routine): Result<Unit> = try {
        routinesCollection.document(routine.id).set(routine).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteRoutine(routineId: String): Result<Unit> = try {
        routinesCollection.document(routineId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getRoutineById(routineId: String): Routine? {
        val doc = firestore.collection("routines")
            .document(routineId)
            .get()
            .await()

        return doc.toObject(Routine::class.java)?.copy(id = doc.id)
    }

    suspend fun searchRoutinesRealTime(userId: String, query: String): List<Routine> {
        if (query.isBlank()) return emptyList()

        return try {
            val snapshot = routinesCollection
                .whereEqualTo("userId", userId)
                .orderBy("nameLowercase")
                .startAt(query.lowercase())
                .endAt(query.lowercase() + "\uf8ff")
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject<Routine>() }
        } catch (e: Exception) {
            emptyList()
        }

    }

}