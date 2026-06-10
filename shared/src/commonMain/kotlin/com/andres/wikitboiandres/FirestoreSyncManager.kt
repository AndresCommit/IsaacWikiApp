package com.andres.wikitboiandres

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore

class FirestoreSyncManager : SyncManager {
    private val firestore = Firebase.firestore
    private val collection = firestore.collection("users")

    override suspend fun uploadAchievements(userId: String, achievementIds: List<Int>) {
        if (userId.isBlank()) return
        try {
            val document = collection.document(userId)
            // Usamos merge = true para actualizar solo el campo de logros
            document.set(mapOf("completedAchievements" to achievementIds), merge = true)
        } catch (e: Exception) {
            println("Error uploadAchievements KMP: ${e.message}")
        }
    }

    override suspend fun downloadAchievements(userId: String): List<Int> {
        if (userId.isBlank()) return emptyList()
        return try {
            val snapshot = collection.document(userId).get()
            if (snapshot.exists) {
                // GitLive devuelve un Map al usar data()
                val data: Map<String, Any?> = snapshot.data()
                val list = data["completedAchievements"] as? List<*>
                list?.mapNotNull { (it as? Number)?.toInt() } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("Error downloadAchievements KMP: ${e.message}")
            emptyList()
        }
    }
}
