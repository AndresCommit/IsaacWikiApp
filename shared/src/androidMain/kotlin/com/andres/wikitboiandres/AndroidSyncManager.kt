package com.andres.wikitboiandres

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AndroidSyncManager : SyncManager {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("users")

    override suspend fun uploadAchievements(userId: String, achievementIds: List<Int>) {
        try {
            collection.document(userId)
                .set(mapOf("completedAchievements" to achievementIds))
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun downloadAchievements(userId: String): List<Int> {
        return try {
            val document = collection.document(userId).get().await()
            val list = document.get("completedAchievements") as? List<*>
            list?.mapNotNull { (it as? Long)?.toInt() } ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
