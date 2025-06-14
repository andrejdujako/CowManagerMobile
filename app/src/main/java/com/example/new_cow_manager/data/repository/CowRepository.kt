package com.example.new_cow_manager.data.repository

import android.util.Log
import com.example.new_cow_manager.data.model.Cow
import com.example.new_cow_manager.data.model.CowExamination
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CowRepository {
    private val TAG = "CowRepository"
    private val firestore = FirebaseFirestore.getInstance()
    private val cowsCollection = firestore.collection("cows_mobile")
    private val examinationsCollection = firestore.collection("examinations_mobile")

    fun getAllCows(): Flow<List<Cow>> = callbackFlow {
        val subscription = cowsCollection
            .orderBy("cowNumber", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting cows", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val cows = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data
                        if (data != null) {
                            Cow.fromFirestore(data + mapOf("id" to doc.id))
                        } else null
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting cow document", e)
                        null
                    }
                } ?: emptyList()
                trySend(cows)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun getCowById(cowId: String): Cow? {
        return try {
            val doc = cowsCollection.document(cowId).get().await()
            if (doc.exists()) {
                val data = doc.data
                if (data != null) {
                    Cow.fromFirestore(data + mapOf("id" to doc.id))
                } else null
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cow by ID", e)
            null
        }
    }

    fun getCowExaminations(cowId: String): Flow<List<CowExamination>> = callbackFlow {
        val subscription = examinationsCollection
            .whereEqualTo("cowId", cowId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val examinations = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val examination = doc.toObject(CowExamination::class.java)
                        examination?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(examinations)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun updateCow(cow: Cow): Result<Unit> {
        return try {
            if (cow.cowNumber.isBlank()) {
                throw IllegalArgumentException("Cow number is required")
            }
            if (cow.id.isBlank()) {
                throw IllegalArgumentException("Invalid cow ID")
            }

            Log.d(TAG, "Updating cow: ${cow.id} with dates: insemination=${cow.inseminationDate}, birth=${cow.birthDate}")

            val cowData = cow.toFirestore()
            cowsCollection.document(cow.id)
                .set(cowData)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating cow", e)
            Result.failure(e)
        }
    }

    suspend fun addCow(cow: Cow): Result<String> {
        return try {
            if (cow.cowNumber.isBlank()) {
                throw IllegalArgumentException("Cow number is required")
            }

            Log.d(TAG, "Adding new cow with dates: insemination=${cow.inseminationDate}, birth=${cow.birthDate}")
            val docRef = cowsCollection.add(cow.toFirestore()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding cow", e)
            Result.failure(e)
        }
    }

    fun searchCowsByNumber(query: String): Flow<List<Cow>> = callbackFlow {
        val subscription = cowsCollection
            .orderBy("cowNumber")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error searching cows", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val cows = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data
                        if (data != null) {
                            Cow.fromFirestore(data + mapOf("id" to doc.id))
                        } else null
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting cow document in search", e)
                        null
                    }
                } ?: emptyList()
                trySend(cows)
            }
        awaitClose { subscription.remove() }
    }

    fun getCowsByPregnancyDuration(minDays: Int): Flow<List<Cow>> = callbackFlow {
        val subscription = cowsCollection
            .whereEqualTo("pregnant", true)
            .whereGreaterThanOrEqualTo("pregnancyDuration", minDays)
            .orderBy("pregnancyDuration", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error filtering by pregnancy", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val cows = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data
                        if (data != null) {
                            Cow.fromFirestore(data + mapOf("id" to doc.id))
                        } else null
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting cow document in pregnancy filter", e)
                        null
                    }
                } ?: emptyList()
                trySend(cows)
            }
        awaitClose { subscription.remove() }
    }
}
