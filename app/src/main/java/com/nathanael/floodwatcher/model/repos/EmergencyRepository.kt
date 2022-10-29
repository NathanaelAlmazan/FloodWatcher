package com.nathanael.floodwatcher.model.repos

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.nathanael.floodwatcher.model.DbCollections
import com.nathanael.floodwatcher.model.EmergencyDirectory
import kotlinx.coroutines.tasks.await

class EmergencyRepository(private val database: FirebaseFirestore) {
    suspend fun fetchDirectories(type: String): Result<List<EmergencyDirectory>> {
        return try {
            val documents = database
                .collection(DbCollections.EMERGENCY.db)
                .document(type)
                .collection("directories")
                .get()
                .await()

            val emergencyDirectories = ArrayList<EmergencyDirectory>()
            for (document in documents) {
                emergencyDirectories.add(document.toObject())
            }

            Result.Success(emergencyDirectories)
        } catch (ex: Exception) {
            Result.Error(ex)
        }
    }
}