package com.example.appurale3.data.models.repositories

import com.example.appurale3.data.models.ActivityTimer
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class TimerRepository @Inject constructor(){

    private val db = FirebaseFirestore.getInstance()

    suspend fun saveTimer(userId: String, timer: ActivityTimer) {
        db.collection("timers")
            .document(userId)
            .collection("activities")
            .document(timer.activityId)
            .set(timer)
            .await()
    }

    suspend fun getTimer(userId: String, activityId: String): ActivityTimer? {
        val doc = db.collection("timers")
            .document(userId)
            .collection("activities")
            .document(activityId)
            .get()
            .await()

        return doc.toObject(ActivityTimer::class.java)
    }
}