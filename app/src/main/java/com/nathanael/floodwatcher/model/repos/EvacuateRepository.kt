package com.nathanael.floodwatcher.model.repos

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.nathanael.floodwatcher.model.DbCollections
import com.nathanael.floodwatcher.model.EvacuationCenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/*
 This page fetches the evacuation centers
 in the database and handles request for
 directions
*/

class EvacuateRepository(private val database: FirebaseFirestore) {
    private val directionsApi = DirectionsRetrofitHelper.getInstance().create(DirectionsApi::class.java)

    suspend fun fetchEvacuationCenters(): Result<List<EvacuationCenter>> {
        return try {
            val documents = database.collection(DbCollections.EVACUATE.db)
                .get()
                .await()

            val evacuationCenters = ArrayList<EvacuationCenter>()
            for (document in documents) {
                evacuationCenters.add(document.toObject())
            }

            Result.Success(evacuationCenters)
        } catch (ex: Exception) {
            Result.Error(ex)
        }
    }

    suspend fun fetchDirection(start: LatLng, destination: LatLng): Result<DirectionResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val from = start.latitude.toString() + "," + start.longitude.toString()
                val to = destination.latitude.toString() + "," + destination.longitude.toString()

                val result = directionsApi
                    .getDirection(from, to)
                    .body() ?: throw Exception("Response is null.")

                Result.Success(result)
            } catch (ex: Exception) {
                Result.Error(ex)
            }
        }
    }

}